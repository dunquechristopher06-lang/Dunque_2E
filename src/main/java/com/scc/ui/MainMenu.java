package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Optional;
import java.util.Scanner;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== Equipment Borrowing & Tracking System ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            switch (prompt("Choose an option: ")) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please enter 1, 2 or 3.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- Register Account ---");
        String username = prompt("Username: ");
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("That username is already taken.");
            return;
        }
        String password = prompt("Password: ");
        if (password.length() < 4) {
            System.out.println("Password must be at least 4 characters.");
            return;
        }
        String role = chooseRole();
        if (role == null) {
            return;
        }
        if (userDao.register(username, password, role)) {
            System.out.println("Account created as " + role + " (status: PENDING).");
        } else {
            System.out.println("That username is already taken.");
        }
    }

    private String chooseRole() {
        System.out.println("Select role:");
        System.out.println("1. TEACHER");
        System.out.println("2. STUDENT");
        return switch (prompt("Role: ")) {
            case "1" -> "TEACHER";
            case "2" -> "STUDENT";
            default -> {
                System.out.println("Invalid role. Registration cancelled.");
                yield null;
            }
        };
    }

    private void login() {
        System.out.println("\n--- Login ---");
        String username = prompt("Username: ");
        String password = prompt("Password: ");
        Optional<User> user = userDao.login(username, password);
        if (user.isPresent()) {
            sessionMenu(user.get());
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private void sessionMenu(User user) {
        System.out.println("\nWelcome, " + user.username() + "! Role: " + user.roles()
                + " | Status: " + user.status());
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- Session Menu ---");
            System.out.println("1. Logout");
            if (prompt("Choose an option: ").equals("1")) {
                System.out.println("Logged out. Returning to main menu.");
                loggedIn = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private String prompt(String label) {
        System.out.print(label);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "3";
    }
}