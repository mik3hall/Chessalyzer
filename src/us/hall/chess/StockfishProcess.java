package us.hall.chess;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import chesspresso.game.Game;
import chesspresso.move.Move;
import chesspresso.pgn.PGNReader;
import chesspresso.pgn.PGNSyntaxError;
import static us.hall.chess.GameRecord.*;

public class StockfishProcess {

	private static final String[] procArgs = new String[] { "stockfish" };
	private static final ProcessBuilder pb = new ProcessBuilder(procArgs);
	static final PrintStream sysout = System.out;
	private Process p;
	private PrintStream procout;
	private int moveNum = 0;
	private Move move = null;
	MoveSet moveSet = new MoveSet();
	private final StringBuilder sb = new StringBuilder();
	private final HashMap<Integer, String> moveData = new HashMap();
	private final String pieces = "QKBNRP";
	private final String[] columns = new String[] { "a","b","c","d","e","f","g","h"};
	private final BufferedWriter writer;
	private final OutType outType;
	private String white, whiteELO, black, blackELO;
	private boolean DEBUG = false;
	private static final ConcurrentLinkedQueue<String> linesQ = new ConcurrentLinkedQueue<String>();
	
	public StockfishProcess(BufferedWriter writer, OutType outType) { 
		this.writer = writer;
		this.outType = outType;
		try {	
			p = pb.start();
			procout = new PrintStream(p.getOutputStream());
			final BufferedReader procin =  
				new BufferedReader(new InputStreamReader(p.getInputStream()));
			move("uci");
			int cores = Runtime.getRuntime().availableProcessors();
			if (cores > 1) {
				move("setoption name Threads value 3"); // + Math.max(2,(cores/2)));
			}
			move("setoption name MultiPV value 3");
			final StockfishProcess sfp = this;
			new Thread(new Runnable() {
				public void run() {
					String line = null;
					for (;;) {
						try {				
							while ((line = procin.readLine(  )) != null) {
								linesQ.add(line);
							}
						}
						catch (IOException ioex) { 
							ioex.printStackTrace(); 
						}	
						catch (Exception ex) {
							System.out.println("LINE: " + line);
							ex.printStackTrace();
						}
						try { Thread.currentThread().wait(100); }
						catch (InterruptedException iex) {
							iex.printStackTrace();
						}
					}			
				}
			},"Stockfish in").start(); 
		}
		catch (IOException ioex) { ioex.printStackTrace(); }
	}
	
	private void move(String command) {
		System.out.println(command);
		procout.println(command);
		procout.flush();
	}
	
	private void newGame() {
		move("ucinewgame");
		sb.setLength(0);
		sb.append("position startpos moves ");
		moveSet = new MoveSet();
	}
	
