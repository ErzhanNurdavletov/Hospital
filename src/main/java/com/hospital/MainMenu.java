package com.hospital;

import com.hospital.service.AuthService;

import java.sql.Date;
import java.util.Scanner;
import java.util.List;

public class MainMenu {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Для входа в систему введите логин:");
        String username = scanner.nextLine();

        System.out.println("Введите пароль:");
        String password = scanner.nextLine();

        String role = AuthService.login(username, password);

        if (role == null) {
            System.out.println("❌ Неверный логин или пароль.");
            return;
        }

        System.out.println("✅ Успешный вход. Ваша роль: " + role);

        switch (role) {
            case "patient" -> showPatientMenu(scanner);
            case "doctor" -> showDoctorMenu(scanner);
            case "medassistant" -> showNurseMenu(scanner);
            case "maindoctor" -> showMainDoctorMenu(scanner);
            default -> System.out.println("Неизвестная роль.");
        }

        scanner.close();
    }

    private static void showPatientMenu(Scanner scanner) {
        com.hospital.dao.PatientDAO patientDAO = new com.hospital.dao.PatientDAO();

        System.out.print("Введите ваш user_id: ");
        int userId = Integer.parseInt(scanner.nextLine());

        int patientId = patientDAO.getPatientIdByUserId(userId);
        if (patientId == -1) {
            System.out.println("❌ Пациент с таким user_id не найден.");
            return;
        }

        while (true) {
            System.out.println("📋 Меню пациента:");
            System.out.println("1. Посмотреть личную информацию");
            System.out.println("2. Посмотреть историю болезни");
            System.out.println("3. Посмотреть даты лечения");
            System.out.println("0. Выход");
            System.out.print("Выбор: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> patientDAO.showPersonalInfo(patientId);
                case "2" -> patientDAO.showMedicalHistory(patientId);
                case "3" -> {
                    System.out.println("📅 Даты лечения:");
                    patientDAO.showMedicalHistory(patientId); // используем те же записи
                }
                case "0" -> {
                    System.out.println("✅ Выход из меню пациента.");
                    return;
                }
                default -> System.out.println("❌ Неверный ввод.");
            }
        }
    }


    private static void showDoctorMenu(Scanner scanner) {
        com.hospital.dao.DoctorDAO dao = new com.hospital.dao.DoctorDAO();

        while (true) {
            System.out.println("📋 Меню лечащего врача:");
            System.out.println("1. Показать список пациентов");
            System.out.println("2. Показать количество пациентов");
            System.out.println("3. Показать поручения для медсестёр");
            System.out.println("4. Написать поручение для медсестры");
            System.out.println("5. Показать завершённые поручения");
            System.out.println("6. Поиск пациента");
            System.out.println("0. Выход");
            System.out.print("Выбор: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> {
                    System.out.println("🩺 Список пациентов:");
                    for (String p : dao.getCurrentPatients()) {
                        System.out.println("– " + p);
                    }
                }
                case "2" -> {
                    int cnt = dao.getPatientCount();
                    System.out.println("🔢 Количество пациентов: " + cnt);
                }
                case "3" -> {
                    System.out.println("📖 Поручения для медсестёр:");
                    for (String t : dao.getPendingNurseTasks()) {
                        System.out.println("– " + t);
                    }
                }
                case "4" -> {
                    System.out.print("Введите nurse_id: ");
                    int nid = Integer.parseInt(scanner.nextLine());
                    System.out.print("Введите текст поручения: ");
                    String desc = scanner.nextLine();
                    dao.addNurseTask(nid, desc);
                }
                case "5" -> {
                    System.out.println("✅ Завершённые поручения:");
                    for (String t : dao.getCompletedNurseTasks()) {
                        System.out.println("– " + t);
                    }
                }
                case "6" -> {
                    System.out.print("Введите часть имени пациента: ");
                    String search = scanner.nextLine();

                    List<Integer> foundIds = dao.findPatientsByName(search);
                    if (foundIds.isEmpty()) {
                        System.out.println("Пациенты не найдены.");
                        break;
                    }

                    System.out.print("Введите ID пациента для действий: ");
                    int patientId = Integer.parseInt(scanner.nextLine());

                    while (true) {
                        System.out.println("🔍 Работа с пациентом ID=" + patientId);
                        System.out.println("1. Посмотреть информацию");
                        System.out.println("2. Посмотреть историю болезни");
                        System.out.println("3. Добавить диагноз");
                        System.out.println("0. Назад");
                        System.out.print("Выбор: ");
                        String sub = scanner.nextLine();

                        switch (sub) {
                            case "1" -> dao.getPatientInfo(patientId);
                            case "2" -> dao.getMedicalHistory(patientId);
                            case "3" -> {
                                System.out.print("Введите текст диагноза: ");
                                String diag = scanner.nextLine();
                                dao.addDiagnosis(patientId, diag);
                            }
                            case "0" -> { break; }
                            default -> System.out.println("❌ Неверный ввод.");
                        }

                        if (sub.equals("0")) break;
                    }
                }

                case "0" -> {
                    System.out.println("✅ Выход из меню лечащего врача.");
                    return;
                }
                default -> System.out.println("❌ Неверный ввод. Попробуйте снова.");
            }
        }
    }


    private static void showNurseMenu(Scanner scanner) {
        com.hospital.dao.NurseDAO nurseDAO = new com.hospital.dao.NurseDAO();

        System.out.print("Введите ваш nurse_id: ");
        int nurseId = Integer.parseInt(scanner.nextLine());

        while (true) {
            System.out.println("📋 Меню медсестры:");
            System.out.println("1. Показать мои поручения");
            System.out.println("2. Выполнить поручение");
            System.out.println("3. Показать завершённые поручения");
            System.out.println("4. Найти пациента по имени");
            System.out.println("0. Выход");
            System.out.print("Выбор: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> {
                    System.out.println("📌 Текущие поручения:");
                    var tasks = nurseDAO.getTasksForNurse(nurseId);
                    if (tasks.isEmpty()) {
                        System.out.println("Нет текущих поручений.");
                    } else {
                        tasks.forEach(t -> System.out.println("– " + t));
                    }
                }
                case "2" -> {
                    System.out.print("Введите ID поручения для завершения: ");
                    int taskId = Integer.parseInt(scanner.nextLine());
                    nurseDAO.completeTask(taskId);
                }
                case "3" -> {
                    System.out.println("✅ Завершённые поручения:");
                    var done = nurseDAO.getCompletedTasksForNurse(nurseId);
                    if (done.isEmpty()) {
                        System.out.println("Нет завершённых поручений.");
                    } else {
                        done.forEach(t -> System.out.println("– " + t));
                    }
                }
                case "4" -> {
                    System.out.print("Введите часть имени пациента: ");
                    String search = scanner.nextLine();
                    var results = nurseDAO.findPatientsByName(search);
                    if (results.isEmpty()) {
                        System.out.println("Пациенты не найдены.");
                    } else {
                        results.forEach(p -> System.out.println("– " + p));
                    }
                }
                case "0" -> {
                    System.out.println("✅ Выход из меню медсестры.");
                    return;
                }
                default -> System.out.println("❌ Неверный ввод. Попробуйте снова.");
            }
        }
    }


    private static void showMainDoctorMenu(Scanner scanner) {
        com.hospital.dao.StaffDAO staffDAO = new com.hospital.dao.StaffDAO();

        while (true) {
            System.out.println("📋 Меню главврача:");
            System.out.println("1. Показать список медсестёр");
            System.out.println("2. Показать список лечащих врачей");
            System.out.println("3. Добавить медсестру");
            System.out.println("4. Добавить лечащего врача");
            System.out.println("5. Показать количество пациентов");
            System.out.println("6. Сотрудник с максимальной зарплатой");
            System.out.println("7. Сотрудник с минимальной зарплатой");
            System.out.println("0. Выход");
            System.out.print("Выбор: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> staffDAO.showNurses();
                case "2" -> staffDAO.showDoctors();
                case "3" -> {
                    System.out.print("Введите user_id медсестры: ");
                    int userId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Введите ФИО: ");
                    String fullName = scanner.nextLine();

                    System.out.print("Введите дату приёма (ГГГГ-ММ-ДД): ");
                    Date hireDate = Date.valueOf(scanner.nextLine());

                    System.out.print("Введите зарплату: ");
                    double salary = Double.parseDouble(scanner.nextLine());

                    staffDAO.addNurse(userId, fullName, hireDate, salary);
                }
                case "4" -> {
                    System.out.print("Введите user_id врача: ");
                    int userId = Integer.parseInt(scanner.nextLine());

                    System.out.print("Введите ФИО: ");
                    String fullName = scanner.nextLine();

                    System.out.print("Введите дату приёма (ГГГГ-ММ-ДД): ");
                    Date hireDate = Date.valueOf(scanner.nextLine());

                    System.out.print("Введите зарплату: ");
                    double salary = Double.parseDouble(scanner.nextLine());

                    staffDAO.addDoctor(userId, fullName, hireDate, salary);
                }
                case "5" -> {
                    int count = staffDAO.getPatientCount();
                    System.out.println("🔢 Всего пациентов: " + count);
                }
                case "6" -> {
                    String maxStaff = staffDAO.getMaxSalaryStaff();
                    System.out.println("🚀 Максимальная зарплата: " + maxStaff);
                }
                case "7" -> {
                    String minStaff = staffDAO.getMinSalaryStaff();
                    System.out.println("🐢 Минимальная зарплата: " + minStaff);
                }
                case "0" -> {
                    System.out.println("✅ Выход из меню главврача.");
                    return;
                }
                default -> System.out.println("❌ Неверный ввод. Попробуйте снова.");
            }
        }
    }

}

