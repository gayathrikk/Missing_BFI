package com.test.Database_Testing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Missing_BFI {

    private Connection conn;
    private static final String URL = "jdbc:mysql://apollo2.humanbrain.in:3306/HBA_V2";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Health#123";

    @BeforeClass
    public void setup() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Database connection established.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Database connection failed.");
        }
    }

    @Test
    public void displayMissingPositionIndexes() {
        String biosampleid = System.getProperty("biosampleid");

        if (biosampleid == null || biosampleid.trim().isEmpty()) {
            throw new RuntimeException("biosampleid system property is missing or empty.");
        }

        System.out.println("Running for biosampleid: " + biosampleid);

        Set<Integer> retrievedIndexes = new HashSet<>();
        int endIndex = 0;
        int missingCount = 0;

        try {
            // Get max positionindex
            String maxQuery = "SELECT MAX(positionindex) AS maxIndex FROM section WHERE jp2Path LIKE ?";
            try (PreparedStatement stmt = conn.prepareStatement(maxQuery)) {
                stmt.setString(1, "%/" + biosampleid + "/BFI/%");
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        endIndex = rs.getInt("maxIndex");
                    }
                }
            }

            // Get all existing indexes
            String query = "SELECT positionindex FROM section WHERE jp2Path LIKE ? ORDER BY positionindex ASC";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, "%/" + biosampleid + "/BFI/%");
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int posIndex = rs.getInt("positionindex");
                        retrievedIndexes.add(posIndex);
                    }
                }
            }

            // Print missing indexes
            System.out.println("Missing position indexes:");
            for (int i = 1; i <= endIndex; i++) {
                if (!retrievedIndexes.contains(i)) {
                    System.out.println(i);
                    missingCount++;
                }
            }

            System.out.println("\nTotal missing position indexes: " + missingCount);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error executing query.");
        }
    }

    @AfterClass
    public void tearDown() {
        try {
            if (conn != null) conn.close();
            System.out.println("Database connection closed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
