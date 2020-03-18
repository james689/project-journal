package gui;

import core.DataAccessObject;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class JournalsPanel extends JPanel {

    private DataAccessObject dao;
    private JTable journalsTable;
    private JournalsTableModel journalsTableModel;
    private JComboBox sortByComboBox;

    public JournalsPanel() {
        dao = DataAccessObject.getInstance();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        sortByComboBox = new JComboBox(DataAccessObject.SortJournalBy.values());
        sortByComboBox.addActionListener(new SortByComboBoxListener());

        journalsTableModel = new JournalsTableModel();
        journalsTable = new JTable(journalsTableModel);
        JScrollPane journalsTableScrollPane = new JScrollPane(journalsTable);
        journalsTable.setPreferredScrollableViewportSize(new Dimension(900, 300));

        JButton createNewJournalButton = new JButton("Create New Journal");
        createNewJournalButton.addActionListener(new CreateNewJournalButtonListener());

        JButton deleteJournalButton = new JButton("Delete journal");
        deleteJournalButton.addActionListener(new DeleteJournalButtonListener());

        add(new JLabel("Sort By: "));
        add(sortByComboBox);
        add(journalsTableScrollPane);
        add(createNewJournalButton);
        add(deleteJournalButton);
    }

    public class CreateNewJournalButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String journalName = JOptionPane.showInputDialog("Enter journal name");

            if (journalName == null) {
                // user cancelled dialog
                return;
            }

            if (journalName.isEmpty()) {
                JOptionPane.showMessageDialog(null, "journal name cannot be empty");
                return;
            }

            dao.createNewJournal(journalName);
            DataAccessObject.SortJournalBy sortBy = (DataAccessObject.SortJournalBy) sortByComboBox.getSelectedItem();
            journalsTableModel.updateData(sortBy); // table model needs to be told there
            // is new data to display
        }
    }

    public class DeleteJournalButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int selectedRow = journalsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "no journal selected");
                return;
            }

            dao.deleteJournal(journalsTableModel.getJournalID(selectedRow));
            DataAccessObject.SortJournalBy sortBy = (DataAccessObject.SortJournalBy) sortByComboBox.getSelectedItem();
            journalsTableModel.updateData(sortBy); // table model needs to be told the
            // data it is to display has changed
        }
    }

    public class SortByComboBoxListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            DataAccessObject.SortJournalBy sortBy = (DataAccessObject.SortJournalBy) sortByComboBox.getSelectedItem();
            journalsTableModel.updateData(sortBy); // table model needs to be told the
            // data it is to display has changed
            System.out.println("sort by = " + sortBy);
        }
    }
}
