package com.hospital.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@"; // замени

    // Метод для создания пациента (привязка к существующему пользователю)
    public void createPatient(int userId, String fullName, Date birthDate,
                              int heightCm, int weightKg, String bloodGroup, String rhesus) {

        String sql = """
            INSERT INTO patients(user_id, full_name, birth_date, height_cm, weight_kg, blood_group, rhesus)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

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

    // Метод для получения всех пациентов
    public List<String> getAllPatients() {
        List<String> patients = new ArrayList<>();

        String sql = """
            SELECT p.full_name, p.birth_date, p.height_cm, p.weight_kg, p.blood_group, p.rhesus,
                   u.username
            FROM patients p
            JOIN users u ON p.user_id = u.id
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String patient = String.format(
                        "%s (username: %s), Дата рождения: %s, Рост: %d см, Вес: %d кг, Группа крови: %s%s",
                        rs.getString("full_name"),
                        rs.getString("username"),
                        rs.getDate("birth_date"),
                        rs.getInt("height_cm"),
                        rs.getInt("weight_kg"),
                        rs.getString("blood_group"),
                        rs.getString("rhesus")
                );
                patients.add(patient);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении пациентов:");
            e.printStackTrace();
        }

        return patients;
    }
    public int getPatientIdByUserId(int userId) {
        String sql = "SELECT id FROM patients WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public void showPersonalInfo(int patientId) {
        String sql = """
        SELECT full_name, birth_date, height_cm, weight_kg, blood_group, rhesus
        FROM patients
        WHERE id = ?
    """;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("👤 Личная информация:");
                    System.out.printf("""
                        ФИО: %s
                        Дата рождения: %s
                        Рост: %d см
                        Вес: %d кг
                        Группа крови: %s%s
                        """,
                            rs.getString("full_name"),
                            rs.getDate("birth_date"),
                            rs.getInt("height_cm"),
                            rs.getInt("weight_kg"),
                            rs.getString("blood_group"),
                            rs.getString("rhesus"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void showMedicalHistory(int patientId) {
        String sql = """
        SELECT record_date, description
        FROM medical_history
        WHERE patient_id = ?
        ORDER BY record_date DESC
    """;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("📖 История болезни:");
                while (rs.next()) {
                    System.out.printf("[%s] — %s%n",
                            rs.getTimestamp("record_date"),
                            rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

