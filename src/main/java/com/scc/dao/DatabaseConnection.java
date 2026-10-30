package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class DatabaseConnection {

    private static final String URL = System.getProperty("db.url", "jdbc:sqlite:equipment.db");

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    public static void initialize() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            for (String sql : SCHEMA) {
                st.execute(sql);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialize database: " + e.getMessage(), e);
        }
    }

    private static final List<String> SCHEMA = List.of(
            """
            CREATE TABLE IF NOT EXISTS users (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                name           TEXT,
                username       TEXT NOT NULL UNIQUE,
                password       TEXT NOT NULL,
                roles          TEXT NOT NULL CHECK (roles IN
                                   ('ADMIN', 'CUSTODIAN', 'TECHNICIAN', 'TEACHER', 'STUDENT')),
                contact_number TEXT,
                status         TEXT NOT NULL DEFAULT 'PENDING'
                                   CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE'))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS equipment (
                equipment_id        INTEGER PRIMARY KEY AUTOINCREMENT,
                equipment_name      TEXT NOT NULL,
                category            TEXT NOT NULL,
                description         TEXT,
                quantity            INTEGER NOT NULL DEFAULT 1 CHECK (quantity >= 0),
                condition           TEXT NOT NULL DEFAULT 'GOOD'
                                        CHECK (condition IN ('GOOD', 'FAIR', 'DAMAGED', 'OUT_OF_ORDER')),
                availability_status TEXT NOT NULL DEFAULT 'AVAILABLE'
                                        CHECK (availability_status IN
                                            ('AVAILABLE', 'BORROWED', 'UNDER_REPAIR', 'RETIRED'))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS borrowing_transactions (
                transaction_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id              INTEGER NOT NULL REFERENCES users(id),
                equipment_id         INTEGER NOT NULL REFERENCES equipment(equipment_id),
                borrow_date          TEXT NOT NULL,
                expected_return_date TEXT NOT NULL,
                actual_return_date   TEXT,
                quantity             INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
                status               TEXT NOT NULL DEFAULT 'PENDING'
                                         CHECK (status IN
                                             ('PENDING', 'BORROWED', 'RETURNED', 'OVERDUE', 'CANCELLED'))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS equipment_logs (
                log_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                equipment_id INTEGER REFERENCES equipment(equipment_id),
                user_id      INTEGER REFERENCES users(id),
                action       TEXT NOT NULL,
                action_date  TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
                remarks      TEXT
            )
            """
    );
}