package gui;

import data.JournalDataChangeListener;
import data.DataAccessObject;
import utility.Utility;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * The JournalsScreen class represents the GUI screen that displays all journals stored
 * in the system. It implements the JournalDataChangeListener interface so that
 * it can be notified when a journal's data has changed and then refresh it
 * display of the data.
 */
public class JournalsScreen extends JPanel implements JournalDataChangeListener {

    private DataAccessObject dao;
    private JTable journalsTable;
    private JournalsTableModel journalsTableModel;
    private JComboBox sortByComboBox;
    private CardsPanel cardsPanel;
    private JournalEntriesScreen journalViewScreen;
    private JLabel numEntriesLabel, totalDurationLabel, numJournalsLabel;

    public JournalsScreen(CardsPanel cardsPanel, JournalEntriesScreen journalViewScreen) {
        this.cardsPanel = cardsPanel;
        this.journalViewScreen = journalViewScreen;

        dao = DataAccessObject.getInstance();
        dao.addJournalDataChangeListener(this);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        journalsTableModel = new JournalsTableModel();
        journalsTable = new JTable(journalsTableModel);
        JScrollPane journalsTableScrollPane = new JScrollPane(journalsTable);
        journalsTable.setPreferredScrollableViewportSize(new Dimension(900, 300));

        JPanel sortByPanel = new JPanel();
        sortByComboBox = new JComboBox(DataAccessObject.SortJournalBy.values());
        sortByComboBox.setSelectedItem(journalsTableModel.getDataSortingMethod());
        sortByComboBox.addActionListener(new SortByComboBoxListener());
        sortByPanel.add(new JLabel("Sort By: "));
        sortByPanel.add(sortByComboBox);

        numEntriesLabel = new JLabel("Total Entries: ");
        totalDurationLabel = new JLabel("Total Duration: ");
        numJournalsLabel = new JLabel("Num Journals: ");
        Box summaryDataLabelsBox = Box.createVerticalBox();
        summaryDataLabelsBox.add(numEntriesLabel);
        summaryDataLabelsBox.add(totalDurationLabel);
        summaryDataLabelsBox.add(numJournalsLabel);
        updateSummaryDataLabels();

        JPanel buttonsPanel = new JPanel();

        JButton createJournalButton = new JButton("Create New Journal");
        createJournalButton.addActionListener(new CreateJournalButtonListener());
        buttonsPanel.add(createJournalButton);

        JButton deleteJournalButton = new JButton("Delete Journal");
        deleteJournalButton.addActionListener(new DeleteJournalButtonListener());
        buttonsPanel.add(deleteJournalButton);

        JButton viewJournalButton = new JButton("View/Edit Journal");
        viewJournalButton.addActionListener(new ViewJournalButtonListener());
        buttonsPanel.add(viewJournalButton);

        add(sortByPanel);
        add(summaryDataLabelsBox);
        add(journalsTableScrollPane);
        add(buttonsPanel);
    }

    /**
     * This method is called when a journal's data has changed, for example a
     * new entry has been added to the journal, an entry has been deleted from
     * the journal etc. The screen then needs to update its display of the data.
     */
    public void dataChanged() {
        journalsTableModel.updateData();
        updateSummaryDataLabels();
    }

    private void updateSummaryDataLabels() {
        try {
            DataAccessObject.JournalsSummaryData jsd = dao.getAllJournalsSummaryData();
            numEntriesLabel.setText("Total Entries: " + jsd.getJournalEntriesCount());
            totalDurationLabel.setText("Total Duration: " + Utility.getHourMinDuration(jsd.getTotalDuration()));
            numJournalsLabel.setText("Journals Count: " + jsd.getJournalsCount());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error in retrieving journals summary data");
        }
    }

    // inner class listeners
    public class CreateJournalButtonListener implements ActionListener {

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

            try {
                dao.createJournal(journalName);
                dataChanged();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error in creating journal");
            }
        }
    }

    public class DeleteJournalButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int selectedRow = journalsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "no journal selected for deletion");
                return;
            }

            // prompt user to make sure they really do want to delete this journal
            int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this journal?",
                    "Delete confirmation", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                // delete the journal from the database
                try {
                    dao.deleteJournal(journalsTableModel.getJournalID(selectedRow));
                    dataChanged();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error in deleting journal");
                }
            }
        }
    }

    public class ViewJournalButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int selectedRow = journalsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "no journal selected");
                return;
            }

            int journalID = journalsTableModel.getJournalID(selectedRow);
            journalViewScreen.setJournalID(journalID);
            cardsPanel.switchToCard("journal view");
        }
    }

    public class SortByComboBoxListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            DataAccessObject.SortJournalBy sortBy = (DataAccessObject.SortJournalBy) sortByComboBox.getSelectedItem();
            journalsTableModel.setDataSortingMethod(sortBy);
            journalsTableModel.updateData(); // the table model needs to be told the
            // data it is to display has changed 
        }
    }
}
