package com.hospital;

import com.hospital.service.AuthService;
import com.hospital.dao.StaffDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.NurseDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.UserDAO;

import java.sql.Date;
import java.util.List;
import java.util.Scanner;

public class MainMenu {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== АИС «Больница» ===");
            System.out.println("0. Выход");
            System.out.print("Введите тип аккаунта (patient/doctor/medassistant/maindoctor): ");
            String type = scanner.nextLine().trim().toLowerCase();
            if ("0".equals(type)) {
                System.out.println("Программа завершена.");
                break;
            }

            System.out.print("Логин: ");
            String login = scanner.nextLine().trim();
            System.out.print("Пароль: ");
            String pass = scanner.nextLine().trim();

            String role = AuthService.login(login, pass);
            if (role == null || !role.equals(type)) {
                System.out.println("❌ Неверный тип аккаунта, логин или пароль.");
                continue;
            }
            UserDAO userDAO = new UserDAO();
            int currentUserId = userDAO.getUserIdByUsername(login);

            switch (role) {
                case "patient"      -> showPatientMenu(scanner, currentUserId);
                case "doctor"       -> showDoctorMenu(scanner);
                case "medassistant" -> showNurseMenu(scanner, currentUserId);
                case "maindoctor"   -> showMainDoctorMenu(scanner);
                default               -> System.out.println("❌ Неизвестная роль.");
            }
        }
        scanner.close();
    }

    private static void showPatientMenu(Scanner scanner, int userId) {
        PatientDAO patientDAO = new PatientDAO();

        int patientId = patientDAO.getPatientIdByUserId(userId);
        if (patientId < 0) {
            System.out.println("❌ Пациент не найден.");
            return;
        }
        while (true) {
            System.out.println("\n--- Меню пациента ---");
            System.out.println("1. Показать мой диагноз");
            System.out.println("2. Показать мою информацию");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) break;
            switch (choice) {
                case "1" -> {
                    String diag = patientDAO.getLatestDiagnosis(patientId);
                    System.out.println(diag == null ? "Нет диагнозов." : "🔍 Ваш диагноз: " + diag);
                }
                case "2" -> patientDAO.showPersonalInfo(patientId);
                default  -> System.out.println("❌ Неверный ввод.");
            }
        }
    }

    private static void showDoctorMenu(Scanner scanner) {
        DoctorDAO dao       = new DoctorDAO();
        StaffDAO staffDAO = new StaffDAO();
        while (true) {
            System.out.println("\n--- Меню лечащего врача ---");
            System.out.println("1. Показать список пациентов");
            System.out.println("2. Показать спиоск медсестер");
            System.out.println("3. Написать поручение для медсестры");
            System.out.println("4. Показать не завершённые поручения");
            System.out.println("5. Показать завершённые поручения");
            System.out.println("6. Поиск пациента");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            String c = scanner.nextLine().trim();
            if ("0".equals(c)) break;
            switch (c) {
                case "1" -> dao.getCurrentPatients().forEach(p -> System.out.println("– " + p));
                case "2" -> staffDAO.showNurses();
                case "3" -> {
                    int nid;
                    while (true) {
                        System.out.print("Введите user_id медсестры: ");
                        try { nid = Integer.parseInt(scanner.nextLine().trim()); break; }
                        catch (NumberFormatException e) { System.out.println("❌ Введите числовой ID."); }
                    }
                    System.out.print("Текст поручения: ");
                    String desc = scanner.nextLine().trim();
                    if (!desc.isEmpty()) dao.addNurseTask(nid, desc);
                    else System.out.println("❌ Описание не должно быть пустым.");
                }
                case "4" -> dao.getPendingNurseTasks().forEach(t -> System.out.println("– " + t));
                case "5" -> dao.getCompletedNurseTasks().forEach(t -> System.out.println("– " + t));
                case "6" -> {
                    PatientDAO patDao = new PatientDAO();

                    // 1) Ввод и проверка user_id
                    int pid;
                    while (true) {
                        System.out.print("Введите user_id пациента: ");
                        String line = scanner.nextLine().trim();
                        try {
                            pid = Integer.parseInt(line);
                            if (patDao.getPatientIdByUserId(pid) < 0) {
                                System.out.println("❌ Пациент с таким user_id не найден.");
                                continue;
                            }
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Введите корректный числовой ID.");
                        }
                    }

                    // 2) Загрузка списка допустимых диагнозов
                    List<String> allowed = dao.getAllowedDiagnoses();
                    if (allowed.isEmpty()) {
                        System.out.println("❌ Список диагнозов пуст. Обратитесь к администратору.");
                        break;
                    }

                    // 3) Подменю пациента
                    while (true) {
                        System.out.println("\n--- Работа с пациентом user_id=" + pid + " ---");
                        System.out.println("1. Показать информацию");
                        System.out.println("2. Показать историю болезни");
                        System.out.println("3. Добавить диагноз");
                        System.out.println("0. Назад");
                        System.out.print("Выбор: ");
                        String sub = scanner.nextLine().trim();

                        switch (sub) {
                            case "1" -> patDao.showPersonalInfo(patDao.getPatientIdByUserId(pid));
                            case "2" -> patDao.showMedicalHistory(patDao.getPatientIdByUserId(pid));
                            case "3" -> {
                                System.out.println("Доступные диагнозы:");
                                allowed.forEach(d -> System.out.println("– " + d));

                                System.out.print("Введите диагноз из списка: ");
                                String diag = scanner.nextLine().trim();
                                if (allowed.contains(diag)) {
                                    dao.addDiagnosis(patDao.getPatientIdByUserId(pid), diag);
                                } else {
                                    System.out.println("❌ Такого диагноза нет в списке.");
                                }
                            }
                            case "0" -> { break; }
                            default  -> System.out.println("❌ Неверный ввод.");
                        }

                        if ("0".equals(sub)) break;
                    }
                }


                default -> System.out.println("❌ Неверный ввод.");
            }
        }
    }

    private static void showNurseMenu(Scanner scanner, int currentUserId) {
        NurseDAO dao = new NurseDAO();
        int nurseId = 0;
        try {
            nurseId = dao.getNurseIdByUserId(currentUserId);
        } catch (Exception e) {
            System.out.println("Ошибка при поиске nurse id");
        }
        if (nurseId < 0) {
            System.out.println("❌ Профиль медсестры не найден.");
            return;
        }

        while (true) {
            System.out.println("\n--- Меню медсестры ---");
            System.out.println("1. Показать не завершённые поручения");
            System.out.println("2. Выполнить поручение");
            System.out.println("3. Показать завершённые поручения");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            String c = scanner.nextLine().trim();
            if ("0".equals(c)) break;
            switch (c) {
//                case "1" -> {
//                    System.out.print("Часть имени: ");
//                    String part = scanner.nextLine().trim();
//                    if (!part.isEmpty()) dao.findPatientsByName(part).forEach(p -> System.out.println("– " + p));
//                    else System.out.println("❌ Строка не должна быть пустой.");
//                }
                case "1" -> dao.getTasksForNurse(nurseId).forEach(t -> System.out.println("– " + t));
                case "2" -> {
                    int tid;
                    while (true) {
                        System.out.print("Введите ID поручения: ");
                        try { tid = Integer.parseInt(scanner.nextLine().trim()); break; }
                        catch (NumberFormatException e) { System.out.println("❌ Введите числовой ID."); }
                    }
                    dao.completeTask(tid);
                }
                case "3" -> dao.getCompletedTasksForNurse(nurseId).forEach(t -> System.out.println("– " + t));
                default -> System.out.println("❌ Неверный ввод.");
            }
        }
    }

    private static void showMainDoctorMenu(Scanner scanner) {
        StaffDAO staffDAO = new StaffDAO();
        PatientDAO patientDAO = new PatientDAO();
        UserDAO userDAO = new UserDAO();

        while (true) {
            System.out.println("\n--- Меню главврача ---");
            System.out.println("1. Показать список медсестёр");
            System.out.println("2. Показать список лечащих врачей");
            System.out.println("3. Показать список пациентов");
            System.out.println("4. Сотрудник с максимальной зарплатой");
            System.out.println("5. Сотрудник с минимальной зарплатой");
            System.out.println("6. Добавить пациента");
            System.out.println("7. Добавить медсестру");
            System.out.println("8. Добавить лечащего врача");
            System.out.println("9. Удалить пациента");
            System.out.println("10. Удалить медсестру");
            System.out.println("11. Удалить лечащего врача");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) return;
            switch (choice) {
                case "1" -> staffDAO.showNurses();
                case "2" -> staffDAO.showDoctors();
                case "3" -> patientDAO.getAllPatients().forEach(p -> System.out.println("– " + p));
                case "4" -> System.out.println(staffDAO.getMaxSalaryStaff());
                case "5" -> System.out.println(staffDAO.getMinSalaryStaff());
                case "6" -> {
                    // Сначала ввод личных данных пациента
                    String fullName;
                    while (true) {
                        System.out.print("ФИО пациента: ");
                        try {
                            fullName = scanner.nextLine().trim();
                            if (fullName.isBlank()) throw new IllegalArgumentException();
                            break;

                        } catch (Exception e) {
                            System.out.println("❌ Неверный формат ФИО");
                        }
                    }

                    Date birthDate;
                    while (true) {
                        System.out.print("Дата рождения (ГГГГ-ММ-ДД): ");
                        try {
                            birthDate = Date.valueOf(scanner.nextLine().trim());
                            break;
                        } catch (IllegalArgumentException e) {
                            System.out.println("❌ Неверный формат даты.");
                        }
                    }
                    int height;
                    while (true) {
                        System.out.print("Рост (см, >0): ");
                        try {
                            height = Integer.parseInt(scanner.nextLine().trim());
                            if (height > 0) break;
                            else System.out.println("❌ Значение должно быть положительным.");
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Введите число.");
                        }
                    }
                    int weight;
                    while (true) {
                        System.out.print("Вес (кг, >0): ");
                        try {
                            weight = Integer.parseInt(scanner.nextLine().trim());
                            if (weight > 0) break;
                            else System.out.println("❌ Значение должно быть положительным.");
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Введите число.");
                        }
                    }

                    String bloodGroup;
                    while (true) {
                        System.out.print("Группа крови: ");
                        try {
                            bloodGroup = scanner.nextLine().trim();
                            if (bloodGroup.isBlank()) throw new IllegalArgumentException();
                            break;
                        } catch (Exception e) {
                            System.out.println("❌ Ошибка");
                        }
                    }

                    String rhesus;
                    while (true) {
                        System.out.print("Резус: ");
                        try {
                            rhesus = scanner.nextLine().trim();
                            if (rhesus.isBlank()) throw new IllegalArgumentException();
                            break;
                        } catch (Exception e) {
                            System.out.println("❌ Ошибка");
                        }
                    }

                    // После валидации создаем учётку и добавляем
                    int uid = -1;
                    while (uid < 0) {
                        System.out.print("Логин пациента: ");
                        String login = scanner.nextLine().trim();
                        System.out.print("Пароль: ");
                        String pwd = scanner.nextLine().trim();
                        if (login.isBlank() || pwd.isBlank()) {
                            System.out.println("❌ Ошибка");
                            continue;
                        }
                        uid = userDAO.createUser(login, pwd, "patient");
                        if (uid < 0) System.out.println("❌ Логин занят или ошибка. Попробуйте другой.");
                    }
                    patientDAO.createPatient(uid, fullName, birthDate, height, weight, bloodGroup, rhesus);
                }
                case "7" -> {
                    // Личные данные медсестры
                    System.out.print("ФИО медсестры: ");
                    String fullName = scanner.nextLine().trim();
                    Date hireDate;
                    while (true) {
                        System.out.print("Дата приёма (ГГГГ-ММ-ДД): ");
                        try {
                            hireDate = Date.valueOf(scanner.nextLine().trim());
                            break;
                        } catch (IllegalArgumentException e) {
                            System.out.println("❌ Неверный формат даты.");
                        }
                    }
                    double salary;
                    while (true) {
                        System.out.print("Зарплата (>0): ");
                        try {
                            salary = Double.parseDouble(scanner.nextLine().trim());
                            if (salary > 0) break;
                            else System.out.println("❌ Значение должно быть положительным.");
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Введите число.");
                        }
                    }

                    // Создание учётки медсестры
                    int uid = -1;
                    while (uid < 0) {
                        System.out.print("Логин медсестры: ");
                        String login = scanner.nextLine().trim();
                        System.out.print("Пароль: ");
                        String pwd = scanner.nextLine().trim();
                        uid = userDAO.createUser(login, pwd, "medassistant");
                        if (uid < 0) System.out.println("❌ Логин занят или ошибка. Попробуйте другой.");
                    }
                    staffDAO.addNurse(uid, fullName, hireDate, salary);
                }
                case "8" -> {
                    // Личные данные врача
                    System.out.print("ФИО врача: ");
                    String fullName = scanner.nextLine().trim();
                    Date hireDate;
                    while (true) {
                        System.out.print("Дата приёма (ГГГГ-ММ-ДД): ");
                        try {
                            hireDate = Date.valueOf(scanner.nextLine().trim());
                            break;
                        } catch (IllegalArgumentException e) {
                            System.out.println("❌ Неверный формат даты.");
                        }
                    }
                    double salary;
                    while (true) {
                        System.out.print("Зарплата (>0): ");
                        try {
                            salary = Double.parseDouble(scanner.nextLine().trim());
                            if (salary > 0) break;
                            else System.out.println("❌ Значение должно быть положительным.");
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Введите число.");
                        }
                    }

                    // Создание учётки врача
                    int uid = -1;
                    while (uid < 0) {
                        System.out.print("Логин врача: ");
                        String login = scanner.nextLine().trim();
                        System.out.print("Пароль: ");
                        String pwd = scanner.nextLine().trim();
                        uid = userDAO.createUser(login, pwd, "doctor");
                        if (uid < 0) System.out.println("❌ Логин занят или ошибка. Попробуйте другой.");
                    }
                    staffDAO.addDoctor(uid, fullName, hireDate, salary);
                }
                case "9" -> {
                    int uid;
                    while (true) {
                        System.out.print("Введите user_id пациента для удаления: ");
                        try { uid = Integer.parseInt(scanner.nextLine().trim()); break;}
                        catch (NumberFormatException e) { System.out.println("❌ Введите число."); }
                    }
                    patientDAO.deletePatient(uid);
                }
                case "10" -> {
                    int uid;
                    while (true) {
                        System.out.print("Введите user_id медсестры для удаления: ");
                        try { uid = Integer.parseInt(scanner.nextLine().trim()); break; }
                        catch (NumberFormatException e) { System.out.println("❌ Введите число."); }
                    }
                    try {
                        staffDAO.deleteNurseCascade(uid);
                    } catch (Exception e) {
                        System.out.println("Ошибка при попытке удаления медсестры");
                    }
                }
                case "11" -> {
                    int uid;
                    while (true) {
                        System.out.print("Введите user_id врача для удаления: ");
                        try { uid = Integer.parseInt(scanner.nextLine().trim()); break; }
                        catch (NumberFormatException e) { System.out.println("❌ Введите число."); }
                    }
                    staffDAO.deleteDoctor(uid);
                }
                default -> System.out.println("❌ Неверный ввод.");
            }
        }
    }
}