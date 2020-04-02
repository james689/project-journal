package core;

import gui.JournalDataChangeListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

    public enum SortJournalBy {
        NAME_ASC, NAME_DESC, DURATION_ASC, DURATION_DESC, ENTRIES_ASC, ENTRIES_DESC
    };

    // returns all journals stored in the system
    public ResultSet getJournals(SortJournalBy value) {
        String query = "SELECT journals.id, journals.name, SUM(journalentries.duration) AS total_duration, "
                + "COUNT(journalentries.id) AS num_entries "
                + "FROM journals LEFT OUTER JOIN journalentries "
                + "ON journals.id = journalentries.journal_id "
                + "GROUP BY journals.id ";
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
        ResultSet rs = null;
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
        String query = "INSERT INTO journals(name) VALUES(?);";
        try {
            PreparedStatement createJournalStatement = db.prepareStatement(query);
            createJournalStatement.setString(1, journalName);
            createJournalStatement.executeUpdate();
            createJournalStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void deleteJournal(int journalID) {
        String query = "DELETE FROM journals WHERE id = ?;";
        try {
            PreparedStatement deleteJournalStatement = db.prepareStatement(query);
            deleteJournalStatement.setInt(1, journalID);
            deleteJournalStatement.executeUpdate();
            deleteJournalStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public ResultSet getJournalMetaData(int journalID) {
        String query = "SELECT journals.name AS journal_name, "
                + "SUM(journalentries.duration) AS total_duration, "
                + "COUNT(journalentries.id) AS num_entries "
                + "FROM journals LEFT OUTER JOIN journalentries "
                + "ON journals.id = journalentries.journal_id "
                + "WHERE journals.id = ?;";
        System.out.println(query);
        // execute the query
        ResultSet rs = null;
        try {
            PreparedStatement getJournalMetaDataStatement = db.prepareStatement(query);
            getJournalMetaDataStatement.setInt(1, journalID);
            rs = getJournalMetaDataStatement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    // options available for sorting a journal's entries
    public enum SortJournalEntryBy {
        DATE_ASC, DATE_DESC, DURATION_ASC, DURATION_DESC
    };

    // returns all journal entries for the journal with the given journalID
    // ordered according to the sortBy option
    public ResultSet getJournalEntries(int journalID, SortJournalEntryBy sortBy) {
        // for DATE_FORMAT see https://www.w3schools.com/sql/func_mysql_date_format.asp
        // build the query
        String query = "SELECT id, DATE_FORMAT(date, \"%d/%m/%Y\") AS date_formatted, "
                + "duration, entry FROM journalentries WHERE journal_id = ?";
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
        // execute the query
        ResultSet rs = null;
        try {
            PreparedStatement getJournalEntriesStatement = db.prepareStatement(query);
            getJournalEntriesStatement.setInt(1, journalID);
            rs = getJournalEntriesStatement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }
    
    public void deleteJournalEntry(int journalEntryID) {
        String query = "DELETE FROM journalentries WHERE id = ?;";
        try {
            PreparedStatement deleteJournalEntryStatement = db.prepareStatement(query);
            deleteJournalEntryStatement.setInt(1, journalEntryID);
            deleteJournalEntryStatement.executeUpdate();
            deleteJournalEntryStatement.close();
            notifyJournalDataChangeListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addJournalEntry(int journalID, String date, String duration, String entry) {
        String query = "INSERT INTO journalentries(journal_id, date, duration, entry) "
                + "VALUES(?,?,?,?);";      
        try {
            PreparedStatement createJournalEntryStatement = db.prepareStatement(query);
            createJournalEntryStatement.setInt(1, journalID);
            createJournalEntryStatement.setString(2, date);
            createJournalEntryStatement.setString(3, duration);
            createJournalEntryStatement.setString(4, entry);
            createJournalEntryStatement.executeUpdate();
            createJournalEntryStatement.close();
            notifyJournalDataChangeListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // returns true if one or more journal entries exist
    // in the database for the given journal on the given date
    public boolean checkJournalEntryExists(int journalID, String date) {
        boolean ret = true; 
        
        String query = "SELECT * FROM journalentries WHERE journal_id = ? AND date = ?;";   
        try {
            PreparedStatement statement = db.prepareStatement(query);
            statement.setInt(1, journalID);
            statement.setString(2, date);
            ResultSet rs = statement.executeQuery();
            if (rs.first() == false) { // see https://stackoverflow.com/questions/18301326/can-a-resultset-be-null-in-java
                // there are no rows in the result set i.e. result set is empty
                ret = false;
            }
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    public void updateJournalEntry(int journalEntryID, String date, String duration, String entry) {
        System.out.println("new prepared statement update journal being used");
        String query = "UPDATE journalentries SET "
                + "date = ?, duration = ?, entry = ? WHERE id = ?;";
        try {
            PreparedStatement updateJournalEntryStatement = db.prepareStatement(query);
            updateJournalEntryStatement.setString(1, date);
            updateJournalEntryStatement.setString(2, duration);
            updateJournalEntryStatement.setString(3, entry);
            updateJournalEntryStatement.setInt(4,journalEntryID);
            updateJournalEntryStatement.executeUpdate();
            updateJournalEntryStatement.close();
            notifyJournalDataChangeListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyJournalDataChangeListeners() {
        for (JournalDataChangeListener listener : journalDataChangeListeners) {
            listener.dataChanged();
        }
    }
}
