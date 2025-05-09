package com.hospital.dao;

import java.sql.*;

public class StaffDAO {

    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Erzhan123@"; // замени

    public void showNurses() {
        String sql = """
            SELECT full_name, hire_date, salary
            FROM nurses
            ORDER BY hire_date
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("🩺 Медсёстры:");
            while (rs.next()) {
                System.out.printf("– %s, Принята: %s, Зарплата: %.2f%n",
                        rs.getString("full_name"),
                        rs.getDate("hire_date"),
                        rs.getDouble("salary"));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка медсестёр:");
        }
    }

    public void showDoctors() {
        String sql = """
            SELECT full_name, hire_date, salary
            FROM doctors
            ORDER BY hire_date
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("🩻 Лечащие врачи:");
            while (rs.next()) {
                System.out.printf("– %s, Принят: %s, Зарплата: %.2f%n",
                        rs.getString("full_name"),
                        rs.getDate("hire_date"),
                        rs.getDouble("salary"));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка врачей:");
        }
    }
    // Добавить врача (user_id можно получить из users)
    public void addDoctor(int userId, String fullName, Date hireDate, double salary) {
        String sql = """
        INSERT INTO doctors(user_id, full_name, hire_date, salary)
        VALUES (?, ?, ?, ?)
    """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, fullName);
            stmt.setDate(3, hireDate);
            stmt.setDouble(4, salary);

            stmt.executeUpdate();
            System.out.println("✔ Врач добавлен: " + fullName);

        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении врача:");
        }
    }

    // Добавить медсестру
    public void addNurse(int userId, String fullName, Date hireDate, double salary) {
        String sql = """
        INSERT INTO nurses(user_id, full_name, hire_date, salary)
        VALUES (?, ?, ?, ?)
    """;

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
    // Подсчёт всех пациентов
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
        }
        return 0;
    }
    // Сотрудник с максимальной зарплатой (врач или медсестра)
    public String getMaxSalaryStaff() {
        String sql = """
        SELECT full_name, salary, role FROM (
          SELECT full_name, salary, 'doctor' AS role FROM doctors
          UNION ALL
          SELECT full_name, salary, 'medassistant' AS role FROM nurses
        ) AS t
        ORDER BY salary DESC
        LIMIT 1
    """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return String.format("%s (%s), зарплата: %.2f",
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getDouble("salary"));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске сотрудника с максимальной зарплатой:");
        }
        return "Данных нет";
    }

    // Сотрудник с минимальной зарплатой (врач или медсестра)
    public String getMinSalaryStaff() {
        String sql = """
        SELECT full_name, salary, role FROM (
          SELECT full_name, salary, 'doctor' AS role FROM doctors
          UNION ALL
          SELECT full_name, salary, 'medassistant' AS role FROM nurses
        ) AS t
        ORDER BY salary ASC
        LIMIT 1
    """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return String.format("%s (%s), зарплата: %.2f",
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getDouble("salary"));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске сотрудника с минимальной зарплатой:");
        }
        return "Данных нет";
    }
    // Удалить лечащего врача по user_id
    public void deleteDoctor(int userId) {
        String sql = "DELETE FROM doctors WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int cnt = stmt.executeUpdate();
            if (cnt > 0) {
                System.out.println("✔ Врач с user_id=" + userId + " удалён.");
            } else {
                System.out.println("❌ Врач с таким user_id не найден.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении врача:");
        }
    }

    // Удалить медсестру по user_id
    public void deleteNurse(int userId) {
        String sql = "DELETE FROM nurses WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int cnt = stmt.executeUpdate();
            if (cnt > 0) {
                System.out.println("✔ Медсестра с user_id=" + userId + " удалена.");
            } else {
                System.out.println("❌ Медсестра с таким user_id не найдена.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении медсестры:");
        }
    }

}

