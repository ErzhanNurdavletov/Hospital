package com.hospital.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@";

    // 1) Список всех пациентов
    public List<String> getCurrentPatients() {
        List<String> patients = new ArrayList<>();
        String sql = """
            SELECT p.full_name, u.username
            FROM patients p
            JOIN users u ON p.user_id = u.id
        """;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                patients.add(rs.getString("full_name") +
                        " (login: " + rs.getString("username") + ")");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка пациентов:");
            e.printStackTrace();
        }
        return patients;
    }

    // 2) Количество пациентов
    public int getPatientCount() {
        String sql = "SELECT COUNT(*) AS cnt FROM patients";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при подсчёте пациентов:");
            e.printStackTrace();
        }
        return 0;
    }

    // 3) Текущие поручения медсестрам (не выполненные)
    public List<String> getPendingNurseTasks() {
        List<String> tasks = new ArrayList<>();
        String sql = """
            SELECT id, nurse_id, task_description, assigned_date
            FROM nurse_tasks
            WHERE completed = FALSE
            ORDER BY assigned_date
        """;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(String.format(
                        "ID:%d, nurse_id:%d, \"%s\", assigned:%s",
                        rs.getInt("id"),
                        rs.getInt("nurse_id"),
                        rs.getString("task_description"),
                        rs.getTimestamp("assigned_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении поручений:");
            e.printStackTrace();
        }
        return tasks;
    }

    // 4) Добавить поручение медсестре
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

    // 5) Список выполненных поручений
    public List<String> getCompletedNurseTasks() {
        List<String> tasks = new ArrayList<>();
        String sql = """
            SELECT id, nurse_id, task_description, completed_date
            FROM nurse_tasks
            WHERE completed = TRUE
            ORDER BY completed_date
        """;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(String.format(
                        "ID:%d, nurse_id:%d, \"%s\", done:%s",
                        rs.getInt("id"),
                        rs.getInt("nurse_id"),
                        rs.getString("task_description"),
                        rs.getTimestamp("completed_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении выполненных поручений:");
            e.printStackTrace();
        }
        return tasks;
    }
    public List<Integer> findPatientsByName(String namePart) {
        List<Integer> ids = new ArrayList<>();
        String sql = """
        SELECT id, full_name
        FROM patients
        WHERE full_name ILIKE ?
    """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + namePart + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("🔍 Найденные пациенты:");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("full_name");
                    System.out.printf("ID: %d — %s%n", id, name);
                    ids.add(id);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске пациента:");
            e.printStackTrace();
        }

        return ids;
    }
    public void getPatientInfo(int patientId) {
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
                    System.out.println("👤 Информация о пациенте:");
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
                } else {
                    System.out.println("❌ Пациент не найден.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении информации о пациенте:");
            e.printStackTrace();
        }
    }
    public void getMedicalHistory(int patientId) {
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
            System.err.println("Ошибка при получении истории болезни:");
            e.printStackTrace();
        }
    }
    public void addDiagnosis(int patientId, String description) {
        String sql = """
        INSERT INTO medical_history(patient_id, description)
        VALUES (?, ?)
    """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.setString(2, description);
            stmt.executeUpdate();
            System.out.println("✔ Диагноз добавлен в историю болезни.");
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении диагноза:");
            e.printStackTrace();
        }
    }

}

