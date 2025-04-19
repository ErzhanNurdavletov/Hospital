package com.hospital;

import com.hospital.dao.PatientDAO;

import java.sql.Date;

public class PatientTest {
    public static void main(String[] args) {
        PatientDAO dao = new PatientDAO();

        // id пользователя из users, к которому привязан пациент (создавали admin1)
        int userId = 1; // замените на ID admin1, если он отличается

        dao.createPatient(
                userId,
                "Иван Петров",
                Date.valueOf("1990-06-15"),
                180,
                75,
                "A",
                "+"
        );

        System.out.println("Список пациентов:");
        for (String p : dao.getAllPatients()) {
            System.out.println("– " + p);
        }
    }
}

