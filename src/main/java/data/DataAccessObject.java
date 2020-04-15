package data;

import java.io.File;
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
    private List<JournalDataChangeListener> journalDataChangeListeners; // listeners that
    // will be notified when journal data is changed, such as when a new entry is added to
    // a journal or an entry is deleted from a journal.
    private static DataAccessObject instance;

    // points Java DB to the directory the database is located in
    private static void setDBSystemDir() {
        // Decide on the db system directory: <userhome>/.projectjournals/
        String userHomeDir = System.getProperty("user.home", ".");
        System.out.println("userHomeDir = " + userHomeDir);
        String systemDir = userHomeDir + File.separator + ".projectjournals";
        System.out.println("systemDir = " + systemDir);
        // Set the db system directory.
        System.setProperty("derby.system.home", systemDir);
    }
    
    public static DataAccessObject getInstance() {
        if (instance == null) {
            instance = new DataAccessObject();
        }
        return instance;
    }
    
    // checks to see whether the database already exists
    // by attempting to create a connection to it. If cannot connect
    // to the database, assume that it doesn't exist. (I'll probably
    // need to improve this check in the future as its too simple minded)
    private boolean databaseExists() {
        try (Connection connection = getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    private void createDatabase() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL + "create=true")) {
            System.out.println("created database");
            createTables(connection);
            System.out.println("created tables");
        } catch (SQLException ex) {
            ex.printStackTrace();
            // shut the program down if the database cannot be created 
            // since the program is useless without being able to access the database
            JOptionPane.showMessageDialog(null, "Could not create database, exiting...");
            System.exit(1);
        }
    }
    
    private void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {

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
        } 
    }
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
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
    }

    public void addJournalDataChangeListener(JournalDataChangeListener listener) {
        journalDataChangeListeners.add(listener);
    }

    //////////////////// data classes ///////////////////////
    
    // classes used to transport data back to the client#
    
    public static class JournalsSummaryData {

        private int journalEntriesCount, totalDuration, journalsCount;

        public JournalsSummaryData(int journalEntriesCount, int totalDuration, int journalsCount) {
            this.journalEntriesCount = journalEntriesCount;
            this.totalDuration = totalDuration;
            this.journalsCount = journalsCount;
        }

        public int getJournalEntriesCount() {
            return journalEntriesCount;
        }

        public int getTotalDuration() {
            return totalDuration;
        }

        public int getJournalsCount() {
            return journalsCount;
        }
    }

    public static class JournalInfo {

        private int id;
        private String name;
        private int duration;
        private int numEntries;

        public JournalInfo(int id, String name, int duration, int numEntries) {
            this.id = id;
            this.name = name;
            this.duration = duration;
            this.numEntries = numEntries;
        }

        public int getID() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getDuration() {
            return duration;
        }

        public int getNumEntries() {
            return numEntries;
        }
    }

    public static class JournalEntry {

        private int id;
        private String date;
        private int duration;
        private String entry;

        public JournalEntry(int id, String date, int duration, String entry) {
            this.id = id;
            this.date = date;
            this.duration = duration;
            this.entry = entry;
        }

        public int getID() {
            return id;
        }

        public String getDate() {
            return date;
        }

        public int getDuration() {
            return duration;
        }

        public String getEntry() {
            return entry;
        }
    }

    ////////// journals operations //////////////////
    
    /*
    * returns the summary data for all journals stored in the system. This
    * includes the total number of journal entries, total duration of
    * journal entries and total number of journals.
     */
    public JournalsSummaryData getAllJournalsSummaryData() throws SQLException {
        String query = "SELECT COUNT(journalentries.id) as journal_entries_count,"
                + " SUM(journalentries.duration) as total_duration,"
                + " COUNT(DISTINCT(journals.id)) as journals_count"
                + " FROM journals LEFT OUTER JOIN journalentries"
                + " ON journals.id = journalentries.journal_id";

        JournalsSummaryData jsd = null;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                int journalEntriesCount = rs.getInt("journal_entries_count");
                int totalDuration = rs.getInt("total_duration");
                int journalsCount = rs.getInt("journals_count");
                jsd = new JournalsSummaryData(journalEntriesCount, totalDuration, journalsCount);
            }
        } 
        
        return jsd;
    }

    public enum SortJournalBy {
        NAME_ASC, NAME_DESC, DURATION_ASC, DURATION_DESC, ENTRIES_ASC, ENTRIES_DESC
    };

    // returns all journals stored in the system sorted according to the sort by value
    public List<JournalInfo> getJournals(SortJournalBy value) throws SQLException {
        String query = "SELECT journals.id AS journal_id, "
                + "journals.name AS journal_name, "
                + "SUM(journalentries.duration) AS total_duration, "
                + "COUNT(journalentries.id) AS num_entries "
                + "FROM journals LEFT OUTER JOIN journalentries "
                + "ON journals.id = journalentries.journal_id "
                + "GROUP BY journals.id, journals.name ";
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
        query += ("ORDER BY " + orderBy);

        List<JournalInfo> ret = new ArrayList<>();

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("journal_id");
                String name = rs.getString("journal_name");
                int duration = rs.getInt("total_duration");
                int numEntries = rs.getInt("num_entries");
                ret.add(new JournalInfo(id, name, duration, numEntries));
            }
        } 
        
        return ret;
    }

    // creates a new journal with the given name
    public void createJournal(String journalName) throws SQLException {
        String query = "INSERT INTO journals(name) VALUES(?)";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, journalName);
            stmt.executeUpdate();
        } 
    }

    public void deleteJournal(int journalID) throws SQLException {
        String query = "DELETE FROM journals WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, journalID);
            stmt.executeUpdate();
        } 
    }

    // returns the meta data for a journal, including the journal's
    // name, duration and number of entries
    public JournalInfo getJournalMetaData(int journalID) throws SQLException {
        String query = "SELECT journals.name AS journal_name, "
                + "SUM(journalentries.duration) AS total_duration, "
                + "COUNT(journalentries.id) AS num_entries "
                + "FROM journals LEFT OUTER JOIN journalentries "
                + "ON journals.id = journalentries.journal_id "
                + "WHERE journals.id = ? "
                + "GROUP BY journals.name";

        JournalInfo ret = null;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, journalID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("journal_name");
                int duration = rs.getInt("total_duration");
                int numEntries = rs.getInt("num_entries");
                ret = new JournalInfo(journalID, name, duration, numEntries);
            }
        } 
        
        return ret;
    }

    //////////// journal entries operations /////////////////////////
    
    // options available for sorting a journal's entries
    public enum SortJournalEntryBy {
        DATE_ASC, DATE_DESC, DURATION_ASC, DURATION_DESC
    };

    // returns all journal entries for the journal with the given journalID
    // ordered according to the sortBy option
    public List<JournalEntry> getJournalEntries(int journalID, SortJournalEntryBy sortBy) 
        throws SQLException {
        String query = "SELECT id, date AS date_formatted, duration, entry"
                + " FROM journalentries WHERE journal_id = ?";
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
        query += (" ORDER BY " + orderBy);

        List<JournalEntry> journalEntries = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, journalID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("date_formatted");
                int duration = rs.getInt("duration");
                String entry = rs.getString("entry");
                journalEntries.add(new JournalEntry(id, date, duration, entry));
            }
        } 
        
        return journalEntries;
    }

    public void createJournalEntry(int journalID, String date, String duration, 
            String entry) throws SQLException {
        String query = "INSERT INTO journalentries(journal_id, date, duration, entry) "
                + "VALUES(?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, journalID);
            stmt.setString(2, date);
            stmt.setString(3, duration);
            stmt.setString(4, entry);
            stmt.executeUpdate();
            notifyJournalDataChangeListeners();
        } 
    }

    public void deleteJournalEntry(int journalEntryID) throws SQLException {
        String query = "DELETE FROM journalentries WHERE id = ?";
        try (Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, journalEntryID);
            stmt.executeUpdate();
            notifyJournalDataChangeListeners();
        } 
    }
    
    public void updateJournalEntry(int journalEntryID, String date, 
            String duration, String entry) throws SQLException {
        String query = "UPDATE journalentries SET "
                + "date = ?, duration = ?, entry = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, date);
            stmt.setString(2, duration);
            stmt.setString(3, entry);
            stmt.setInt(4, journalEntryID);
            stmt.executeUpdate();
            notifyJournalDataChangeListeners();
        } 
    }

    private void notifyJournalDataChangeListeners() {
        for (JournalDataChangeListener listener : journalDataChangeListeners) {
            listener.dataChanged();
        }
    }
}
