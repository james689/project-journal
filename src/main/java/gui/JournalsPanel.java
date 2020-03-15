package gui;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class JournalsPanel extends JPanel {
    public JournalsPanel() {
        JournalsTableModel journalsTableModel = new JournalsTableModel();
        JTable journalsTable = new JTable(journalsTableModel);
        JScrollPane journalsTableScrollPane = new JScrollPane(journalsTable);
        journalsTable.setPreferredScrollableViewportSize(new Dimension(900, 300));
        add(journalsTableScrollPane);
    }
}
