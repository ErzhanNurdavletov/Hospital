package com.hospital;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // URL: jdbc:postgresql://<host>:<port>/<database>
    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        System.out.println("Попытка подключения к БД...");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✔ Успешно подключились к hospital_db!");
            } else {
                System.out.println("✘ Не удалось подключиться.");
            }
        } catch (SQLException ex) {
            System.err.println("Ошибка при подключении:");
        }
    }
}

