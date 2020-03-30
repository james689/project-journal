package core; 

import gui.JournalDataChangeListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// This object is used to access and manipulate data from the data source
// (in this case a MySQL database)
public class DataAccessObject {

    private static DataAccessObject instance; // the single instance of the DataAccessObject
    private Connection db; // the connection to the database
    private List<JournalDataChangeListener> journalDataChangeListeners; // listeners that
    // will be notified when journal data is changed, such as when a new entry is added to
    // a journal or an entry is deleted from a journal etc.

    // DAO constructor is private, DataAccessObject is a singleton
    private DataAccessObject() {
        journalDataChangeListeners = new ArrayList<>();
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
    
    public void addJournalDataChangeListener(JournalDataChangeListener listener) {
        journalDataChangeListeners.add(listener);
    }
    
    private ResultSet executeQuery(String query) {
        ResultSet rs = null;
        try {
            Statement statement = db.createStatement();
            rs = statement.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }
    
    private void executeUpdate(String query) {
        try {
            Statement statement = db.createStatement();
            statement.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public enum SortJournalBy { NAME_ASC, NAME_DESC, DURATION_ASC, DURATION_DESC, ENTRIES_ASC, ENTRIES_DESC };

    // returns all journals stored in the system
    public ResultSet getJournals(SortJournalBy value) {
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
        return executeQuery(query);
    }
    
    // creates a new journal with the given name
    public void createNewJournal(String journalName) {
        String query = "INSERT INTO journals(name) VALUES(\"" + journalName + "\");";
        executeUpdate(query);
    }
    
    // deletes the journal with the given id
    public void deleteJournal(int journalID) {
        String query = "DELETE FROM journals WHERE id = " + journalID + ";";
        executeUpdate(query);
    }
    
    public ResultSet getJournalMetaData(int journalID) {   
        String query =  "SELECT journals.name AS journal_name, " +
                        "SUM(journalentries.duration) AS total_duration, " +
                        "COUNT(journalentries.id) AS num_entries " +
                        "FROM journals LEFT OUTER JOIN journalentries " +
                        "ON journals.id = journalentries.journal_id " +
                        "WHERE journals.id = " + journalID + ";";
        return executeQuery(query);
    }
    
    // options available for sorting a journal's entries
    public enum SortJournalEntryBy { DATE_ASC, DATE_DESC, DURATION_ASC, DURATION_DESC};
    
    // returns all journal entries for the journal with the given journalID
    // ordered according to the sortBy option
    public ResultSet getJournalEntries(int journalID, SortJournalEntryBy sortBy) {
        String query =  "SELECT id, date, duration, entry " + 
                        "FROM journalentries " +
                        "WHERE journal_id = " + journalID;
        
        String orderBy = "";
        switch (sortBy) {
            case DATE_ASC:
                orderBy = "date ASC";
                break;
            case DATE_DESC:
                orderBy = "date DESC";
                break;
            case DURATION_ASC:
                orderBy = "duration ASC";
                break;
            case DURATION_DESC:
                orderBy = "duration DESC";
                break;
        } 
        query += (" ORDER BY " + orderBy + ";");
        System.out.println(query);
        return executeQuery(query);
    }
    
    public void deleteJournalEntry(int journalEntryID) {
        String query = "DELETE FROM journalentries WHERE id = " + journalEntryID + ";";
        executeUpdate(query);
        notifyJournalDataChangeListeners();
    }
    
    public void addJournalEntry(int journalID, String date, String duration, String entry) {
        String query = "INSERT INTO journalentries(journal_id, date, duration, entry) " + 
                        "VALUES(" + journalID + ", " + 
                        "\"" + date + "\"" + ", " + 
                        duration + ", " + 
                        "\"" + entry + "\"" + ");";
        System.out.println(query);
        executeUpdate(query);
        notifyJournalDataChangeListeners();
    }
    
    private void notifyJournalDataChangeListeners() {
        for (JournalDataChangeListener listener : journalDataChangeListeners) {
            listener.dataChanged();
        }
    }
}