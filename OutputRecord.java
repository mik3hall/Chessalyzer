package us.hall.chess;

public class OutputRecord {

	public enum OutType {
		/**
		 * Weka ARFF
		 */
		ARFF,
		/**
		 * CSV comma separated values
		 */
		CSV
	}
	 
	private static final String NL = System.getProperty("line.separator","\n");

	private static final String arffPrefix = "@RELATION DigitalTurk" + NL +
	    "" + NL +
	    "@ATTRIBUTE color         {White,Black}" + NL +
	    "@ATTRIBUTE player        string" + NL +
	    "@ATTRIBUTE move_best     NUMERIC" + NL +
	    "@ATTRIBUTE move_second   NUMERIC" + NL +
	    "@ATTRIBUTE move_third    NUMERIC" + NL + 
	    "@ATTRIBUTE total_moves   NUMERIC" + NL +
	    "@ATTRIBUTE elo           NUMERIC" + NL +
	    "@ATTRIBUTE class         {Human,Computer}"
	    "" + NL +
	    "@DATA" + NL;
	
	private static final String csvPrefix = "color,player,move_best,move_second,move_third," +
	    "total_moves,elo,class" + NL;
	 
	private final MoveSet ms;
	private final Game game;
	  
	public OutputRecord(MoveSet ms, Game game) {
		this.ms = ms;
		this.game = game;
	}
	    
	public static final String getArffPrefix() {
		return arffPrefix;
	}
	
	public static final String getCsvPrefix() {
		return csvPrefix;
	}
	 
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.toString();
	}   
}