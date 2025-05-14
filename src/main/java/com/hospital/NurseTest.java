package com.hospital;

import com.hospital.dao.UserDAO;

public class NurseTest {
    public static void main(String[] args) {
        UserDAO udao = new UserDAO();
        udao.createUser("nurse1", "nursepass", "medassistant");
        System.out.println("Готово: создана учётка nurse1/medassistant.");
    }
}

