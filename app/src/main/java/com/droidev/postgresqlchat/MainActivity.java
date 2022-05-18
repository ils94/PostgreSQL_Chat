package com.droidev.postgresqlchat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {

    private TextView chat;
    private EditText textToSend;
    private Button send;
    private Boolean confirm = false, autoScroll = true;
    Menu menuItem;

    private TinyDB tinyDB;

    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri uri = getIntent().getData();

        if (uri != null) {

            String path = uri.toString();

            Toast.makeText(this, path, Toast.LENGTH_SHORT).show();

            deepLink(path.replace("https://psqlchat.go/", ""));
        }

        tinyDB = new TinyDB(this);

        chat = findViewById(R.id.chat);
        textToSend = findViewById(R.id.textToSend);
        send = findViewById(R.id.send);

        textToSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                send.setEnabled(s.toString().trim().length() != 0);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                send.setEnabled(s.toString().trim().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
                send.setEnabled(s.toString().trim().length() != 0);
            }
        });

        textToSend.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND) {

                sendText();

                return true;
            }

            return false;
        });

        chat.setMovementMethod(new ScrollingMovementMethod());

        send.setOnClickListener(v -> sendText());

        send.setEnabled(false);

        if (!tinyDB.getString("textSize").isEmpty()) {

            chat.setTextSize(TypedValue.COMPLEX_UNIT_SP, Integer.parseInt(tinyDB.getString("textSize")));
        }

        makeConnection();

        loadChatHandlerLoop();
    }

    @Override
    public void onBackPressed() {
        if (confirm) {
            Handler handler = new Handler();
            handler.removeCallbacksAndMessages(null);
            finish();
        } else {
            Toast.makeText(this, "Press back again to exit app.",
                    Toast.LENGTH_SHORT).show();
            confirm = true;
            new Handler().postDelayed(() -> confirm = false, 3 * 1000);
        }
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.pauseResumeChatLoop:

                pauseResumeChatLoop();

                break;

            case R.id.login:

                login();

                break;

            case R.id.shareLink:

                if (tinyDB.getString("dbName").isEmpty()) {

                    Toast.makeText(this, "There are no credentials saved yet.", Toast.LENGTH_SHORT).show();
                } else {

                    String link = "https://psqlchat.go/"
                            + tinyDB.getString("dbName")
                            + "/" + tinyDB.getString("dbUser")
                            + "/" + tinyDB.getString("dbPass")
                            + "/" + tinyDB.getString("dbHost")
                            + "/" + tinyDB.getString("dbPort");

                    Intent shareLinkIntent = new Intent(Intent.ACTION_SEND);
                    shareLinkIntent.setType("text/plain");
                    shareLinkIntent.putExtra(Intent.EXTRA_TEXT, link);
                    startActivity(Intent.createChooser(shareLinkIntent, "Share link to..."));
                }

                break;

            case R.id.changeTextSize:

                changeTextSize();

                break;

            case R.id.showAllMessages:

                showAllMessages();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        menuItem = menu;

        return super.onCreateOptionsMenu(menu);
    }

    private void makeConnection() {

        new Thread(() -> {
            try {

                String dbHost = tinyDB.getString("dbHost");
                String dbPort = tinyDB.getString("dbPort");
                String dbName = tinyDB.getString("dbName");
                String dbUser = tinyDB.getString("dbUser");
                String dbPass = tinyDB.getString("dbPass");

                String url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;

                if (!dbName.isEmpty()) {

                    connection = DriverManager.getConnection(url, dbUser, dbPass);
                }

            } catch (SQLException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadChat() {

        try {

            if (connection.isClosed() || connection == null) {

                makeConnection();

            } else {

                dbQueries db = new dbQueries();

                StringBuilder dbLoad = db.loadChat(MainActivity.this, connection, "SELECT * FROM CHAT ORDER BY ID ASC LIMIT 1000");

                chat.setText(dbLoad);

                if (autoScroll) {

                    chat.append(" ");
                }
            }
        } catch (Exception e) {

            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadChatHandlerLoop() {

        final Handler handler = new Handler();
        final int delay = 3000;

        handler.postDelayed(new Runnable() {
            public void run() {

                if (!(connection == null)) {

                    try {
                        if (!connection.isClosed() && autoScroll) {

                            loadChat();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void sendText() {

        if (!textToSend.getText().toString().isEmpty()) {

            dbQueries db = new dbQueries();

            db.insertIntoChat(MainActivity.this, connection, tinyDB.getString("user"), textToSend.getText().toString());

            textToSend.setText("");

            resumeChatLoop();

            chat.append(" ");

            send.setEnabled(false);

            loadChat();

        } else {

            Toast.makeText(this, "Message cannot be empty.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void login() {

        EditText user = new EditText(this);
        user.setHint("User Name");
        user.setInputType(InputType.TYPE_CLASS_TEXT);
        user.setMaxLines(1);

        EditText dbName = new EditText(this);
        dbName.setHint("dbName");
        dbName.setInputType(InputType.TYPE_CLASS_TEXT);
        dbName.setMaxLines(1);

        EditText dbUser = new EditText(this);
        dbUser.setHint("dbUser");
        dbUser.setInputType(InputType.TYPE_CLASS_TEXT);
        dbUser.setMaxLines(1);

        EditText dbPass = new EditText(this);
        dbPass.setHint("dbPass");
        dbPass.setInputType(InputType.TYPE_CLASS_TEXT);
        dbPass.setMaxLines(1);

        EditText dbHost = new EditText(this);
        dbHost.setHint("dbHost");
        dbHost.setInputType(InputType.TYPE_CLASS_TEXT);
        dbHost.setMaxLines(1);

        EditText dbPort = new EditText(this);
        dbPort.setHint("dbPort");
        dbPort.setInputType(InputType.TYPE_CLASS_NUMBER);
        dbPort.setMaxLines(1);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(user);
        lay.addView(dbName);
        lay.addView(dbUser);
        lay.addView(dbPass);
        lay.addView(dbHost);
        lay.addView(dbPort);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Login")
                .setMessage("Insert user name and database credentials below:")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Clear all", null)
                .setView(lay)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        user.setText(tinyDB.getString("user"));
        dbName.setText(tinyDB.getString("dbName"));
        dbUser.setText(tinyDB.getString("dbUser"));
        dbPass.setText(tinyDB.getString("dbPass"));
        dbHost.setText(tinyDB.getString("dbHost"));
        dbPort.setText(tinyDB.getString("dbPort"));

        if (dbPort.getText().toString().isEmpty()) {

            dbPort.setText("5432");
        }

        positiveButton.setOnClickListener(v -> {

            if (user.getText().toString().isEmpty() || dbName.getText().toString().isEmpty() || dbUser.getText().toString().isEmpty() || dbPass.getText().toString().isEmpty() || dbHost.getText().toString().isEmpty() || dbPort.getText().toString().isEmpty()) {

                Toast.makeText(MainActivity.this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show();

            } else if (user.getText().toString().length() > 10) {

                Toast.makeText(MainActivity.this, "User name cannot be bigger than 10 characters.", Toast.LENGTH_SHORT).show();
            } else {

                clearTinyDBKeys();

                tinyDB.putString("user", user.getText().toString());
                tinyDB.putString("dbName", dbName.getText().toString());
                tinyDB.putString("dbUser", dbUser.getText().toString());
                tinyDB.putString("dbPass", dbPass.getText().toString());
                tinyDB.putString("dbHost", dbHost.getText().toString());
                tinyDB.putString("dbPort", dbPort.getText().toString());

                dialog.dismiss();

                Toast.makeText(MainActivity.this, "Saved.", Toast.LENGTH_SHORT).show();

                new Thread(() -> {

                    try {

                        if (!(connection == null)) {

                            connection.close();
                        }

                        makeConnection();
                    } catch (SQLException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }
        });

        neutralButton.setOnClickListener(v -> {

            dbName.setText("");
            dbUser.setText("");
            dbPass.setText("");
            dbHost.setText("");
        });
    }

    private void changeTextSize() {

        float scaledDensity = MainActivity.this.getResources().getDisplayMetrics().scaledDensity;
        float sp = chat.getTextSize() / scaledDensity;

        EditText textSize = new EditText(this);
        textSize.setHint("Current size is: " + sp);
        textSize.setInputType(InputType.TYPE_CLASS_NUMBER);
        textSize.setMaxLines(1);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(textSize);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Adjust Text Size")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .setView(lay)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            try {

                chat.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(textSize.getText().toString()));

                TinyDB tinyDB = new TinyDB(MainActivity.this);

                tinyDB.remove("textSize");
                tinyDB.putString("textSize", textSize.getText().toString());

                dialog.dismiss();

            } catch (Exception e) {

                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearTinyDBKeys() {

        tinyDB.remove("user");
        tinyDB.remove("dbName");
        tinyDB.remove("dbUser");
        tinyDB.remove("dbPass");
        tinyDB.remove("dbHost");
        tinyDB.remove("dbPort");
    }

    public void deepLink(String link) {

        EditText userName = new EditText(this);
        userName.setHint("Insert your user name here");
        userName.setInputType(InputType.TYPE_CLASS_TEXT);
        userName.setMaxLines(1);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(userName);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Login from Deeplink")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .setView(lay)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            if (userName.getText().toString().isEmpty()) {

                Toast.makeText(this, "User name cannot be empty.", Toast.LENGTH_SHORT).show();

            } else if (userName.getText().toString().length() > 10) {

                Toast.makeText(MainActivity.this, "User name cannot be bigger than 10 characters.", Toast.LENGTH_SHORT).show();
            } else {

                String[] linkArray = link.split("/");

                clearTinyDBKeys();

                tinyDB.putString("user", userName.getText().toString());
                tinyDB.putString("dbName", linkArray[0]);
                tinyDB.putString("dbUser", linkArray[1]);
                tinyDB.putString("dbPass", linkArray[2]);
                tinyDB.putString("dbHost", linkArray[3]);
                tinyDB.putString("dbPort", linkArray[4]);

                dialog.dismiss();

                Toast.makeText(MainActivity.this, "Saved.", Toast.LENGTH_SHORT).show();

                new Thread(() -> {

                    try {

                        if (!(connection == null)) {

                            connection.close();
                        }

                        makeConnection();
                    } catch (SQLException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }
        });
    }

    private void showAllMessages() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Show All Messages")
                .setMessage("You are about to load all messages from the database, proceed?")
                .setPositiveButton("Yes", null)
                .setNegativeButton("Cancel", null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            try {

                if (connection.isClosed() || connection == null) {

                    makeConnection();

                } else {

                    dbQueries db = new dbQueries();

                    StringBuilder dbLoad = db.loadChat(MainActivity.this, connection, "SELECT * FROM CHAT ORDER BY ID ASC");

                    Intent intent = new Intent(this, ShowAllMessagesActivity.class);
                    intent.putExtra("chat", dbLoad.toString());
                    startActivity(intent);
                }
            } catch (Exception e) {

                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });
    }

    private void pauseResumeChatLoop() {

        if (autoScroll) {

            Toast.makeText(MainActivity.this, "Chat loop paused.", Toast.LENGTH_SHORT).show();

            autoScroll = false;

            menuItem.findItem(R.id.pauseResumeChatLoop).setIcon(R.drawable.play);
        } else {

            Toast.makeText(MainActivity.this, "Chat loop resumed.", Toast.LENGTH_SHORT).show();

            autoScroll = true;

            menuItem.findItem(R.id.pauseResumeChatLoop).setIcon(R.drawable.pause);
        }
    }

    private void resumeChatLoop() {

        if (!autoScroll) {

            Toast.makeText(MainActivity.this, "Chat loop resumed.", Toast.LENGTH_SHORT).show();

            autoScroll = true;

            menuItem.findItem(R.id.pauseResumeChatLoop).setIcon(R.drawable.pause);
        }

    }
}