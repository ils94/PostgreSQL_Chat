package com.droidev.postgresqlchat;

import android.app.Activity;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class dbQueries {

    StringBuilder chat = new StringBuilder();

    public StringBuilder loadChat(Activity activity, Connection connection) {
        Thread thread = new Thread(() -> {

            try {

                Statement stmt;
                String sql = "SELECT * FROM CHAT ORDER BY ID ASC";

                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {

                    String user = rs.getString("CHAT_USER");
                    String conversation = rs.getString("CONVERSATION");

                    chat.append(user).append(": ").append(conversation).append("\n");
                }

            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
        }

        return chat;
    }

    public void insertIntoChat(Activity activity, Connection connection, String user, String conversation) {
        Thread thread = new Thread(() -> {

            try {

                Statement stmt;
                String sql = "INSERT INTO CHAT (CHAT_USER, CONVERSATION) VALUES ('" + user + "', '" + conversation + "')";

                stmt = connection.createStatement();
                stmt.executeUpdate(sql);

            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
        }
    }

    public void truncateDB(Activity activity, Connection connection) {
        Thread thread = new Thread(() -> {

            try {

                Statement stmt;
                String sql = "TRUNCATE TABLE CHAT";

                stmt = connection.createStatement();
                stmt.executeUpdate(sql);

            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
        }
    }
}
