package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:weather_app.db";
    
    public DatabaseHandler() {
        createTableIfNotExists();
    }
    
    private void createTableIfNotExists() {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            connection = DriverManager.getConnection(DB_URL);
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS search_history " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "city_name TEXT NOT NULL, " +
                    "search_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        }
    }
    
    public void saveSearch(String cityName) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO search_history (city_name) VALUES (?)");
            pstmt.setString(1, cityName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving search: " + e.getMessage());
        }
    }
    
    public List<String> getRecentSearches(int limit) {
        List<String> searches = new ArrayList<>();
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT DISTINCT city_name FROM search_history " +
                    "ORDER BY search_time DESC LIMIT ?");
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                searches.add(rs.getString("city_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving searches: " + e.getMessage());
        }
        return searches;
    }
    
    public String getLastSearchedCity() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT city_name FROM search_history " +
                    "ORDER BY search_time DESC LIMIT 1");
            
            if (rs.next()) {
                return rs.getString("city_name");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving last search: " + e.getMessage());
        }
        return "London"; // Default city if no history exists
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}