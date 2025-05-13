package com.hospital.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {
    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@"; // замените

    // Список пациентов
    public List<String> getAllowedDiagnoses() {
        List<String> diagnoses = new ArrayList<>();
        String filePath = "src/main/resources/diagnos.txt"; // путь к файлу

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                diagnoses.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла diagnos.txt:");
        }

        return diagnoses;
    }
    public List<String> getCurrentPatients() {
        List<String> result = new ArrayList<>();
        String sql = """
        SELECT u.id, p.full_name
        FROM patients p
        JOIN users u ON p.user_id = u.id
        ORDER BY p.full_name;
        """;
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = c.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                int uid = rs.getInt("id");
                String name = rs.getString("full_name");
                result.add(name + " (user_id: " + uid + ")");
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка при получении списка пациентов:");
        }
        return result;
    }


    // Написать поручение медсестре
    public void addNurseTask(int userId, String taskDescription) {
        try {
            int nurseId = findNurseRecordId(userId);
            if (nurseId < 0) {
                System.out.println("❌ Медсестра с таким user_id не найдена.");
                return;
            }
            String sql = "INSERT INTO nurse_tasks(nurse_id, task_description) VALUES (?, ?)";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, nurseId);
                stmt.setString(2, taskDescription);
                stmt.executeUpdate();
                System.out.println("✔ Поручение добавлено медсестре user_id=" + userId);
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка при добавлении поручения:");
        }
    }

    // возвращает nurses.id по users.id
    private int findNurseRecordId(int userId) throws SQLException {
        String sql = "SELECT id FROM nurses WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                else return -1;
            }
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
        }
        return ids;
    }

    // Информация о пациенте
    public void getPatientInfo(int userId) {
        String sql = """
        SELECT p.full_name, p.birth_date, p.height_cm, p.weight_kg, p.blood_type
        FROM patients p
        JOIN users u ON p.user_id = u.id
        WHERE u.id = ?;
        """;
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    System.out.println("🧾 Информация о пациенте:");
                    System.out.println("ФИО: " + rs.getString("full_name"));
                    System.out.println("Дата рождения: " + rs.getDate("birth_date"));
                    System.out.println("Рост: " + rs.getInt("height_cm") + " см");
                    System.out.println("Вес: " + rs.getInt("weight_kg") + " кг");
                    System.out.println("Группа крови: " + rs.getString("blood_type"));
                } else {
                    System.out.println("❌ Пациент не найден.");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка при получении информации:");
        }
    }


    // История болезни пациента
    public void getMedicalHistory(int userId) {
        String sql = """
        SELECT d.diagnosis, d.timestamp
        FROM diagnoses d
        JOIN patients p ON d.patient_id = p.id
        JOIN users u ON p.user_id = u.id
        WHERE u.id = ?
        ORDER BY d.timestamp DESC;
        """;
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                System.out.println("📖 История болезней:");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println("– " + rs.getString("diagnosis") + " (" + rs.getTimestamp("timestamp") + ")");
                }
                if (!found) {
                    System.out.println("Нет записей.");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка при получении истории:");
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
        }
    }
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
        }
        return -1;
    }
}
