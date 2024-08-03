/*
 Sample SQL file for describing a database and its table schemas
 */

-- Create the DB
CREATE DATABASE SampleDatabase;

 -- Account
CREATE TABLE Accounts (
    ID              uuid PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    Account_Type    CITEXT NOT NULL,
    First_Name      CITEXT NOT NULL,
    Last_Name       CITEXT NOT NULL,
    Email           CITEXT NOT NULL,
    DOB             DATE NOT NULL,
    Client          uuid NULL,
    Roles           INTEGER,
    Created         TIMESTAMP NOT NULL DEFAULT (now() at time zone 'utc'),
    Updated         TIMESTAMP NOT NULL DEFAULT (now() at time zone 'utc'),
    FOREIGN KEY (Account_Type)  REFERENCES AccountTypes(ID),
    FOREIGN KEY (Roles)         REFERENCES Role_FK_Arrays(ID),
    FOREIGN KEY (Client)        REFERENCES Clients(ID)
);

-- Account type definitions
CREATE TABLE AccountTypes (
    ID          SERIAL PRIMARY KEY NOT NULL,
    Name        CITEXT NOT NULL,
    Description CITEXT NOT NULL
);

-- Messages
CREATE TABLE Messages (
    ID          SERIAL PRIMARY KEY NOT NULL,
    Account_ID  uuid NOT NULL,
    Message     CITEXT NOT NULL,
    Created     TIMESTAMP NOT NULL DEFAULT (now() at time zone 'utc'),
    FOREIGN KEY (Account_ID) REFERENCES Accounts(ID)
);

-- Role definitions
CREATE TABLE Roles (
    ID      SERIAL PRIMARY KEY NOT NULL,
    Name    CITEXT NOT NULL,
    Grants  INTEGER[] NOT NULL
);

/*
Array of foreign keys to defined roles. This let's us have a single foreign key on an account that references
many roles; ultimately letting us have an array of foreign keys.
 */
CREATE TABLE Role_FK_Arrays (
    ID          SERIAL PRIMARY KEY,
    ROLE_IDS    INTEGER[] NOT NULL
);

-- Grant definitions
CREATE TABLE Grants (
    ID      SERIAL PRIMARY KEY NOT NULL,
    Name    CITEXT NOT NULL
);

-- Client information
CREATE TABLE Clients (
    ID      uuid PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    Name    CITEXT NOT NULL
)