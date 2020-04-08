/* 
This document contains the sql script for creating the
learning_journal database.
The script assumes MySQL is the DBMS being used.
*/

-- ----------------------
-- Create the database
-- ----------------------
CREATE DATABASE learning_journal;

use learning_journal;

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


