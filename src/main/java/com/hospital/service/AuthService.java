package com.hospital.service;

import java.sql.*;

public class AuthService {

    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@";

    public static String login(String username, String password) {
        String sql = """
            SELECT r.name AS role
            FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.username = ? AND u.password = ?
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role"); // patient / doctor / maindoctor / medassistant
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при входе:");
            return null;
        }
    }
}

