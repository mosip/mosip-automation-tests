package org.mosip.dataprovider.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DBDataSource {

	 public static void main(String[] args) throws SQLException {
	   /* 	        
		 createAndFillDatabase();
	     Map<String, String> allData = getData();
	     String oneData = this.getOneName("A");
	        String noData = this.getOneName("D");
	        System.out.println("allData = " + allData);
	        System.out.println("oData   = " + oneData);
	        System.out.println("noData  = " + noData);*/
	    }

	    private Connection getConnection() throws SQLException {
	        return DriverManager.getConnection("jdbc:derby:resource/dn/test;create=true");
	    }

	    private void createAndFillDatabase() throws SQLException {
	        try (Connection conn = getConnection()) {
	            ResultSet tables = conn.getMetaData().getTables(null, null, "POSSIBLESQLTABLE", null);
	            if (!tables.next()) {
	                // create table
	                Statement stmt = conn.createStatement();
	                stmt.execute("CREATE TABLE possibleSqlTable(\n"
	                        + "  letter VARCHAR(1) NOT NULL PRIMARY KEY\n"
	                        + ", name VARCHAR(14)\n"
	                        + ")");
	                // insert data
	                String[][] data = {{"A", "alpha-static"}, {"B", "beta-static"}, {"C", "charlie-static"}};
	                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO possibleSqlTable(letter,name) VALUES (?,?)");
	                for (String[] row : data) {
	                    pstmt.setString(1, row[0]);
	                    pstmt.setString(2, row[1]);
	                    pstmt.execute();
	                }
	                conn.commit();
	            }
	        }
	    }

	    private Map<String, String> getData() throws SQLException {
	        Map<String, String> result = new HashMap<>();
	        try (Connection conn = getConnection()) {
	            Statement stmt = conn.createStatement();
	            ResultSet results = stmt.executeQuery("select * from possibleSqlTable");
	            while (results.next()) {
	                result.put(results.getString("letter"), results.getString("name"));
	            }
	        }
	        return result;
	    }

	    private String getOneName(String letter) throws SQLException {
	        String result = null;
	        try (Connection conn = getConnection()) {
	            PreparedStatement pstmt = conn.prepareStatement("select * from possibleSqlTable where letter = ?");
	            pstmt.setString(1, letter);
	            ResultSet results = pstmt.executeQuery();
	            while (results.next()) {
	                result = results.getString("name");
	            }
	        }
	        return result;
	    }
	
}
