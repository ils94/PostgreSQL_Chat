# PostgreSQL_Chat
A simple chat where anyone can setup a PostgreSQL Database to store conversations and talk to anyone who has access to the Database

# How to setup the database strucutre

create a table with this command:

CREATE TABLE IF NOT EXISTS CHAT (ID SERIAL PRIMARY KEY, CHAT_USER VARCHAR(10) NOT NULL, CONVERSATION VARCHAR NOT NULL)
