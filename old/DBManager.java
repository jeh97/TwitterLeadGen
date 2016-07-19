
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DBManager {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost:3306/TwitterLeadGen?autoReconnect=true&useSSL=false";
	
	static final String USER = "mysqladmin";
	static final String PASS = "defaultAccess";
	
	private static DBManager instance;
	
	private DBManager() {
		
	}
	public static DBManager getInstance() {
		if (instance == null) {
			instance = new DBManager();
		}
		return instance;
	}
	public void executeSql(String sql) {
		Connection conn = null;
		Statement stmt = null;
		
		try {
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			stmt.execute(sql);
		} catch (Exception e) { e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
		}
	}
	
	public void addWord(String word, int relTweets, int irrTweets, int totTweets) {
		String sql = "INSERT INTO twitter_word_stats "
				+ "(word, relevantCount, irrelevantCount, totalCount) "
				+ String.format("VALUES ('%s', %d, %d, %d)", word,relTweets,irrTweets,totTweets);
		executeSql(sql);
	}
	public void addWord(WordObject word) {
		this.addWord(word.getWord(),word.getRel(),word.getIrr(),word.getTotal());
	}
	
	public WordObject getInfoForWord(String word) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		try {
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			String sql = String.format("SELECT * FROM twitter_word_stats WHERE word=%s", word);
			set = stmt.executeQuery(sql);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {

			try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				set.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		return null;
	}
	public void addRelevantInstanceOfWord(String word) {
		String sql = String.format("UPDATE twitter_word_stats SET relevantCount = relevantCount+1, totalCount = totalCount + 1 WHEN word = %s", word);
		executeSql(sql);
	}

	public void addIrrelevantInstanceOfWord(String word) {
		String sql = String.format("UPDATE twitter_word_stats SET irrelevantCount = irrelevantCount+1, totalCount = totalCount + 1 WHEN word = %s", word);
		executeSql(sql);
	}
	
	
	
}

