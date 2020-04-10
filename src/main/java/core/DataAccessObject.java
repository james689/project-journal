package core;

import gui.JournalDataChangeListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

// This object is used to access and manipulate data from the data source
public class DataAccessObject {

    private static final String DATABASE_URL = "jdbc:derby:learningjournal;";

    private static DataAccessObject instance; // the single instance of the DataAccessObject
    private Connection dbConnection; // the connection to the database
    private List<JournalDataChangeListener> journalDataChangeListeners; // listeners that
    // will be notified when journal data is changed, such as when a new entry is added to
    // a journal or an entry is deleted from a journal.

    private static void setDBSystemDir() {
        // Decide on the db system directory: <userhome>/.projectjournals/
        String userHomeDir = System.getProperty("user.home", ".");
        System.out.println("userHomeDir = " + userHomeDir);
        String systemDir = userHomeDir + "\\.projectjournals";
        System.out.println("systemDir = " + systemDir);
        // Set the db system directory.
        System.setProperty("derby.system.home", systemDir);
    }

    // checks to see whether the database already exists or not
    // by attempting to create a connection to it. If cannot connect
    // to the database, assume that it doesn't exist. (I'll probably
    // need to improve this check in the future as its too simple minded)
    private boolean databaseExists() {
        try {
            DriverManager.getConnection(DATABASE_URL);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    // this method is executed when the program is run for the very first time
    // and there is no pre-existing database.
    private void createDatabase() {
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL + "create=true");
            System.out.println("created database");
            createTables(connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
            // shut the program down if the database cannot be created 
            // since the program is useless without being able to access the database
            JOptionPane.showMessageDialog(null, "Could not create database, exiting...");
            System.exit(1);
        }
    }

    // creates the database tables
    private void createTables(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            
            String query = "CREATE TABLE journals("
                    + "id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + " name CHAR(254) NOT NULL)";
            
            statement.execute(query);
            System.out.println("created journals table");
            
            query = "CREATE TABLE journalentries("
                    + "id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + " journal_id INTEGER NOT NULL,"
                    + " date DATE NOT NULL,"
                    + " duration INTEGER NOT NULL,"
                    + " entry LONG VARCHAR NOT NULL)";
            
            statement.execute(query);
            System.out.println("created journal entries table");
        } catch (Exception e) {
            System.out.println("error creating database tables");
            e.printStackTrace();
        }
    }

    private void connectToDatabase() {
        try {
            dbConnection = DriverManager.getConnection(DATABASE_URL);
            System.out.println("connected to database");
        } catch (SQLException ex) {
            ex.printStackTrace();
            // shut the program down if a database connection cannot be established
            // since the program is useless without being able to access the database
            JOptionPane.showMessageDialog(null, "Could not initialize database, exiting...");
            System.exit(1);
        }
    }

    // constructor is private, DataAccessObject is a singleton
    private DataAccessObject() {
        System.out.println("data access object constructor invoked");
        journalDataChangeListeners = new ArrayList<>();

        setDBSystemDir(); // make sure the database is set up in the correct directory

        if (!databaseExists()) {
            System.out.println("no database found, creating database...");
            createDatabase(); // create the database if it doesn't exist
        } else {
            System.out.println("found existing database");
        }

        connectToDatabase(); // establish a connection to the database and use the
        // connection for all future SQL statements
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

    /*
    * returns the summary data for all journals stored in the system. This
    * includes the total number of journal entries, total duration of
    * journal entries and total number of journals.
     */
    public ResultSet getAllJournalsSummaryData() {
        String query = "SELECT COUNT(journalentries.id) as journal_entries_count,"
                + " SUM(journalentries.duration) as total_duration,"
                + " COUNT(DISTINCT(journals.id)) as journals_count"
                + " FROM journals LEFT OUTER JOIN journalentries"
                + " ON journals.id = journalentries.journal_id;";
        ResultSet rs = null;
        try {
            Statement statement = dbConnection.createStatement();
            rs = statement.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public enum SortJournalBy {
        NAME_ASC, NAME_DESC, DURATION_ASC, DURATION_DESC, ENTRIES_ASC, ENTRIES_DESC
    };

    // returns all journals stored in the system
    // sorted according to the sort by value
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
        ResultSet rs = null;
        try {
            Statement statement = dbConnection.createStatement();
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
            PreparedStatement createJournalStatement = dbConnection.prepareStatement(query);
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
            PreparedStatement deleteJournalStatement = dbConnection.prepareStatement(query);
            deleteJournalStatement.setInt(1, journalID);
            deleteJournalStatement.executeUpdate();
            deleteJournalStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // returns the meta data for a journal, including the journal's
    // name, duration and number of entries
    public ResultSet getJournalMetaData(int journalID) {
        String query = "SELECT journals.name AS journal_name, "
                + "SUM(journalentries.duration) AS total_duration, "
                + "COUNT(journalentries.id) AS num_entries "
                + "FROM journals LEFT OUTER JOIN journalentries "
                + "ON journals.id = journalentries.journal_id "
                + "WHERE journals.id = ?;";
        ResultSet rs = null;
        try {
            PreparedStatement getJournalMetaDataStatement = dbConnection.prepareStatement(query);
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
        // execute the query
        ResultSet rs = null;
        try {
            PreparedStatement getJournalEntriesStatement = dbConnection.prepareStatement(query);
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
            PreparedStatement deleteJournalEntryStatement = dbConnection.prepareStatement(query);
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
            PreparedStatement createJournalEntryStatement = dbConnection.prepareStatement(query);
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
            PreparedStatement statement = dbConnection.prepareStatement(query);
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
        String query = "UPDATE journalentries SET "
                + "date = ?, duration = ?, entry = ? WHERE id = ?;";
        try {
            PreparedStatement updateJournalEntryStatement = dbConnection.prepareStatement(query);
            updateJournalEntryStatement.setString(1, date);
            updateJournalEntryStatement.setString(2, duration);
            updateJournalEntryStatement.setString(3, entry);
            updateJournalEntryStatement.setInt(4, journalEntryID);
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
