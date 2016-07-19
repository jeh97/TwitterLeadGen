//import jxl.*;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;
import jxl.read.biff.BiffException;
import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Number;

import java.util.*;
import java.io.File;
import java.io.IOException;

public class FilterStatsRecorder {
	private static final String STATS_INPUT_FILE = "/Users/Jacob/Documents/Repositories/TwitterLeadGen/Excel/Past stats.xls";
	private static final String STATS_OUTPUT_FILE = "/Users/Jacob/Documents/Repositories/TwitterLeadGen/Excel/Stats.xls";
	private static final String EXCEL_FILE_FOLDER = "/Users/Jacob/Documents/Repositories/TwitterLeadGen/Excel/Reviewed files/";
	/**
	 * Opens an existing excel workbook and returns the Workbook version of it
	 * @param filename name of the file to read. It is assumed it is in 
	 * @return Workbook object containing the workbook
	 * @throws IOException
	 */
	public static Workbook ReadExcel(String filename) throws IOException {
		File inputWorkbook = new File(EXCEL_FILE_FOLDER+filename);
		Workbook w;
		try {
			w = Workbook.getWorkbook(inputWorkbook);
			return w;
		} catch (BiffException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	public static void recordStatsForKeywordSearch(String query) throws IOException {
		Workbook wb = ReadExcel(query+".xls");
		Sheet sheet = wb.getSheet(0);
		
		File inputWorkbook = new File(STATS_INPUT_FILE);
		Workbook statsWBRO = null;
		try {
			statsWBRO = Workbook.getWorkbook(inputWorkbook);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		WritableWorkbook statsWB = 
				Workbook.createWorkbook(new File(
						STATS_OUTPUT_FILE),statsWBRO);
		
		WritableSheet statsSheet = statsWB.getSheet(0);
		
		Stemmer stemmer = new Stemmer();
		

		int relTweets = (int)((Number)(statsSheet.getCell(1,1))).getValue();
		int irrTweets = (int)((Number)(statsSheet.getCell(2,1))).getValue();
		int totalTweets = (int)((Number)(statsSheet.getCell(3,1))).getValue();
		
		int relLinks = (int)((Number)(statsSheet.getCell(1,2))).getValue();
		int irrLinks = (int)((Number)(statsSheet.getCell(2,2))).getValue();
		int totalLinks = (int)((Number)(statsSheet.getCell(3,2))).getValue();
		
		int relHashtags = (int)((Number)(statsSheet.getCell(1,3))).getValue();
		int irrHashtags = (int)((Number)(statsSheet.getCell(2,3))).getValue();
		int totalHashtags = (int)((Number)(statsSheet.getCell(3,3))).getValue();
		
		int relAts = (int)((Number)(statsSheet.getCell(1,4))).getValue();
		int irrAts = (int)((Number)(statsSheet.getCell(2,4))).getValue();
		int totalAts = (int)((Number)(statsSheet.getCell(3,4))).getValue();
		
		Map<String,WordObject> words = new TreeMap<String,WordObject>();
		
		for (int row = 10; statsSheet.getCell(0,row).getType() == CellType.LABEL; row++) {
			String word = statsSheet.getCell(0,row).getContents();
			int rel = (int)((Number)statsSheet.getCell(1,row)).getValue();
			int irr = (int)((Number)statsSheet.getCell(2,row)).getValue();
			int tot = (int)((Number)statsSheet.getCell(3,row)).getValue();
			
			WordObject newWord = new WordObject(word,rel,irr,tot);
			words.put(word, newWord);
		}
		
		int count = 1;
		boolean hasMore = true;
		while (hasMore) {
			System.out.printf("Looking at tweet #%d\n",count);
			boolean isRelevant = false;
			boolean hasLink = false;
			boolean hasAt = false;
			boolean hasHash = false;
			
			Cell relevance = sheet.getCell(0,count);
			isRelevant = relevance.getType() != CellType.EMPTY;

			
			
			String contents = sheet.getCell(4,count).getContents().toLowerCase();
			String[] splitStrings = contents.split(" ");
			int len = splitStrings.length;
			
			Map<String,WordObject> wordsInText = new TreeMap<String,WordObject>();
			
			for (int c = 0; c < len; c++) {
				String current = splitStrings[c];
				if (current.indexOf("http") != -1) {
					hasLink = true;
				} else if (current.indexOf('@') != -1) {
					hasAt = true;
				} else if (current.indexOf('#') != -1) {
					hasHash = true;
				} else {
					current = current.replaceAll("[^A-Za-z]+","");
					//System.out.printf("current was    %s\n", current);
					current = stemmer.StemWordWithWordNet(current);
					//System.out.printf("current is now %s\n", current);
					if (current != null && current.length() > 0) {
						WordObject curObj = null;
						try {
							curObj = wordsInText.get(current);
							if (curObj == null) {
								curObj = new WordObject(current);
								
								wordsInText.put(current, curObj);
							}
						} catch (Exception e){
							e.printStackTrace();
							System.exit(1);
						}
					}
				}
					
			}
			

			if (isRelevant) {
				relTweets++;
				if (hasLink) {relLinks++;totalLinks++;}
				if (hasAt) {relAts++;totalAts++;}
				if (hasHash) {relHashtags++;totalHashtags++;}
			} else {
				irrTweets++;
				if (hasLink) {irrLinks++;totalLinks++;}
				if (hasAt) {irrAts++;totalAts++;}
				if (hasHash) {irrHashtags++;totalHashtags++;}
			}
			totalTweets++;
			
			WordObject thisWord;
			for (Map.Entry<String,WordObject> entry : wordsInText.entrySet()) {
				String key = entry.getKey();
				WordObject value = entry.getValue();
				
				thisWord = words.get(key);
				int relev = 0;
				if (isRelevant) {
					relev = 1;
				}
				if (thisWord == null) {
					words.put(key,new WordObject(key,relev,1-relev,1));
				} else if (isRelevant){
					thisWord.addRel();
				} else {
					thisWord.addIrr();
				}
			}
			count++;
			try {
				hasMore = (sheet.getCell(1,count).getType() != CellType.EMPTY);
			} catch (Exception e) {
				hasMore = false;
			}
		}
		
		//Place data back in datasheet
		
		((Number)statsSheet.getCell(1,1)).setValue(relTweets);
		((Number)statsSheet.getCell(2,1)).setValue(irrTweets);
		((Number)statsSheet.getCell(3,1)).setValue(totalTweets);
		((Number)statsSheet.getCell(1,2)).setValue(relLinks);
		((Number)statsSheet.getCell(2,2)).setValue(irrLinks);
		((Number)statsSheet.getCell(3,2)).setValue(totalLinks);
		((Number)statsSheet.getCell(1,3)).setValue(relHashtags);
		((Number)statsSheet.getCell(2,3)).setValue(irrHashtags);
		((Number)statsSheet.getCell(3,3)).setValue(totalHashtags);
		((Number)statsSheet.getCell(1,4)).setValue(relAts);
		((Number)statsSheet.getCell(2,4)).setValue(irrAts);
		((Number)statsSheet.getCell(3,4)).setValue(totalAts);
		
		int row = 10;
		for (Map.Entry<String,WordObject> entry : words.entrySet()) {
			try {
				System.out.println("Putting the values into the stats sheet");
				String key = entry.getKey();
				WordObject word = entry.getValue();
				Label thisWordName = new Label(0,row,key);
				Number rel = new Number(1,row,word.getRel());
				Number irr = new Number(2,row,word.getIrr());
				Number tot = new Number(3,row,word.getTotal());
				statsSheet.addCell(thisWordName);
				statsSheet.addCell(rel);
				statsSheet.addCell(irr);
				statsSheet.addCell(tot);
			} catch (Exception e) {
				e.printStackTrace();
			}
			row++;
			
		}
		Number numWords = new Number(3,8,row-10);
		try {
			statsSheet.addCell(numWords);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			System.out.println("Saving the workbook");
			statsWB.write();
			statsWB.close();
		} catch (Exception e) {
			System.out.println("****Problem saving the workbook****");
			e.printStackTrace();
		}
	}
	
	
	public static void main(String [] args) throws IOException {
		recordStatsForKeywordSearch("new game android working");
		
	}
}
