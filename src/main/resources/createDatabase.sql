/* 
This document contains all of the sql scripts necessary to
create the learning_journal database from scratch. 
Execute the sql scripts in order from top to bottom i.e. first
create the database, then create the two tables and finally
define the foreign key.
These sql scripts assume MySQL is the DBMS being used.
*/

-- ----------------------
-- Create the database
-- ----------------------
CREATE DATABASE learning_journal;

-- ----------------------
-- Create Journals table
-- ----------------------
CREATE TABLE Journals
(
  id		int		NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name		char(255)  	NOT NULL
);

-- -----------------------
-- Create JournalEntries table
-- -----------------------
CREATE TABLE JournalEntries
(
  id		int		NOT NULL AUTO_INCREMENT PRIMARY KEY,
  journal_id	int  		NOT NULL,
  date 		Date		NOT NULL,
  duration	int		NOT NULL,
  entry		TEXT		NOT NULL
);

-- -------------------
-- Define foreign keys
-- -------------------
ALTER TABLE JournalEntries ADD CONSTRAINT FK_Journals_JournalEntries FOREIGN KEY (journal_id) REFERENCES Journals (id) ON DELETE CASCADE;


