package com.hospital.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@"; // замени

    // Добавление пользователя по имени роли
    public void createUser(String username, String password, String roleName) {
        String getRoleIdSql = "SELECT id FROM roles WHERE name = ?";
        String insertUserSql = "INSERT INTO users(username, password, role_id) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            // Получаем role_id по имени
            int roleId = -1;
            try (PreparedStatement roleStmt = conn.prepareStatement(getRoleIdSql)) {
                roleStmt.setString(1, roleName);
                try (ResultSet rs = roleStmt.executeQuery()) {
                    if (rs.next()) {
                        roleId = rs.getInt("id");
                    } else {
                        System.err.println("❌ Роль не найдена: " + roleName);
                        return;
                    }
                }
            }

            // Вставляем пользователя
            try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setInt(3, roleId);
                insertStmt.executeUpdate();
                System.out.println("✔ Пользователь добавлен: " + username);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении пользователя:");
            e.printStackTrace();
        }
    }

    // Получение всех пользователей (с ролями)
    public List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        String sql = """
            SELECT u.username, r.name AS role
            FROM users u
            JOIN roles r ON u.role_id = r.id
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String username = rs.getString("username");
                String role = rs.getString("role");
                users.add(username + " (" + role + ")");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении пользователей:");
            e.printStackTrace();
        }

        return users;
    }
}

