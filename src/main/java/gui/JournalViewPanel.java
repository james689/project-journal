package gui;

import core.DataAccessObject;
import core.Utility;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class JournalViewPanel extends JPanel implements JournalDataChangeListener {

    private CardsPanel cardsPanel;
    private int journalID; // id of the journal the panel will display

    private JLabel nameLabel, durationLabel, numEntriesLabel;
    private JPanel journalEntriesPanel; // holds all of the journal entries

    private DataAccessObject dao;

    public JournalViewPanel(CardsPanel cardsPanel) {
        this.cardsPanel = cardsPanel;
        dao = DataAccessObject.getInstance();
        dao.addJournalDataChangeListener(this);
        journalID = -1;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel metaDataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        metaDataPanel.setBorder(BorderFactory.createTitledBorder("Meta data"));
        nameLabel = new JLabel("tetris journal");
        durationLabel = new JLabel("duration: 8 hours 30 mins");
        numEntriesLabel = new JLabel("# entries: 6");
        Box labelsBox = Box.createVerticalBox();
        labelsBox.add(nameLabel);
        labelsBox.add(durationLabel);
        labelsBox.add(numEntriesLabel);
        metaDataPanel.add(labelsBox);

        journalEntriesPanel = new JPanel();
        journalEntriesPanel.setBorder(BorderFactory.createTitledBorder("Entries"));
        journalEntriesPanel.setLayout(new BoxLayout(journalEntriesPanel, BoxLayout.Y_AXIS));

        JScrollPane journalEntriesPanelScrollPane = new JScrollPane(journalEntriesPanel);
        
        JButton backToJournalsPanelButton = new JButton("back to journals");
        backToJournalsPanelButton.addActionListener(new BackToJournalsPanelButtonListener());

        JButton newJournalEntryButton = new JButton("New entry");
        newJournalEntryButton.addActionListener(new NewJournalEntryButtonListener());
        
        add(backToJournalsPanelButton);
        add(metaDataPanel);
        add(journalEntriesPanelScrollPane);
        add(newJournalEntryButton);
    }

    public void dataChanged() {
        System.out.println("JournalViewPanel.dataChanged() called");
        refreshData();
    }

    public void setJournalID(int journalID) {
        this.journalID = journalID;
        refreshData();
    }

    private void refreshData() {
        ResultSet journalMetaData = dao.getJournalMetaData(journalID);
        ResultSet journalEntriesData = dao.getJournalEntries(journalID, DataAccessObject.SortJournalEntryBy.DATE_ASC);

        populateJournalMetaDataLabels(journalMetaData);
        populateJournalEntriesPanel(journalEntriesData);
        
        revalidate();
        repaint();
    }

    private void populateJournalEntriesPanel(ResultSet journalEntriesData) {
        // clear out the previous contents of the journal entries panel
        journalEntriesPanel.removeAll();

        try {
            while (journalEntriesData.next()) {
                int entryID = journalEntriesData.getInt("id");
                String date = journalEntriesData.getString("date_formatted");
                int duration = journalEntriesData.getInt("duration");
                String entry = journalEntriesData.getString("entry");
                journalEntriesPanel.add(new JournalEntryPanel(entryID, date, duration, entry));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("repopulated journal entries panel");
    }

    private void populateJournalMetaDataLabels(ResultSet journalMetaData) {
        try {
            while (journalMetaData.next()) {
                nameLabel.setText("Name: " + journalMetaData.getString("journal_name"));
                int durationMins = journalMetaData.getInt("total_duration");
                durationLabel.setText("Duration: " + Utility.getHourMinDuration(durationMins));
                numEntriesLabel.setText("Num entries: " + journalMetaData.getString("num_entries"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("repopulated journal meta data panel");
    }

    // graphical representation of a journal entry
    class JournalEntryPanel extends JPanel implements ActionListener {

        private int entryID;
        private String date;
        private int duration;
        private String entryText;

        public JournalEntryPanel(int entryID, String date, int duration, String entryText) {
            this.entryID = entryID;
            this.date = date;
            this.duration = duration;
            this.entryText = entryText;
            
            setLayout(new FlowLayout(FlowLayout.LEFT));

            JTextArea textArea = new JTextArea(entryText, 20, 40);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            String borderTitle = "Entry id: " + entryID + " Date: " + date + " Duration: " + duration;
            setBorder(BorderFactory.createTitledBorder(borderTitle));

            JButton editButton = new JButton("Edit");
            editButton.addActionListener(this);
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(this);

            add(textArea);
            add(editButton);
            add(deleteButton);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Edit")) {
                System.out.println("edit button for id " + entryID);
                edit();
            } else if (e.getActionCommand().equals("Delete")) {
                System.out.println("delete button for id " + entryID);
                delete();
            }
        }

        private void delete() {
            // prompt user to make sure they really do want to delete this journal entry
            int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this journal entry?", 
                    "Delete confirmation", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                // delete the journal entry from the database
                dao.deleteJournalEntry(entryID);
            }
        }
        
        private void edit() {
            // create a JournalDataEntryPanel populated with the data from this journal entry
            JournalDataEntryPanel journalDataEntryPanel = new JournalDataEntryPanel();
            journalDataEntryPanel.setDate(date);
            journalDataEntryPanel.setDuration(Integer.toString(duration));
            journalDataEntryPanel.setEntry(entryText);
            // display a popup
            int result = JOptionPane.showConfirmDialog(null, journalDataEntryPanel,
                    "Edit journal entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                System.out.println("user clicked ok on edit journal");
                // get data from journal entry panel
                String date = journalDataEntryPanel.getDate();
                String dateFormattedForMysql = Utility.convertToMysqlDateFormat(date);
                String duration = journalDataEntryPanel.getDuration();
                String entry = journalDataEntryPanel.getEntry();
                dao.updateJournalEntry(entryID, dateFormattedForMysql, duration, entry);
            } else {
                System.out.println("user clicked cancel on edit journal");
            }
        }
    }

    // inner class listeners
    class BackToJournalsPanelButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            cardsPanel.switchToCard("journals");
        }
    }

    class NewJournalEntryButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // show a dialog that allows the user to write a new journal entry
            JournalDataEntryPanel journalDataEntryPanel = new JournalDataEntryPanel();
            int result = JOptionPane.showConfirmDialog(null, journalDataEntryPanel,
                    "Add new journal entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                System.out.println("user clicked ok");
                // get data from journal entry panel
                String date = journalDataEntryPanel.getDate();
                String dateFormattedForMysql = Utility.convertToMysqlDateFormat(date);
                String duration = journalDataEntryPanel.getDuration();
                String entry = journalDataEntryPanel.getEntry();
                System.out.println("Date: " + date);
                System.out.println("Duration: " + duration);
                System.out.println("Entry: " + entry);
                dao.addJournalEntry(journalID, dateFormattedForMysql, duration, entry);
            } else {
                System.out.println("user clicked cancel");
            }
        }
    }
}
