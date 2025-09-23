/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bse_oop2_2025;

import java.sql.*;

/**
 *
 * @author jonah
 */
public class DbConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/market_vendor_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Jonah@1170";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
