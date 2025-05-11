package com.hospital.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@"; // замените на ваш пароль

    // Создание пациента
    public void createPatient(int userId, String fullName, Date birthDate,
                              int heightCm, int weightKg, String bloodGroup, String rhesus) {
        String sql = "INSERT INTO patients(user_id, full_name, birth_date, height_cm, weight_kg, blood_group, rhesus) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, fullName);
            stmt.setDate(3, birthDate);
            stmt.setInt(4, heightCm);
            stmt.setInt(5, weightKg);
            stmt.setString(6, bloodGroup);
            stmt.setString(7, rhesus);
            stmt.executeUpdate();
            System.out.println("✔ Пациент добавлен: " + fullName);
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении пациента:");
            e.printStackTrace();
        }
    }

    // Личная информация пациента
    public void showPersonalInfo(int patientId) {
        String sql = "SELECT user_id, full_name, birth_date, height_cm, weight_kg, blood_group, rhesus FROM patients WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("user_id=%d, ФИО: %s, ДР: %s, Рост: %d, Вес: %d, Группа: %s%s%n",
                            rs.getInt("user_id"), rs.getString("full_name"), rs.getDate("birth_date"),
                            rs.getInt("height_cm"), rs.getInt("weight_kg"),
                            rs.getString("blood_group"), rs.getString("rhesus"));
                } else {
                    System.out.println("❌ Пациент не найден.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении информации пациента:");
            e.printStackTrace();
        }
    }

    // История болезни пациента
    public void showMedicalHistory(int patientId) {
        String sql = "SELECT record_date, description FROM medical_history WHERE patient_id = ? ORDER BY record_date DESC";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("[%s] — %s%n", rs.getTimestamp("record_date"), rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении истории болезни:");
            e.printStackTrace();
        }
    }

    // Последний диагноз пациента
    public String getLatestDiagnosis(int patientId) {
        String sql = "SELECT description FROM medical_history WHERE patient_id = ? ORDER BY record_date DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("description");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении последнего диагноза:");
            e.printStackTrace();
        }
        return null;
    }

    // Список всех пациентов с user_id
    public List<String> getAllPatients() {
        List<String> patients = new ArrayList<>();
        String sql = "SELECT user_id, full_name, (SELECT username FROM users u WHERE u.id = p.user_id) AS login FROM patients p";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int uid = rs.getInt("user_id");
                String name = rs.getString("full_name");
                String login = rs.getString("login");
                patients.add(String.format("user_id=%d, %s (login: %s)", uid, name, login));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка пациентов:");
            e.printStackTrace();
        }
        return patients;
    }

    // Удаление пациента по user_id
    public void deletePatient(int userId) {
        String sql = "DELETE FROM patients WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int cnt = stmt.executeUpdate();
            if (cnt > 0) System.out.println("✔ Пациент удалён.");
            else System.out.println("❌ Пациент не найден.");
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении пациента:");
            e.printStackTrace();
        }
    }

    // Получить patient_id по user_id
    public int getPatientIdByUserId(int userId) {
        String sql = "SELECT id FROM patients WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении ID пациента по user_id:");
            e.printStackTrace();
        }
        return -1;
    }
}
