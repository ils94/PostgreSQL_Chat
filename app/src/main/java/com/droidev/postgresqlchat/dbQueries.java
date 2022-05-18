package com.droidev.postgresqlchat;

import android.app.Activity;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class dbQueries {

    StringBuilder chat = new StringBuilder();

    public StringBuilder loadChat(Activity activity, Connection connection, String sql) {
        Thread thread = new Thread(() -> {

            try {

                Statement stmt;

                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {

                    String user_name = rs.getString("USER_NAME");
                    String user_message = rs.getString("USER_MESSAGE");

                    chat.append(user_name).append(": ").append(user_message).append("\n");
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

    public StringBuilder searchMessage(Activity activity, Connection connection, String string) {
        Thread thread = new Thread(() -> {

            try {

                PreparedStatement pst;

                String sql = "SELECT * FROM CHAT WHERE USER_NAME ILIKE ? OR USER_MESSAGE ILIKE ? ORDER BY ID ASC";

                pst = connection.prepareStatement(sql);

                pst.setString(1, "%" + string + "%");
                pst.setString(2, "%" + string + "%");

                ResultSet rs = pst.executeQuery();

                while (rs.next()) {

                    String user_name = rs.getString("USER_NAME");
                    String user_message = rs.getString("USER_MESSAGE");

                    chat.append(user_name).append(": ").append(user_message).append("\n");
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

    public void insertIntoChat(Activity activity, Connection connection, String user_name, String user_message) {
        Thread thread = new Thread(() -> {

            try {

                PreparedStatement pst;
                String sql = "INSERT INTO CHAT (USER_NAME, USER_MESSAGE) VALUES (?, ?)";

                pst = connection.prepareStatement(sql);
                pst.setString(1, user_name);
                pst.setString(2, user_message);
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
