package com.hospital.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NurseDAO {
    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@"; // замени

    // 1. Мои поручения (не выполненные)
    public List<String> getTasksForNurse(int nurseId) {
        List<String> tasks = new ArrayList<>();
        String sql = """
            SELECT id, task_description, assigned_date
            FROM nurse_tasks
            WHERE nurse_id = ? AND completed = FALSE
            ORDER BY assigned_date
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nurseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(String.format("ID:%d – %s (назначено: %s)",
                        rs.getInt("id"),
                        rs.getString("task_description"),
                        rs.getTimestamp("assigned_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    // 2. Выполнить поручение
    public void completeTask(int taskId) {
        String sql = """
            UPDATE nurse_tasks
            SET completed = TRUE, completed_date = NOW()
            WHERE id = ?
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("✔ Поручение выполнено.");
            } else {
                System.out.println("❌ Поручение не найдено.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. Завершённые поручения
    public List<String> getCompletedTasksForNurse(int nurseId) {
        List<String> tasks = new ArrayList<>();
        String sql = """
            SELECT id, task_description, completed_date
            FROM nurse_tasks
            WHERE nurse_id = ? AND completed = TRUE
            ORDER BY completed_date DESC
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nurseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(String.format("ID:%d – %s (выполнено: %s)",
                        rs.getInt("id"),
                        rs.getString("task_description"),
                        rs.getTimestamp("completed_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    // 4. Поиск пациента по имени
    public List<String> findPatientsByName(String namePart) {
        List<String> patients = new ArrayList<>();
        String sql = """
            SELECT full_name, birth_date, blood_group, rhesus
            FROM patients
            WHERE full_name ILIKE ?
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + namePart + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                patients.add(String.format("%s — %s г.р., группа: %s%s",
                        rs.getString("full_name"),
                        rs.getDate("birth_date"),
                        rs.getString("blood_group"),
                        rs.getString("rhesus")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return patients;
    }
}
