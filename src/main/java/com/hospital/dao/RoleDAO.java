package com.hospital.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO {

    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@";

    public void addRole(String name) {
        String sql = "INSERT INTO roles(name) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.executeUpdate();
            System.out.println("✔ Роль добавлена: " + name);
        } catch (SQLException e) {
            System.err.println("Ошибка добавления роли:");
        }
    }

    public List<String> getAllRoles() {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT name FROM roles";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                roles.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения ролей:");
        }

        return roles;
    }
}
