package com.hospital;

import com.hospital.dao.UserDAO;

public class UserTest {
    public static void main(String[] args) {
        UserDAO dao = new UserDAO();

        // Добавим тестового пользователя
        dao.createUser("admin1", "adminpass", "maindoctor");

        // Посмотрим список всех пользователей
        System.out.println("Пользователи:");
        for (String user : dao.getAllUsers()) {
            System.out.println("– " + user);
        }
    }
}

