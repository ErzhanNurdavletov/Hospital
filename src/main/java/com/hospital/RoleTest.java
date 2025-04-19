package com.hospital;

import com.hospital.dao.RoleDAO;

public class RoleTest {
    public static void main(String[] args) {
        RoleDAO dao = new RoleDAO();

        // Добавление новой роли
        dao.addRole("testrole");

        // Чтение всех ролей
        System.out.println("Роли в системе:");
        for (String role : dao.getAllRoles()) {
            System.out.println("– " + role);
        }
    }
}

