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
            String type = scanner.nextLine().trim();
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

            switch (role) {
                case "patient"      -> showPatientMenu(scanner);
                case "doctor"       -> showDoctorMenu(scanner);
                case "medassistant" -> showNurseMenu(scanner);
                case "maindoctor"   -> showMainDoctorMenu(scanner);
                default               -> System.out.println("❌ Неизвестная роль.");
            }
        }
        scanner.close();
    }

    private static void showPatientMenu(Scanner scanner) {
        PatientDAO patientDAO = new PatientDAO();
        int userId;
        while (true) {
            System.out.print("Введите ваш user_id: ");
            try {
                userId = Integer.parseInt(scanner.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println("❌ Введите корректный числовой ID.");
            }
        }
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
        DoctorDAO dao = new DoctorDAO();

        while (true) {
            System.out.println("\n--- Меню лечащего врача ---");
            System.out.println("1. Показать список пациентов");
            System.out.println("2. Написать поручение для медсестры");
            System.out.println("3. Показать не завершённые поручения");
            System.out.println("4. Показать завершённые поручения");
            System.out.println("5. Поиск пациента");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            String c = scanner.nextLine().trim();
            if ("0".equals(c)) break;
            switch (c) {
                case "1" -> dao.getCurrentPatients().forEach(p -> System.out.println("– " + p));
                case "2" -> {
                    int nid;
                    while (true) {
                        System.out.print("Введите nurse_id: ");
                        try { nid = Integer.parseInt(scanner.nextLine().trim()); break; }
                        catch (NumberFormatException e) { System.out.println("❌ Введите числовой ID."); }
                    }
                    System.out.print("Текст поручения: ");
                    String desc = scanner.nextLine().trim();
                    if (!desc.isEmpty()) dao.addNurseTask(nid, desc);
                    else System.out.println("❌ Описание не должно быть пустым.");
                }
                case "3" -> dao.getPendingNurseTasks().forEach(t -> System.out.println("– " + t));
                case "4" -> dao.getCompletedNurseTasks().forEach(t -> System.out.println("– " + t));
                case "5" -> {
                    System.out.print("Часть имени: ");
                    String part = scanner.nextLine().trim();
                    if (part.isEmpty()) { System.out.println("❌ Строка не должна быть пустой."); break; }
                    List<Integer> ids = dao.findPatientsByName(part);
                    if (ids.isEmpty()) { System.out.println("Пациенты не найдены."); break; }
                    int pid;
                    while (true) {
                        System.out.print("Введите ID пациента: ");
                        try { pid = Integer.parseInt(scanner.nextLine().trim()); break; }
                        catch (NumberFormatException e) { System.out.println("❌ Введите числовой ID."); }
                    }
                    while (true) {
                        System.out.println("1. Информация");
                        System.out.println("2. История болезни");
                        System.out.println("3. Добавить диагноз");
                        System.out.println("0. Назад");
                        System.out.print("Выбор: ");
                        String sub = scanner.nextLine().trim();
                        if ("0".equals(sub)) break;
                        switch (sub) {
                            case "1" -> dao.getPatientInfo(pid);
                            case "2" -> dao.getMedicalHistory(pid);
                            case "3" -> {
                                System.out.print("Введите диагноз: ");
                                String diag = scanner.nextLine().trim();
                                if (!diag.isEmpty()) dao.addDiagnosis(pid, diag);
                                else System.out.println("❌ Описание диагноза пустое.");
                            }
                            default -> System.out.println("❌ Неверный ввод.");
                        }
                    }
                }
                default -> System.out.println("❌ Неверный ввод.");
            }
        }
    }

    private static void showNurseMenu(Scanner scanner) {
        NurseDAO dao = new NurseDAO();
        int nurseId;
        while (true) {
            System.out.print("Введите nurse_id: ");
            try { nurseId = Integer.parseInt(scanner.nextLine().trim()); break; }
            catch (NumberFormatException e) { System.out.println("❌ Введите корректный числовой ID."); }
        }

        while (true) {
            System.out.println("\n--- Меню медсестры ---");
            System.out.println("1. Поиск пациента");
            System.out.println("2. Показать не завершённые поручения");
            System.out.println("3. Выполнить поручение");
            System.out.println("4. Показать завершённые поручения");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            String c = scanner.nextLine().trim();
            if ("0".equals(c)) break;
            switch (c) {
                case "1" -> {
                    System.out.print("Часть имени: ");
                    String part = scanner.nextLine().trim();
                    if (!part.isEmpty()) dao.findPatientsByName(part).forEach(p -> System.out.println("– " + p));
                    else System.out.println("❌ Строка не должна быть пустой.");
                }
                case "2" -> dao.getTasksForNurse(nurseId).forEach(t -> System.out.println("– " + t));
                case "3" -> {
                    int tid;
                    while (true) {
                        System.out.print("Введите ID поручения: ");
                        try { tid = Integer.parseInt(scanner.nextLine().trim()); break; }
                        catch (NumberFormatException e) { System.out.println("❌ Введите числовой ID."); }
                    }
                    dao.completeTask(tid);
                }
                case "4" -> dao.getCompletedTasksForNurse(nurseId).forEach(t -> System.out.println("– " + t));
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
                    staffDAO.deleteNurse(uid);
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