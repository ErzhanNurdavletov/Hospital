package com.hospital.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class BaseDAO {
    protected static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    protected static final String USER = "postgres";
    protected static final String PASSWORD = "Erzhan123@";

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
