package gui;

import core.DataAccessObject;
import core.Utility;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * The data model for the JournalsTable that is displayed on the 
 * Journals screen.
 */
public class JournalsTableModel extends AbstractTableModel {

    private DataAccessObject.SortJournalBy dataSortingMethod; 
    private List<String[]> data; // each String[] represents one row in the table
    private String[] tableHeaders = {"journal id", "journal name", "duration", "# entries"};

    public JournalsTableModel() {
        dataSortingMethod = DataAccessObject.SortJournalBy.DURATION_ASC;
        updateData();
    }

    // standard table model methods
    
    public String getColumnName(int i) {
        return tableHeaders[i];
    }

    public int getColumnCount() {
        return tableHeaders.length;
    }

    public int getRowCount() {
        return data.size();
    }

    public Object getValueAt(int row, int col) {
        String[] rowData = data.get(row);
        switch (col) {
            case 0:
                return rowData[0];
            case 1:
                return rowData[1];
            case 2:
                return rowData[2];
            case 3:
                return rowData[3];
            default:
                return null;
        }
    }

    // table model methods specific to JournalsTableModel
    
    public void setDataSortingMethod(DataAccessObject.SortJournalBy sortingMethod) {
        this.dataSortingMethod = sortingMethod;
    }

    public DataAccessObject.SortJournalBy getDataSortingMethod() {
        return dataSortingMethod;
    }

    // returns the journal ID for the given row
    public int getJournalID(int row) {
        String[] rowData = data.get(row);
        return Integer.parseInt(rowData[0]);
    }

    public void updateData() {
        data = getData();
        fireTableDataChanged();
    }

    // retrieves the data from the database
    private List<String[]> getData() {
        List<String[]> theData = new ArrayList<>();
        DataAccessObject dao = DataAccessObject.getInstance();
        ResultSet rs = dao.getJournals(dataSortingMethod);
        try {
            while (rs.next()) {
                String journalID = rs.getString("id");
                String journalName = rs.getString("name");
                int journalDurationMins = rs.getInt("total_duration");
                String numEntries = rs.getString("num_entries");
                theData.add(new String[]{journalID, journalName, 
                    Utility.getHourMinDuration(journalDurationMins), numEntries});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return theData;
    }
    
    /*private List<String[]> getData() {
        List<String[]> theData = new ArrayList<>();
        theData.add(new String[]{"journal id", "journal name", "journal duration", "num entries"});
        DataAccessObject dao = DataAccessObject.getInstance();
        ResultSet rs = dao.getJournals(dataSortingMethod);

        return theData;
    }*/
}
