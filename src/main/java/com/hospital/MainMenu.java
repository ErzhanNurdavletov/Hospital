package com.hospital;

import com.hospital.dao.*;
import com.hospital.service.AuthService;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class MainMenu {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== АИС Больница ===");
            System.out.println("Для выхода введите 0");
            System.out.print("Введите тип аккаунта (patient/doctor/medassistant/maindoctor): ");
            String type = scanner.nextLine().trim();
            if ("0".equals(type)) {
                System.out.println("Программа завершена. Спасибо за использование.");
                break;
            }

            System.out.print("Введите логин: ");
            String username = scanner.nextLine();
            System.out.print("Введите пароль: ");
            String password = scanner.nextLine();

            String role = AuthService.login(username, password);
            if (role == null || !role.equals(type)) {
                System.out.println("❌ Неверный тип аккаунта, логин или пароль. Попробуйте снова.");
                continue;
            }

            System.out.println("✅ Успешный вход. Ваша роль: " + role);
            switch (role) {
                case "patient" -> showPatientMenu(scanner);
                case "doctor" -> showDoctorMenu(scanner);
                case "medassistant" -> showNurseMenu(scanner);
                case "maindoctor" -> showMainDoctorMenu(scanner);
                default -> System.out.println("Неизвестная роль.");
            }
        }
        scanner.close();
    }

    private static void showPatientMenu(Scanner scanner) {
        PatientDAO patientDAO = new PatientDAO();
        System.out.print("Введите ваш user_id: ");
        int userId = Integer.parseInt(scanner.nextLine());
        int patientId = patientDAO.getPatientIdByUserId(userId);
        if (patientId == -1) {
            System.out.println("❌ Пациент с таким user_id не найден.");
            return;
        }
        while (true) {
            System.out.println("\n📋 Меню пациента:");
            System.out.println("1. Посмотреть личную информацию");
            System.out.println("2. Посмотреть историю болезни");
            System.out.println("3. Посмотреть даты лечения");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> patientDAO.showPersonalInfo(patientId);
                case "2" -> patientDAO.showMedicalHistory(patientId);
                case "3" -> patientDAO.showMedicalHistory(patientId);
                case "0" -> { return; }
                default -> System.out.println("❌ Неверный ввод.");
            }
        }
    }

    private static void showDoctorMenu(Scanner scanner) {
        DoctorDAO dao = new DoctorDAO();
        while (true) {
            System.out.println("\n📋 Меню лечащего врача:");
            System.out.println("1. Показать список пациентов");
            System.out.println("2. Показать количество пациентов");
            System.out.println("3. Показать поручения для медсестёр");
            System.out.println("4. Написать поручение для медсестры");
            System.out.println("5. Показать завершённые поручения");
            System.out.println("6. Поиск пациента");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> {
                    System.out.println("🩺 Список пациентов:");
                    dao.getCurrentPatients().forEach(p -> System.out.println("– " + p));
                }
                case "2" -> System.out.println("🔢 Количество пациентов: " + dao.getPatientCount());
                case "3" -> {
                    System.out.println("📖 Поручения для медсестёр:");
                    dao.getPendingNurseTasks().forEach(t -> System.out.println("– " + t));
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
                    dao.getCompletedNurseTasks().forEach(t -> System.out.println("– " + t));
                }
                case "6" -> {
                    System.out.print("Введите часть имени пациента: ");
                    String search = scanner.nextLine();
                    List<Integer> ids = dao.findPatientsByName(search);
                    if (ids.isEmpty()) {
                        System.out.println("Пациенты не найдены.");
                        break;
                    }
                    System.out.print("Введите ID пациента: ");
                    int pid = Integer.parseInt(scanner.nextLine());
                    while (true) {
                        System.out.println("\n🔍 Работа с пациентом ID=" + pid);
                        System.out.println("1. Информация");
                        System.out.println("2. История болезни");
                        System.out.println("3. Добавить диагноз");
                        System.out.println("0. Назад");
                        System.out.print("Выбор: ");
                        String sub = scanner.nextLine();
                        switch (sub) {
                            case "1" -> dao.getPatientInfo(pid);
                            case "2" -> dao.getMedicalHistory(pid);
                            case "3" -> {
                                System.out.print("Введите текст диагноза: ");
                                dao.addDiagnosis(pid, scanner.nextLine());
                            }
                            case "0" -> { break; }
                            default -> System.out.println("❌ Неверный ввод.");
                        }
                        if (sub.equals("0")) break;
                    }
                }
                case "0" -> { return; }
                default -> System.out.println("❌ Неверный ввод.");
            }
        }
    }

    private static void showNurseMenu(Scanner scanner) {
        NurseDAO nurseDAO = new NurseDAO();
        System.out.print("Введите ваш nurse_id: ");
        int nurseId = Integer.parseInt(scanner.nextLine());
        while (true) {
            System.out.println("\n📋 Меню медсестры:");
            System.out.println("1. Показать мои поручения");
            System.out.println("2. Выполнить поручение");
            System.out.println("3. Показать завершённые поручения");
            System.out.println("4. Найти пациента по имени");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> {
                    System.out.println("📌 Мои текущие поручения:");
                    var tasks = nurseDAO.getTasksForNurse(nurseId);
                    if (tasks.isEmpty()) System.out.println("Нет текущих поручений.");
                    else tasks.forEach(t -> System.out.println("– " + t));
                }
                case "2" -> {
                    System.out.print("Введите ID поручения для завершения: ");
                    nurseDAO.completeTask(Integer.parseInt(scanner.nextLine()));
                }
                case "3" -> {
                    System.out.println("✅ Завершённые поручения:");
                    var done = nurseDAO.getCompletedTasksForNurse(nurseId);
                    if (done.isEmpty()) System.out.println("Нет завершённых поручений.");
                    else done.forEach(t -> System.out.println("– " + t));
                }
                case "4" -> {
                    System.out.print("Введите часть имени пациента: ");
                    var results = nurseDAO.findPatientsByName(scanner.nextLine());
                    if (results.isEmpty()) System.out.println("Пациенты не найдены.");
                    else results.forEach(p -> System.out.println("– " + p));
                }
                case "0" -> { return; }
                default -> System.out.println("❌ Неверный ввод.");
            }
        }
    }

    private static void showMainDoctorMenu(Scanner scanner) {
        StaffDAO staffDAO = new StaffDAO();
        UserDAO userDAO = new UserDAO();
        PatientDAO patientDAO = new PatientDAO();

        while (true) {
            System.out.println("\n📋 Меню главврача:");
            System.out.println("1. Показать список медсестёр");
            System.out.println("2. Показать список лечащих врачей");
            System.out.println("----------------------------------");
            System.out.println("3. Добавить пациента");
            System.out.println("4. Добавить медсестру");
            System.out.println("5. Добавить лечащего врача");
            System.out.println("----------------------------------");
            System.out.println("6. Показать количество пациентов");
            System.out.println("7. Сотрудник с максимальной зарплатой");
            System.out.println("8. Сотрудник с минимальной зарплатой");
            System.out.println("----------------------------------");
            System.out.println("9. Удалить медсестру");
            System.out.println("10. Удалить врача");
            System.out.println("11. Удалить пациента");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> staffDAO.showNurses();
                case "2" -> staffDAO.showDoctors();
                case "3" -> {
                    System.out.print("Введите логин для пациента: ");
                    String login = scanner.nextLine().trim();
                    System.out.print("Введите пароль: ");
                    String pwd = scanner.nextLine().trim();
                    if (login.isEmpty() || pwd.isEmpty() || login.contains(" ") || pwd.contains(" ")) {
                        System.out.println("Некорректный логин или пароль, попробуйте снова");
                        continue;
                    }

                    try {
                        System.out.print("ФИО пациента: ");
                        String fn = scanner.nextLine().trim();
                        if (fn.isEmpty()) {
                            throw new IllegalArgumentException("Некорректное ФИО");
                        }
                        System.out.print("Дата рождения (ГГГГ-ММ-ДД): ");
                        Date bd = Date.valueOf(scanner.nextLine());
                        System.out.print("Рост (см): ");
                        int height = Integer.parseInt(scanner.nextLine());
                        if (height <= 0) {
                            throw new IllegalArgumentException("Рост не может быть отрицательным или ноль");
                        }
                        System.out.print("Вес (кг): ");
                        int weight = Integer.parseInt(scanner.nextLine());
                        if (weight <= 0) {
                            throw new IllegalArgumentException("Вес не может быть отрицательным или ноль");
                        }
                        System.out.print("Группа крови (0, A, B, AB): ");
                        String bg = scanner.nextLine().trim().toUpperCase();
                        if (!bg.equals("A") && !bg.equals("B") && !bg.equals("0") && !bg.equals("AB")) {
                            throw new IllegalArgumentException("Некорректный формат группые крови");
                        }
                        System.out.print("Резус (+ или -): ");
                        String res = scanner.nextLine().trim();
                        if (!res.equals("+") && !res.equals("-")) {
                            throw new IllegalArgumentException("Некорректный формат резус фактора");
                        }
                        int uid = userDAO.createUser(login, pwd, "patient");
                        if (uid < 0) {
                            throw new IllegalArgumentException("Такой логин уже существует");
                        }
                        patientDAO.createPatient(uid, fn, bd, height, weight, bg, res);
                    } catch (Exception e) {
                        if (e.getMessage() == null) System.out.println("Некорректный формат даты");
                        else System.out.println(e.getMessage());
                    }

                }
                case "4" -> {
                    System.out.print("Введите логин для медсестры: ");
                    String login = scanner.nextLine().trim();
                    System.out.print("Введите пароль: ");
                    String pwd = scanner.nextLine().trim();
                    if (login.isEmpty() || login.isEmpty() || login.contains(" ") || pwd.contains(" ")) {
                        System.out.println("Некорректный логин или пароль, попробуйте снова");
                        continue;
                    }
                    // Добавление записи в nurses
                    try {
                        System.out.print("ФИО: ");
                        String fn = scanner.nextLine().trim();
                        if (fn.isEmpty()) {
                            throw new IllegalArgumentException("Некорректное ФИО");
                        }
                        System.out.print("Дата приёма (ГГГГ-ММ-ДД): ");
                        Date hd = Date.valueOf(scanner.nextLine().trim());
                        System.out.print("Зарплата: ");
                        double sal = Double.parseDouble(scanner.nextLine());
                        if (sal <= 0) {
                            throw new IllegalArgumentException("Зарплата не может быть отрицательной или ноль");
                        }
                        int uid = userDAO.createUser(login, pwd, "medassistant");
                        if (uid < 0) {
                            throw new IllegalArgumentException("Такой логин уже существует");
                        }
                        staffDAO.addNurse(uid, fn, hd, sal);
                    } catch (Exception e) {
                        System.out.println("❌ Неверный ввод данных о сотруднике.");
                    }
                }
                case "5" -> {
                    System.out.print("Введите логин для доктора: ");
                    String login = scanner.nextLine().trim();
                    System.out.print("Введите пароль: ");
                    String pwd = scanner.nextLine().trim();
                    if (login.isEmpty() || login.isEmpty() || login.contains(" ") || pwd.contains(" ")) {
                        System.out.println("Некорректный логин или пароль, попробуйте снова");
                        continue;
                    }
                    // Добавление записи в doctors
                    try {
                        System.out.print("ФИО: ");
                        String fn = scanner.nextLine().trim();
                        if (fn.isEmpty()) {
                            throw new IllegalArgumentException("Некорректное ФИО");
                        }
                        System.out.print("Дата приёма (ГГГГ-ММ-ДД): ");
                        Date hd = Date.valueOf(scanner.nextLine().trim());
                        System.out.print("Зарплата: ");
                        double sal = Double.parseDouble(scanner.nextLine());
                        if (sal <= 0) {
                            throw new IllegalArgumentException("Зарплата не может быть отрицательной или ноль");
                        }
                        int uid = userDAO.createUser(login, pwd, "doctor");
                        if (uid < 0) {
                            throw new IllegalArgumentException("Такой логин уже существует");
                        }
                        staffDAO.addDoctor(uid, fn, hd, sal);
                    } catch (Exception e) {
                        System.out.println("❌ Неверный ввод данных о сотруднике.");
                    }
                }
                case "6" -> System.out.println("🔢 Всего пациентов: " + staffDAO.getPatientCount());
                case "7" -> System.out.println("🚀 Макс. зарплата: " + staffDAO.getMaxSalaryStaff());
                case "8" -> System.out.println("🐢 Мин. зарплата: " + staffDAO.getMinSalaryStaff());
                case "9" -> {
                    try {
                        System.out.print("Введите user_id медсестры для удаления: ");
                        int uid = Integer.parseInt(scanner.nextLine());
                        staffDAO.deleteNurse(uid);
                    } catch (Exception e) {
                        System.out.println("Некорректный user_id");
                    }
                }
                case "10" -> {
                    try {
                        System.out.print("Введите user_id врача для удаления: ");
                        int uid = Integer.parseInt(scanner.nextLine());
                        staffDAO.deleteDoctor(uid);
                    } catch (Exception e) {
                        System.out.println("Некорректный user_id");
                    }
                }
                case "11" -> {
                    try {
                        System.out.print("Введите user_id пациента для удаления: ");
                        int uid = Integer.parseInt(scanner.nextLine());
                        patientDAO.deletePatient(uid);
                    } catch (Exception e) {
                        System.out.println("Некорректный user_id");
                    }
                }

                case "0" -> {
                    return;
                }
                default -> System.out.println("❌ Неверный ввод.");
            }
        }
    }
}
