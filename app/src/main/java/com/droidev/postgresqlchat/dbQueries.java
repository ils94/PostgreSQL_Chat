package com.droidev.postgresqlchat;

import android.app.Activity;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

                    String user = rs.getString("USER_NAME");
                    String conversation = rs.getString("USER_MESSAGE");

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

    public void insertIntoChat(Activity activity, Connection connection, String user, String message) {
        Thread thread = new Thread(() -> {

            try {

                PreparedStatement pst;
                String sql = "INSERT INTO CHAT (USER_NAME, USER_MESSAGE) VALUES (?, ?)";

                pst = connection.prepareStatement(sql);
                pst.setString(1, user);
                pst.setString(2, message);
                pst.executeUpdate();

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
