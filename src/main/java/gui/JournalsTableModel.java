package gui;

import data.DataAccessObject;
import data.DataAccessObject.JournalInfo;
import java.sql.SQLException;
import utility.Utility;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for the JournalsTable that is displayed on the Journals
 * screen.
 */
public class JournalsTableModel extends AbstractTableModel {

    private DataAccessObject.SortJournalBy dataSortingMethod;
    private List<DataAccessObject.JournalInfo> data;
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
        DataAccessObject.JournalInfo rowData = data.get(row);
        switch (col) {
            case 0:
                return rowData.getID();
            case 1:
                return rowData.getName();
            case 2:
                return Utility.getHourMinDuration(rowData.getDuration());
            case 3:
                return rowData.getNumEntries();
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
        return data.get(row).getID();
    }

    public void updateData() {
        data = getData();
        fireTableDataChanged();
    }

    // retrieves the data from the database
    private List<DataAccessObject.JournalInfo> getData() {
        DataAccessObject dao = DataAccessObject.getInstance();
        List<JournalInfo> ret = null;
        try {
            ret = dao.getJournals(dataSortingMethod);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving journals from database");
        }
        return ret;
    }
}
