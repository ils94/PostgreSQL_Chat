package com.droidev.postgresqlchat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Connection;

public class ShowAllMessagesActivity extends AppCompatActivity {

    TextView chat;

    TinyDB tinyDB;

    Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_messages);

        connection = MainActivity.connection;

        setTitle("Showing all Messages");

        tinyDB = new TinyDB(this);

        chat = findViewById(R.id.allMessages);

        chat.setMovementMethod(new ScrollingMovementMethod());

        chat.setText(getIntent().getStringExtra("chat"));

        if (!tinyDB.getString("textSize").isEmpty()) {

            chat.setTextSize(TypedValue.COMPLEX_UNIT_SP, Integer.parseInt(tinyDB.getString("textSize")));
        }
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.searchMessage) {
            EditText search = new EditText(this);
            search.setInputType(InputType.TYPE_CLASS_TEXT);
            search.setMaxLines(1);

            LinearLayout lay = new LinearLayout(this);
            lay.setOrientation(LinearLayout.VERTICAL);
            lay.addView(search);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Search in the Database")
                    .setPositiveButton("Search", null)
                    .setNegativeButton("Cancel", null)
                    .setView(lay)
                    .show();

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(v -> {

                dbQueries db = new dbQueries();

                StringBuilder dbLoad = db.searchMessage(ShowAllMessagesActivity.this, connection, search.getText().toString());

                chat.setText(dbLoad);

                dialog.dismiss();
            });
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_show_all_messages_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}