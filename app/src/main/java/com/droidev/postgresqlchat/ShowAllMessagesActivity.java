package com.droidev.postgresqlchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.widget.TextView;

public class ShowAllMessagesActivity extends AppCompatActivity {

    TextView chat;

    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_messages);

        setTitle("Showing all Messages");

        tinyDB = new TinyDB(this);

        chat = findViewById(R.id.allMessages);

        chat.setMovementMethod(new ScrollingMovementMethod());

        chat.setText(getIntent().getStringExtra("chat"));

        if (!tinyDB.getString("textSize").isEmpty()) {

            chat.setTextSize(TypedValue.COMPLEX_UNIT_SP, Integer.parseInt(tinyDB.getString("textSize")));
        }
    }
}