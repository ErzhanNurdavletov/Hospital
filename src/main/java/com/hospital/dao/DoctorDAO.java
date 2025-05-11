package com.hospital.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {
    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@"; // замените

    // Список пациентов
    public List<String> getCurrentPatients() {
        List<String> patients = new ArrayList<>();
        String sql = "SELECT p.full_name, u.username FROM patients p JOIN users u ON p.user_id = u.id";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                patients.add(rs.getString("full_name") + " (" + rs.getString("username") + ")");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка пациентов:");
            e.printStackTrace();
        }
        return patients;
    }

    // Написать поручение медсестре
    public void addNurseTask(int nurseId, String taskDescription) {
        String sql = "INSERT INTO nurse_tasks(nurse_id, task_description) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nurseId);
            stmt.setString(2, taskDescription);
            stmt.executeUpdate();
            System.out.println("✔ Поручение добавлено медсестре ID=" + nurseId);
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении поручения:");
            e.printStackTrace();
        }
    }

    // Поручения не завершённые
    public List<String> getPendingNurseTasks() {
        List<String> tasks = new ArrayList<>();
        String sql = "SELECT id, nurse_id, task_description, assigned_date FROM nurse_tasks WHERE completed = FALSE";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add("ID:" + rs.getInt("id") + ", nurse_id:" + rs.getInt("nurse_id") + ", "
                        + rs.getString("task_description") + ", assigned:" + rs.getTimestamp("assigned_date"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении поручений:");
            e.printStackTrace();
        }
        return tasks;
    }

    // Поручения завершённые
    public List<String> getCompletedNurseTasks() {
        List<String> tasks = new ArrayList<>();
        String sql = "SELECT id, nurse_id, task_description, completed_date FROM nurse_tasks WHERE completed = TRUE";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add("ID:" + rs.getInt("id") + ", nurse_id:" + rs.getInt("nurse_id") + ", "
                        + rs.getString("task_description") + ", done:" + rs.getTimestamp("completed_date"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении завершённых поручений:");
            e.printStackTrace();
        }
        return tasks;
    }

    // Поиск пациентов по имени
    public List<Integer> findPatientsByName(String namePart) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id FROM patients WHERE full_name ILIKE ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + namePart + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске пациентов:");
            e.printStackTrace();
        }
        return ids;
    }

    // Информация о пациенте
    public void getPatientInfo(int patientId) {
        String sql = "SELECT full_name, birth_date, height_cm, weight_kg, blood_group, rhesus FROM patients WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("ФИО: %s, ДР: %s, рост: %d, вес: %d, кровь: %s%s%n",
                            rs.getString("full_name"), rs.getDate("birth_date"),
                            rs.getInt("height_cm"), rs.getInt("weight_kg"),
                            rs.getString("blood_group"), rs.getString("rhesus"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении информации о пациенте:");
            e.printStackTrace();
        }
    }

    // История болезни пациента
    public void getMedicalHistory(int patientId) {
        String sql = "SELECT record_date, description FROM medical_history WHERE patient_id = ? ORDER BY record_date DESC";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("[%s] %s%n", rs.getTimestamp("record_date"), rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении истории:");
            e.printStackTrace();
        }
    }

    // Добавить диагноз (запись в историю)
    public void addDiagnosis(int patientId, String description) {
        String sql = "INSERT INTO medical_history(patient_id, description) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.setString(2, description);
            stmt.executeUpdate();
            System.out.println("✔ Диагноз добавлен.");
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении диагноза:");
            e.printStackTrace();
        }
    }
}
