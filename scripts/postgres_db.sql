/*
 Sample SQL file for describing a database and its table schemas
 */

-- Create the DB
CREATE DATABASE sampledatabase;

-- Connect to DB
\c sampledatabase

-- Uncomment to create a user with all privileges on DB sampledatabase
/*
CREATE USER sampleuser WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE sampledatabase TO sampleuser;
 */

-- Create CITEXT extension
CREATE EXTENSION citext;

-- NOTE: Ordering of table creation is specific

-- Account type definitions
CREATE TABLE AccountTypes (
    ID          SERIAL PRIMARY KEY NOT NULL,
    Name        CITEXT NOT NULL,
    Description CITEXT NOT NULL
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
    ID          SERIAL PRIMARY KEY NOT NULL,
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
);

-- Account
CREATE TABLE Accounts (
    ID              uuid PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    Account_Type    INTEGER NOT NULL,
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

-- Messages
CREATE TABLE Messages (
    ID          SERIAL PRIMARY KEY NOT NULL,
    Account_ID  uuid NOT NULL,
    Message     CITEXT NOT NULL,
    Created     TIMESTAMP NOT NULL DEFAULT (now() at time zone 'utc'),
    FOREIGN KEY (Account_ID) REFERENCES Accounts(ID)
);

-- INSERT SAMPLE DATA

-- AccountTypes data
INSERT INTO AccountTypes (Name, Description) VALUES
('Admin', 'Administrator with full access'),
('User', 'Regular user with limited access'),
('Guest', 'Guest user with minimal access');

-- Roles data
INSERT INTO Roles (Name, Grants) VALUES
('Manager', '{1, 2}'),
('Employee', '{2}'),
('Intern', '{3}');

-- Grants data
INSERT INTO Grants (Name) VALUES
('Read Access'),
('Write Access'),
('Execute Access');

-- Clients data
INSERT INTO Clients (ID, Name) VALUES
(gen_random_uuid(), 'Client A'),
(gen_random_uuid(), 'Client B'),
(gen_random_uuid(), 'Client C');

-- Role_FK_Arrays data
INSERT INTO Role_FK_Arrays (ROLE_IDS) VALUES
('{1, 2}'),
('{2, 3}'),
('{1, 3}');

-- Accounts data
INSERT INTO Accounts (Account_Type, First_Name, Last_Name, Email, DOB, Client, Roles) VALUES
(1, 'John', 'Doe', 'john.doe@example.com', '1980-01-01', (SELECT ID FROM Clients LIMIT 1 OFFSET 0), 1),
(2, 'Jane', 'Smith', 'jane.smith@example.com', '1990-02-01', (SELECT ID FROM Clients LIMIT 1 OFFSET 1), 2),
(3, 'Mike', 'Brown', 'mike.brown@example.com', '1985-03-01', (SELECT ID FROM Clients LIMIT 1 OFFSET 2), 3);

-- Messages data
INSERT INTO Messages (Account_ID, Message) VALUES
((SELECT ID FROM Accounts LIMIT 1 OFFSET 0), 'Hello from John!'),
((SELECT ID FROM Accounts LIMIT 1 OFFSET 1), 'Hello from Jane!'),
((SELECT ID FROM Accounts LIMIT 1 OFFSET 2), 'Hello from Mike!');