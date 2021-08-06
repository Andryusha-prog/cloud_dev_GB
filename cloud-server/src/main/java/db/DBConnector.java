package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    public static Connection getConnection(){
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/usersclouddb?useUnicode=true&serverTimezone=UTC", "root", "12345");
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }
}