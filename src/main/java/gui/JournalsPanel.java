package gui;

import core.DataAccessObject;
import core.Utility;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
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

public class JournalsPanel extends JPanel implements JournalDataChangeListener {

    private DataAccessObject dao;
    private JTable journalsTable;
    private JournalsTableModel journalsTableModel;
    private JComboBox sortByComboBox;
    private CardsPanel cardsPanel;
    private JournalViewPanel journalViewPanel;
    private JLabel numEntriesLabel, totalDurationLabel, numJournalsLabel;

    public JournalsPanel(CardsPanel cardsPanel, JournalViewPanel journalViewPanel) {
        this.cardsPanel = cardsPanel;
        this.journalViewPanel = journalViewPanel;

        dao = DataAccessObject.getInstance();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        journalsTableModel = new JournalsTableModel();
        //dao.addJournalDataChangeListener(journalsTableModel);
        dao.addJournalDataChangeListener(this);
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

        JButton createNewJournalButton = new JButton("Create New Journal");
        createNewJournalButton.addActionListener(new CreateNewJournalButtonListener());
        buttonsPanel.add(createNewJournalButton);

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
    
    public void dataChanged() {
        // a journal's data has changed, tell the table model (which is owned by this
        // panel) to update its data and also update the summary data labels
        journalsTableModel.updateData();
        updateSummaryDataLabels();
    }
    
    private void updateSummaryDataLabels() {
        ResultSet rs = dao.getAllJournalsSummaryData();
        try {
            while (rs.next()) {
                numEntriesLabel.setText("Total Entries: " + rs.getInt("journal_entries_count"));
                totalDurationLabel.setText("Total Duration: " + Utility.getHourMinDuration(rs.getInt("total_duration")));
                numJournalsLabel.setText("Journals Count: " + rs.getInt("journals_count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            dataChanged();
        }
    }

    public class DeleteJournalButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int selectedRow = journalsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "no journal selected");
                return;
            }

            // prompt user to make sure they really do want to delete this journal
            int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this journal?",
                    "Delete confirmation", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                // delete the journal from the database
                dao.deleteJournal(journalsTableModel.getJournalID(selectedRow));
                dataChanged();
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
            // show the JournalViewPanel with specified journal
            journalViewPanel.setJournalID(journalID);
            cardsPanel.switchToCard("journal view");
        }
    }

    public class SortByComboBoxListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            DataAccessObject.SortJournalBy sortBy = (DataAccessObject.SortJournalBy) sortByComboBox.getSelectedItem();
            journalsTableModel.setDataSortingMethod(sortBy);
            journalsTableModel.updateData(); // table model needs to be told the
            // data it is to display has changed (don't need to update the journals summary
            // data since this won't be affected so don't need to call dataChanged())
        }
    }
}
