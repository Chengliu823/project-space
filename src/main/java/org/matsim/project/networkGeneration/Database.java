package org.matsim.project.networkGeneration;

import org.junit.jupiter.api.Test;

import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class Database {


    public static void createNewTable() {
        // SQLite connection string
        String url = "jdbc:sqlite:E:\\TU_Berlin\\Masterarbeit\\project-space\\lib\\tests.db";

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS Linkinfo (\n"
                + "	linkid integer PRIMARY KEY NOT NULL ,\n"
                + "	freespeed100 real,\n"
                + "	freespeed110 real\n"
                + " freespeed90 real\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insert(){

    }

    public static void main(String[] args) {
        createNewTable();
        insert();
    }

}
