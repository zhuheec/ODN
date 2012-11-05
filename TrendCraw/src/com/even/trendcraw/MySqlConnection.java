// or done

package com.even.trendcraw;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.zh.odn.trace.ObjectRelation;


public class MySqlConnection {
	
	private String url = "jdbc:mysql://localhost:3306/trend_db";
	private String user = "root";
	private String pw = "zhoufeng";
	
	private Connection dbConn;
	
	public MySqlConnection() {
		// setup the properties 
		java.util.Properties prop = new java.util.Properties();
		prop.put("characterEncoding", "utf8");
		prop.put("user", user);
		prop.put("password" , pw);
		ObjectRelation.addRelation(prop, user, pw);
		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection(url, prop);
			ObjectRelation.addRelation(dbConn, url, prop);
			if(!dbConn.isClosed()){
				ObjectRelation.addRelation(this, dbConn);
                System.out.println("Successfully connected to database.");
            }
		} catch (SQLException e) {
			System.err.println("ERROR: Connect to database exception: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("ERROR: Class not found: " + e.getMessage());
		}
	}
	
	public void getTrendNames() {
		try {
			Statement stmt = dbConn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT t_name FROM trend_db.trend");
			while(rs.next()) {
				System.out.println(rs.getString(1));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void insertGoogleTrend(GoogleTrend trend, int rangeID, int countryID) {
		try {
			PreparedStatement stmt = dbConn.prepareStatement("INSERT INTO trend_db.trend(s_id, r_id, c_id, t_name, rank_now, " +
					"rank_prev, score, load_page, snippet_src, snippet_content, update_time, collect_time) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ObjectRelation.addRelation(stmt, dbConn);
			int idx = 1;
			stmt.setInt(idx++, 2); // source id - 2 is Google
			stmt.setInt(idx++, rangeID); // range id
			stmt.setInt(idx++, countryID); // country id
			stmt.setString(idx++, trend.getQuery()); // trend name
			stmt.setInt(idx++, trend.getRankNow()); // rank now
			stmt.setInt(idx++, trend.getRankPrev()); // rank prev
			stmt.setFloat(idx++, trend.getScore()); // score
			stmt.setString(idx++, trend.getLoadingPage()); // load page
			stmt.setString(idx++, trend.getSnippetSrc()); // snippet src
			stmt.setString(idx++, trend.getSnippetContent()); // snippet content
			stmt.setTimestamp(idx++, trend.getSqlTimestamp()); // update time
			stmt.setTimestamp(idx++, new Timestamp(new java.util.Date().getTime())); // collect time
			ObjectRelation.addRelation(stmt, trend, rangeID, countryID);
			if (stmt.executeUpdate() > 0) {
				Log.d("Insert trend [" + trend + "] successfully.");
			} else {
				Log.e("Insert trend [" + trend + "] failed.");
			}
			stmt.close();
		} catch (SQLException e) {
			Log.e("Insert google trend exception: " + e.getMessage());
		}
		
	}
	
	public void close() {
		try {
            if(dbConn != null) {
            	dbConn.close();
            	System.out.println("DEBUG: Database connection closed.");
            }
        }catch(SQLException e) {
        	System.err.println("ERROR: Close database connection exception: " + e.getMessage());
        }
	}
	
	public static void main(String[] Args) throws Throwable {
		MySqlConnection conn = new MySqlConnection();
		conn.getTrendNames();
		conn.close();
	}
}
