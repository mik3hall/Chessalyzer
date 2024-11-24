package us.hall.chess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chesspresso.game.Game;

public class GameRecord {

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
	 
	public enum Color {
		WHITE,
		BLACK
	}

	private static final String NL = System.getProperty("line.separator","\n");
	private static final String arffPrefix = "@RELATION GameResult" + NL +
	    "" + NL +
	    "@ATTRIBUTE color         {White,Black}"  + NL + 
	    "@ATTRIBUTE whitePlayer   STRING"  + NL +
	    "@ATTRIBUTE whiteELO      NUMERIC" + NL +
	    "@ATTRIBUTE blackELO      NUMERIC" + NL +
	    "@ATTRIBUTE best          NUMERIC" + NL +
	    "@ATTRIBUTE second_best   NUMERIC" + NL +
	    "@ATTRIBUTE third_best    NUMERIC" + NL + 
	    "@ATTRIBUTE total_moves   NUMERIC" + NL +
	    "@ATTRIBUTE result        NUMERIC" + NL +
	    "@ATTRUBUTE class         {Computer,Human}" + NL +
	    "" + NL +
	    "@DATA" + NL;
	
	private final OutType defaultOutType = OutType.CSV;
	private OutType outType = defaultOutType;
	 
	private static final String csvPrefix = "color,player,whiteELO,blackELO," + 
		"best,second_best,third_best,total_moves,result,class" + NL;

	private static final String WHITE = "white,";
	private static final String BLACK = "black,";
	private static final String LS = System.getProperty("line.separator","\n");
	private final StringBuilder record = new StringBuilder(); 
	private final StringBuilder whiteData = new StringBuilder();
	private final StringBuilder blackData = new StringBuilder();
	
/* From Game
	public String getEvent()       {return m_header.getEvent();}
    public String getSite()        {return m_header.getSite();}
    public String getDate()        {return m_header.getDate();}
    public String getRound()       {return m_header.getRound();}
    public String getWhite()       {return m_header.getWhite();}
    public String getBlack()       {return m_header.getBlack();}
    public String getResultStr()   {return m_header.getResultStr();}
    public String getWhiteEloStr() {return m_header.getWhiteEloStr();}
    public String getBlackEloStr() {return m_header.getBlackEloStr();}
    public String getEventDate()   {return m_header.getEventDate();}
    public String getECO()         {return m_header.getECO();}
    
    public int getResult()         {return m_header.getResult();}
    public int getWhiteElo()       {return m_header.getWhiteElo();}
    public int getBlackElo()       {return m_header.getBlackElo();}
*/	  
	public GameRecord(MoveSet moveSet, Game game, OutType outType) {	
		
		// Add Game info
		record.append(game.getWhite()).append(",");
		record.append(game.getWhiteEloStr()).append(",");
		record.append(game.getBlack()).append(",");
		record.append(game.getBlackEloStr()).append(",");
		// Add MoveSet info
		whiteData.append(moveSet.getBestsCount(Color.WHITE)).append(",");
		whiteData.append(moveSet.get2ndBestsCount(Color.WHITE)).append(",");
		whiteData.append(moveSet.get3rdBestsCount(Color.WHITE)).append(",");
		
	}
	
	public static final String getArffPrefix() {
		return arffPrefix;
	}
	
	public static final String getCsvPrefix() {
		return csvPrefix;
	}
	
	public String toString(Color color) {
		if (color.equals(Color.WHITE)) {
			return toStringWhite();
		}	
		else if (color.equals(Color.BLACK)) {
			return toStringBlack();
		}
		throw new IllegalArgumentException("Unknown string type " + color);
	}
	
	public void setOutType(OutType outType) {
		this.outType = outType;
	}
	
	private String toStringWhite() {
		record.insert(0,WHITE);
		record.append(whiteData);
		record.append(LS);
		return record.toString();
	}  
	
	public String toStringBlack() {
		record.insert(0,BLACK);
		record.append(blackData);
		record.append(LS);
		return record.toString();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.toString();
	}   
}