	private String getBestMove() {
		//move("go movetime 5000"); // depth 10");
		while (true) {
			while(linesQ.size() > 0) {
				String line = linesQ.poll();
				if (line.contains("multipv")) {
					if (DEBUG) {
						System.out.println("MULTIPV: " + line);
					}
					moveSet.addEval(line);
				}
				else if (line.contains("bestmove")) {
					if (DEBUG) {
						System.out.println("BEST: " + line);
					}
					String bestmove = line.split(" ")[1];
					moveSet.setBestMove(bestmove,move.isWhiteMove());
					return bestmove;
				}				
			}
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException iex) {
			
			}
		}
	}
	
	/*
	 * This is to evaluate an unanalyzed move
	 */
	private String searchActualMove(String actualMove) {
		moveSet.resetEvals();
		if (DEBUG) {
			moveSet.printContinuations();
		}
		move("go movetime 5000 search moves " + actualMove);
		String bestMove = getBestMove();
		if (DEBUG) {
			moveSet.printContinuations();
		}
		moveSet.updateScores(0, actualMove, move.isWhiteMove());
		return bestMove;
	}
	
	public synchronized void evalPGN(String path) {
		Path pgn = Paths.get(path);
		try {
			InputStream is = Files.newInputStream(pgn);
			PGNReader rdr = new PGNReader(is, "Chessalyzer");
			Game game = rdr.parseGame();
			while (game != null) {
				newGame();
				white = game.getWhite();
				whiteELO = game.getWhiteEloStr();
				black = game.getBlack();
				blackELO = game.getBlackEloStr();
				game.gotoStart();
				move("go movetime 5000");
				while (game.hasNextMove()) {
					move = game.getNextMove();
					moveNum = game.getCurrentMoveNumber();
					if (move.isWhiteMove()) {
						System.out.println(moveNum + ". " + stockfishify(move));
					}
					else {
						System.out.println(moveNum + ".  ...   " + stockfishify(move));
					}
					if (DEBUG) {
						System.out.println("getting best move - (game move: " + move.getSAN() + ")");
					}
					String bestmove = getBestMove();
					if (DEBUG) {
						System.out.println("got " + bestmove + "(game move: " + move.getSAN() + ")");
					}
					String moveText = stockfishify(move);
					if (!moveSet.addMove(moveText,move.isWhiteMove())) {
						bestmove = searchActualMove(sb.append(moveText).toString());
						// Should fail but stats updated
						//boolean checkNow = moveSet.addMove(moveText,move.isWhiteMove());
					}
 					else sb.append(moveText);
					move(sb.toString());
					move("go movetime 5000"); // depth 10");
					sb.append(" ");
					game.goForward();
				}
				if (writer != null) {
					// Build white/black game output records
					GameRecord gameRec = new GameRecord(moveSet, game, outType);
					write(gameRec);
					//writeIter(iterrec);
				}
				write(new GameRecord(moveSet, game, outType));
				game = rdr.parseGame();
				moveSet = new MoveSet();
			}
			move("quit");
		}
		catch (IOException ioex) {
			ioex.printStackTrace();
		}
		catch (PGNSyntaxError pgnex) {
			pgnex.printStackTrace();
		}
	}
	
	/**
	 * Format move how stockfish expects it
	 */
	private String stockfishify(Move m) {
		String s = m.getLAN().replace("-","").replace("x","");
		if (s.equals("OO")) {
			if (m.isWhiteMove()) {
				return "e1g1";
			}
			else return "e8g8";
		}
		else if (s.equals("OOO")) {
			if (m.isWhiteMove()) {
				return "e1c1";
			}
			else return "e8c8";
		}
		if (pieces.contains(s.substring(0,1))) {
			s = s.substring(1);
			if (s.length() == 3) {
				s = toString(m.getColFrom()) + s;
			}
		}
		return s;
	}
	
	private void write(GameRecord gamerec) {

		try {
			String rec = gamerec.toString(GameRecord.Color.WHITE);
			writer.write(rec,0,rec.length());
			rec = gamerec.toString(GameRecord.Color.BLACK);
			writer.write(rec,0,rec.length());
			writer.flush();
		}
		catch (IOException ioex) { ioex.printStackTrace(); }
	}
	
	private String toString(int col) {
		return columns[col];
	}
	
	private static String dumpBytes(String s) throws UnsupportedEncodingException
	{
		byte[] a = s.getBytes("UTF-8");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < a.length; i++)
		{
			sb.append(toHexString(a[i]));
		}
		return new String(sb);
	}
	
	private static String toHexString(byte b)
 	{
		// Converts a signed byte into an unsigned byte,
		// and returns a hexadecimal string representation;
		// useful if you are examining memory locations
		// or other binary data. Doesn't omit leading zeros,
		// which can be a problem with the original
		// "toHexString" methods in java.lang.
		// Kjell Krona <krona@nada.kth.se>, 01/20 1999
		// Free to use under the Gnu Public License
		//
		int theByte;
		char hexArray[] =
			{'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		if (b < 0)
			theByte = (int)b + 256;
		else
			theByte = (int)b;
		int highOrder = theByte/16;
		int lowOrder = theByte - (highOrder * 16);
		return (String.valueOf(hexArray[highOrder]) +
			String.valueOf(hexArray[lowOrder]));
 	}
}