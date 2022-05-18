# PostgreSQL_Chat

A simple chat where anyone can setup a PostgreSQL Database to store messages.

# How to setup the database strucutre

create a table with this command:

CREATE TABLE IF NOT EXISTS CHAT (ID SERIAL PRIMARY KEY, USER_NAME VARCHAR(10) NOT NULL, USER_MESSAGE VARCHAR NOT NULL)

By default, only the last 1000 rows will be displayed. The reason is that once the database gets bigger and bigger, more and more rows will be selected and displayed in the client, and thus, it may decrease performance overtime. There is a way to select all messages from the db using the option in the app menu.
