package us.hall.chess;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;
import java.util.Scanner;
import static us.hall.chess.GameRecord.*;

public class Chessalyzer {

	private static StockfishProcess sfp;
	private static BufferedWriter writer = null;
	private static final Charset cs = Charset.forName("US-ASCII");	
	
	public static void main(String... args) {
		Runtime.getRuntime().addShutdownHook(new Completion());
		int idx = 0;
		int idxmax = args.length - 1;   // - 1 for pgn dataset parameter
				
		if (args.length == 0) {
			System.out.println("Chessalyzer: missing pgn file");
			sfp = null;
			return;
		}
		else if (args[idx].equals("-o")) {		// Write results to arff/csv file
			OutType outType = null;
			try {
				if (idxmax > idx) {
					String outputFile = null;
					if (args[idx+1].endsWith(".arff") || args[idx+1].endsWith(".csv")) {
						outputFile = args[idx+1];
						idx++;
					}
					else {
						System.out.println("output file extension must be arff or csv");
						return;
					}
					Path outPath = Paths.get(outputFile);
					if (Files.exists(outPath)) {
						Scanner fileOpt = new Scanner(System.in);
						System.out.println(outputFile + " already exists. Overwrite? (y)es/(n)o/(a)ppend");
						String choice = fileOpt.nextLine().toLowerCase();
						if (choice.equals("y")) {
							writer = Files.newBufferedWriter(outPath,cs,CREATE,TRUNCATE_EXISTING,WRITE);
							if (outputFile.endsWith(".arff")) {
								outType = OutType.ARFF;
								String arffPrefix = GameRecord.getArffPrefix();
								writer.write(arffPrefix,0,arffPrefix.length());
							}
							else if (outputFile.endsWith(".csv")) {
								outType = OutType.CSV;
								String csvPrefix = GameRecord.getCsvPrefix();
								writer.write(csvPrefix,0,csvPrefix.length());
							}
							else {
								System.out.println("Chessalyzer - output file must be .arff or .csv");
								return;
							}
						}
						else if (choice.equals("a")) {
							writer = Files.newBufferedWriter(outPath,cs,CREATE,APPEND,WRITE);
						}
						else { 
							return; 
						}
					}
					else {
						writer = Files.newBufferedWriter(outPath,CREATE,WRITE);
						if (outputFile.endsWith(".arff")) {
							String arffPrefix = GameRecord.getArffPrefix();
							writer.write(arffPrefix,0,arffPrefix.length());
						}
						else {
							String csvPrefix = GameRecord.getCsvPrefix();								
							writer.write(csvPrefix,0,csvPrefix.length());
						}
					}
				}
				else {
					System.out.println("Missing output file parameter");
				}
			}
			catch (IOException ioex) { 
				ioex.printStackTrace();
				return;
			}
			sfp = new StockfishProcess(writer,outType);
			sfp.evalPGN(args[idxmax]);
		}
	}
	
	static class Completion extends Thread {
  		
  		public void run() {
  		    if (writer != null) {
  		    	try {
  		    		writer.flush();
  		    		writer.close();
  		    	}
  		    	catch (IOException ioex) { ioex.printStackTrace(); }
  		    }
  		}
  	}
}