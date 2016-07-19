import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import jxl.read.biff.BiffException;
import jxl.Cell;
import jxl.CellType;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;

import java.util.*;
import java.io.File;
import java.io.IOException;

import com.sun.corba.se.impl.util.Version;

public class ExcelToSQL {
	private static final String STATS_INPUT_FILE = "/Users/Jacob/Documents/Repositories/TwitterLeadGen/Excel/Stats.xls";
	
	private Sheet sheet;
	private Workbook w;
	private DBManager manager;
	
	public ExcelToSQL() {
		manager = DBManager.getInstance();
		File inputWorkbook = new File(STATS_INPUT_FILE);
		try {
			w = Workbook.getWorkbook(inputWorkbook);
			sheet = w.getSheet(0);
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void convert() {
		int relTweets, 		irrTweets, 		totTweets;
		int relAts, 		irrAts,    		totAts;
		int relLinks,		irrLinks, 		totLinks;
		int relHashtags, 	irrHashtags, 	totHashtags;
		

		relTweets = (int)((NumberCell)sheet.getCell(1,1)).getValue();
		irrTweets = (int)((NumberCell)sheet.getCell(2,1)).getValue();
		totTweets = (int)((NumberCell)sheet.getCell(3,1)).getValue();
		

		relLinks = (int)((NumberCell)sheet.getCell(1,2)).getValue();
		irrLinks = (int)((NumberCell)sheet.getCell(2,2)).getValue();
		totLinks = (int)((NumberCell)sheet.getCell(3,2)).getValue();

		relHashtags = (int)((NumberCell)sheet.getCell(1,3)).getValue();
		irrHashtags = (int)((NumberCell)sheet.getCell(2,3)).getValue();
		totHashtags = (int)((NumberCell)sheet.getCell(3,3)).getValue();

		relAts = (int)((NumberCell)sheet.getCell(1,4)).getValue();
		irrAts = (int)((NumberCell)sheet.getCell(2,4)).getValue();
		totAts = (int)((NumberCell)sheet.getCell(3,4)).getValue();
		
		manager.addWord("00Tweets",relTweets,irrTweets,totTweets);
		manager.addWord("0Links",relLinks,irrLinks,totLinks);
		manager.addWord("0Hashtags",relHashtags,irrHashtags,totHashtags);
		manager.addWord("0Ats",relAts,irrAts,totAts);
		
		int totalWords = (int)((NumberCell)sheet.getCell(3,8)).getValue();
		
		for (int row = 10; row < totalWords+10; row++) {
			String word = sheet.getCell(0,row).getContents();
			int rel = (int)((NumberCell)sheet.getCell(1,row)).getValue();
			int irr = (int)((NumberCell)sheet.getCell(2,row)).getValue();
			int tot = (int)((NumberCell)sheet.getCell(3,row)).getValue();
			
			manager.addWord(word,rel,irr,tot);
		}
	}
	
	public static void main(String [] args) {
		ExcelToSQL inst = new ExcelToSQL();
		inst.convert();
	}
}














