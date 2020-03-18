package core; 

import gui.JournalsTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

// This object is used to access and manipulate data from the data source
// (in this case a MySQL database)
public class DataAccessObject {

    private static DataAccessObject instance; // the single instance of the DataAccessObject
    private Connection db; // the connection to the database

    // DAO constructor is private, DataAccessObject is a singleton
    private DataAccessObject() {
        try {
            // Allocate a database 'Connection' object
            db = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/learning_journal?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                    "root", "bunslow121");
        } catch (Exception e) {
            System.out.println("could not initialize the database");
            e.printStackTrace();
        }
    }

    public static DataAccessObject getInstance() {
        if (instance == null) {
            instance = new DataAccessObject();
        }
        return instance;
    }
    
    public enum SortJournalBy { NAME_ASC, NAME_DESC, DURATION_ASC, DURATION_DESC, ENTRIES_ASC, ENTRIES_DESC };

    // returns all journals stored in the system
    public ResultSet getJournals(SortJournalBy value) {
        ResultSet rs = null;
        String query =  "SELECT journals.id, journals.name, SUM(journalentries.duration) AS total_duration, " +
                        "COUNT(journalentries.id) AS num_entries " +
                        "FROM journals LEFT OUTER JOIN journalentries " +
                        "ON journals.id = journalentries.journal_id " +
                        "GROUP BY journals.id ";
        String orderBy = "";
        switch (value) {
            case NAME_ASC:
                orderBy = "journals.name ASC";
                break;
            case NAME_DESC:
                orderBy = "journals.name DESC";
                break;
            case DURATION_ASC:
                orderBy = "total_duration ASC";
                break;
            case DURATION_DESC:
                orderBy = "total_duration DESC";
                break;
            case ENTRIES_ASC:
                orderBy = "num_entries ASC";
                break;
            case ENTRIES_DESC:
                orderBy = "num_entries DESC";
                break;
        } 
        query += ("ORDER BY " + orderBy + ";");

        System.out.println(query);
        try {
            Statement statement = db.createStatement();
            rs = statement.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rs;
    }
    
    // creates a new journal with the given name
    public void createNewJournal(String journalName) {
        String query = "INSERT INTO journals(name) VALUES(\"" + journalName + "\");";
        try {
            Statement statement = db.createStatement();
            statement.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // deletes the journal with the given id
    public void deleteJournal(int journalID) {
        String query = "DELETE FROM journals WHERE id = " + journalID + ";";
        try {
            Statement statement = db.createStatement();
            statement.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}