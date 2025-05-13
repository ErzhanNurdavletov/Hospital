package com.hospital.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {
    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@"; // замените

    // Показать медсестёр
    public void showNurses() {
        String sql = "SELECT user_id, full_name, hire_date, salary FROM nurses ORDER BY full_name";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("🩺 Медсестры:");
            while (rs.next()) {
                System.out.printf("user_id=%d, %s, принят: %s, зарплата=%.2f%n",
                        rs.getInt("user_id"), rs.getString("full_name"), rs.getDate("hire_date"), rs.getDouble("salary"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка медсестёр:");
        }
    }

    // Показать врачей
    public void showDoctors() {
        String sql = "SELECT user_id, full_name, hire_date, salary FROM doctors ORDER BY full_name";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("🩻 Лечащие врачи:");
            while (rs.next()) {
                System.out.printf("user_id=%d, %s, принят: %s, зарплата=%.2f%n",
                        rs.getInt("user_id"), rs.getString("full_name"), rs.getDate("hire_date"), rs.getDouble("salary"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка врачей:");
        }
    }

    // Добавить медсестру
    public void addNurse(int userId, String fullName, Date hireDate, double salary) {
        String sql = "INSERT INTO nurses(user_id, full_name, hire_date, salary) VALUES (?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, fullName);
            stmt.setDate(3, hireDate);
            stmt.setDouble(4, salary);
            stmt.executeUpdate();
            System.out.println("✔ Медсестра добавлена: " + fullName);
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении медсестры:");
        }
    }

    // Добавить врача
    public void addDoctor(int userId, String fullName, Date hireDate, double salary) {
        String sql = "INSERT INTO doctors(user_id, full_name, hire_date, salary) VALUES (?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, fullName);
            stmt.setDate(3, hireDate);
            stmt.setDouble(4, salary);
            stmt.executeUpdate();
            System.out.println("✔ Лечащий врач добавлен: " + fullName);
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении врача:");
        }
    }

    // Удалить медсестру по user_id
    public void deleteNurse(int userId) {
        String sql = "DELETE FROM nurses WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int cnt = stmt.executeUpdate();
            if (cnt > 0) System.out.println("✔ Медсестра удалена (user_id=" + userId + ")");
            else System.out.println("❌ Медсестра не найдена.");
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении медсестры:");
        }
    }

    // Удалить врача по user_id
    public void deleteDoctor(int userId) {
        String sql = "DELETE FROM doctors WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int cnt = stmt.executeUpdate();
            if (cnt > 0) System.out.println("✔ Врач удалён (user_id=" + userId + ")");
            else System.out.println("❌ Врач не найден.");
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении врача:");
        }
    }

//    // Количество пациентов
//    public int getPatientCount() {
//        String sql = "SELECT COUNT(*) AS cnt FROM patients";
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(sql)) {
//            if (rs.next()) return rs.getInt("cnt");
//        } catch (SQLException e) {
//            System.err.println("Ошибка при подсчёте пациентов:");
//            e.printStackTrace();
//        }
//        return 0;
//    }

    // Сотрудник с макс. зарплатой
    public String getMaxSalaryStaff() {
        String sql = "SELECT full_name, salary, role FROM ("
                + "SELECT full_name, salary, 'doctor' AS role FROM doctors "
                + "UNION ALL "
                + "SELECT full_name, salary, 'medassistant' AS role FROM nurses) t "
                + "ORDER BY salary DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("full_name") + " (" + rs.getString("role") + "), зарплата=" + rs.getDouble("salary");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске макс. зарплаты:");
        }
        return "Данных нет";
    }

    // Сотрудник с мин. зарплатой
    public String getMinSalaryStaff() {
        String sql = "SELECT full_name, salary, role FROM ("
                + "SELECT full_name, salary, 'doctor' AS role FROM doctors "
                + "UNION ALL "
                + "SELECT full_name, salary, 'medassistant' AS role FROM nurses) t "
                + "ORDER BY salary ASC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("full_name") + " (" + rs.getString("role") + "), зарплата=" + rs.getDouble("salary");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске мин. зарплаты:");
        }
        return "Данных нет";
    }
}
