package com.hospital.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@"; // замените на ваш пароль

    /**
     * Создаёт пользователя и возвращает его ID
     */
    public int createUser(String username, String password, String roleName) {
        String getRoleIdSql = "SELECT id FROM roles WHERE name = ?";
        String insertUserSql = "INSERT INTO users(username, password, role_id) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);
            int roleId;
            try (PreparedStatement roleStmt = conn.prepareStatement(getRoleIdSql)) {
                roleStmt.setString(1, roleName);
                try (ResultSet rs = roleStmt.executeQuery()) {
                    if (rs.next()) {
                        roleId = rs.getInt("id");
                    } else {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setInt(3, roleId);
                insertStmt.executeUpdate();
                try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        conn.commit();
                        System.out.println("✔ Пользователь добавлен: " + username + " (ID=" + newId + ")");
                        return newId;
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            // Если уникальность нарушена (код 23505), просто возвращаем -1, без вывода стека
            if ("23505".equals(e.getSQLState())) {
                return -1;
            }
            System.err.println("Ошибка при добавлении пользователя:");
        }
        return -1;
    }

    /**
     * Возвращает список всех пользователей с их ролями
     */
    public List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        String sql = "SELECT u.username, r.name AS role FROM users u JOIN roles r ON u.role_id = r.id";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(rs.getString("username") + " (" + rs.getString("role") + ")");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении пользователей:");
        }
        return users;
    }
    public int getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, username);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении user_id:");
        }
        return -1;
    }
}
