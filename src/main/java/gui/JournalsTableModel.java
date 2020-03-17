package gui;

import core.DataAccessObject;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class JournalsTableModel extends AbstractTableModel {

    private List<String[]> data;
    private String[] tableHeaders = {"journal id", "journal name", "duration", "# entries"};

    public JournalsTableModel() {
        updateData();
    }
    
    private List<String[]> getData() {
        List<String[]> theData = new ArrayList<>();
        // get the data from database
        DataAccessObject dao = DataAccessObject.getInstance();
        ResultSet rs = dao.getJournals();
        try {
            while (rs.next()) {
                String journalID = rs.getString("id");
                String journalName = rs.getString("name");
                int journalDurationMins = rs.getInt("total_duration");
                String numEntries = rs.getString("num_entries");
                theData.add(new String[] {journalID,journalName,getHourMinDuration(journalDurationMins),numEntries});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return theData;
    }
    
    public void updateData() {
        data = getData();
        fireTableDataChanged();
    }
    
    // converts minutes into hours and minutes e.g. if mins = 75 then
    // return value will be "1 hour 15 mins"
    private static String getHourMinDuration(int mins) {
        int durationHours = mins / 60;
        int durationMins = mins % 60;
        return durationHours + " hours " + durationMins + " mins";
    }

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
}
