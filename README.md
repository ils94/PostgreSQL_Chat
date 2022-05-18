# PostgreSQL_Chat

A simple chat where anyone can setup a PostgreSQL Database to store messages.

# How to setup the database strucutre

create a table with this command:

CREATE TABLE IF NOT EXISTS CHAT (ID SERIAL PRIMARY KEY, USER_NAME VARCHAR(10) NOT NULL, USER_MESSAGE VARCHAR NOT NULL)
