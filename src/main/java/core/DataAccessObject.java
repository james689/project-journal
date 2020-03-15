package core; 

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

    // returns all journals stored in the system
    public ResultSet getJournals() {
        ResultSet rs = null;
        String query =  "SELECT journals.id, journals.name, SUM(journalentries.duration) AS total_duration, " +
                        "COUNT(journalentries.id) AS num_entries " +
                        "FROM journals LEFT OUTER JOIN journalentries " +
                        "ON journals.id = journalentries.journal_id " +
                        "GROUP BY journals.id;";
        try {
            Statement statement = db.createStatement();
            rs = statement.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rs;
    }
}