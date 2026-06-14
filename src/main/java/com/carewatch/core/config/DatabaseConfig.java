package com.carewatch.core.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Database config & connection handler
public class DatabaseConfig {

    // Neon PostgreSQL Credentials
    private static final String URL = "jdbc:postgresql://ep-misty-feather-airr2v8t-pooler.c-4.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require";
    private static final String USER = "neondb_owner";
    private static final String PASSWORD = "npg_gDti6zyenIJ8";

    static {
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found in classpath!");
            e.printStackTrace();
        }
    }

    // Connect to database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
