package us.hall.chess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoveSet {
	GameRecord.Color color;
	int moveNum;
	boolean isWhite = false, bookMove = false;
	String bestmove = null;
	int gameScore = 0;
	int lastMoveScore = 0;
	ArrayList<Integer> whiteScores = new ArrayList<Integer>();
	ArrayList<Integer> blackScores = new ArrayList<Integer>();
	int[] bestsWhite = new int[4];
	int[] bestsBlack = new int[4];
	private final String[] continuations; 
	final String[] centipawns = new String[3];
	private final LinkedList<Integer> scores = new LinkedList<Integer>();
	
	private final String regex = "^.*multipv (\\d{1}).*cp (-?\\d+).*pv ([a-z0-9]{4}?).*$"; 
	
	public MoveSet() {
		this.moveNum = moveNum;
		continuations = new String[] { "X", "X", "X" };	
	}
	
	/*
	 * Reset evaluations if continuation not selected
	 */
	public void resetEvals() {
		continuations[0] = "X";
		continuations[1] = "X";
		continuations[2] = "X";
	}
	
	public void addEval(String line) {
		//int idx = line.indexOf("multipv ")+8;
		
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {                                                
				int contIdx = Integer.valueOf(matcher.group(1)).intValue()-1;
				centipawns[contIdx] = matcher.group(2);
				continuations[contIdx] = matcher.group(3);
			}
			else {
				System.out.println("No match");
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			StockfishProcess.sysout.println(line);
			System.exit(0);
		}
		/*
		for (int i=0; i<3; i++) {
			System.out.println("AE " + continuations[i]);
		}
		*/
	}
	
	public synchronized void setBestMove(String bestmove, boolean isWhite) {
		//throw new IllegalStateException("bestmove " + bestmove + " is white " + isWhite);
		if (!continuations[0].equals("X") && !bestmove.equals(continuations[0])) {
			//StockfishProcess.dumpLines();
			throw new IllegalStateException("MISMATCH best move " + bestmove + " continuation " + continuations[0]);
		}
		this.bestmove = bestmove;
	}
	
	public synchronized boolean addMove(String move, boolean isWhite) {
		int i = 0;
		//System.out.println("addMove " + move);
		for (; i<3; i++) {
			if (continuations[i].equals(move)) {
				if (isWhite) {
					bestsWhite[i]++;
				}
				else {
					bestsBlack[i]++;
				}
				updateScores(i, move, isWhite);
				return true;
			}
		}
		if (isWhite) {
			bestsWhite[3]++;     // Didn't pick a top 3 continuation
			System.out.println("White did not select top 3 move");			
		}
		else {
			bestsBlack[3]++;
			System.out.println("Black did not select top 3 move");
		}	
		return false;	
	}
	
	public void updateScores(int continuation, String move, boolean isWhite) {
		int gameScore = Integer.valueOf(centipawns[continuation]);
		int moveScore = gameScore - lastMoveScore;
		lastMoveScore = gameScore;
		if (isWhite) {
			System.out.println("White move " + move + " score - Move: " + moveScore + " game score = " + gameScore);
		}
		else {
			System.out.println("Black move " + move + " score - Move: " + moveScore + " game score = " + gameScore);
		}
	}
	
	public void setWhiteMove(boolean isWhite) {
		this.isWhite = isWhite;
	}
	
	public void printContinuations() {
		System.out.println("1. " + continuations[0] + " " + centipawns[0]);
		System.out.println("2. " + continuations[1] + " " + centipawns[1]);
		System.out.println("3. " + continuations[2] + " " + centipawns[2]);		
	}
	
	public void setBookMove(boolean bookMove) {
		this.bookMove = bookMove;
	}
	
	public String getBestsCount(GameRecord.Color color) {
		if (color == GameRecord.Color.WHITE) {
			return Integer.toString(bestsWhite[0]);
		}
		return Integer.toString(bestsBlack[0]);
	}
	
	public String get2ndBestsCount(GameRecord.Color color) {
		if (color == GameRecord.Color.WHITE) {
			return Integer.toString(bestsWhite[1]);
		}
		return Integer.toString(bestsBlack[1]);
	}
	
	public String get3rdBestsCount(GameRecord.Color color) {
		if (color == GameRecord.Color.WHITE) {
			return Integer.toString(bestsWhite[2]);
		}
		return Integer.toString(bestsBlack[2]);
	}
	
	@Override
	public int hashCode() {
		return moveNum;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MoveSet)) {
			return false;
		} 		
		return (((MoveSet)o).moveNum == moveNum);
	} 
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MoveSet: ").append(moveNum).append(" ").append(super.toString());
		return sb.toString();
	}
}