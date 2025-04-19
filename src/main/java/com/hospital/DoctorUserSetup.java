package com.hospital;

import com.hospital.dao.UserDAO;

public class DoctorUserSetup {
    public static void main(String[] args) {
        UserDAO udao = new UserDAO();
        udao.createUser("doc1", "pass", "doctor");
        System.out.println("Готово: в БД создан пользователь doc1/doctor.");
    }
}

