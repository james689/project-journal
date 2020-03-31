package gui;

import core.DataAccessObject;
import core.Utility;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel metaDataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        metaDataPanel.setBorder(BorderFactory.createTitledBorder("Meta data"));
        nameLabel = new JLabel();
        durationLabel = new JLabel();
        numEntriesLabel = new JLabel();
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
        add(Box.createVerticalStrut(20));
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

        // the JournalViewPanel must be revalidated and repainted to ensure
        // that the changes in journal entry data are visible.
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
                journalEntriesPanel.add(Box.createRigidArea(new Dimension(5, 10)));
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

            //setLayout(new FlowLayout(FlowLayout.LEFT));
            setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
            //setBackground(Color.GREEN);

            Box entryLabelsBox = Box.createHorizontalBox();
            entryLabelsBox.setAlignmentX( Component.LEFT_ALIGNMENT );
            JLabel entryIDLabel = new JLabel("ID: " + Integer.toString(entryID));
            entryIDLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
            JLabel entryDateLabel = new JLabel("Date: " + date);
            entryDateLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
            JLabel entryDurationLabel = new JLabel("Duration: " + Utility.getHourMinDuration(duration));
            entryDurationLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
            entryLabelsBox.add(entryIDLabel);
            entryLabelsBox.add(Box.createRigidArea(new Dimension(10,0)));
            entryLabelsBox.add(entryDateLabel);
            entryLabelsBox.add(Box.createRigidArea(new Dimension(10,0)));
            entryLabelsBox.add(entryDurationLabel);

            JTextArea textArea = new JTextArea(entryText, 20, 20);
            textArea.setAlignmentX( Component.LEFT_ALIGNMENT );
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            /*String borderTitle = "Entry id: " + entryID + " Date: " + date + " Duration: " + Utility.getHourMinDuration(duration);
            Border titledBorder = BorderFactory.createTitledBorder(borderTitle);
            Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
            setBorder(BorderFactory.createCompoundBorder(emptyBorder, titledBorder));*/

            Box buttonBox = Box.createHorizontalBox();
            buttonBox.setAlignmentX( Component.LEFT_ALIGNMENT );
            JButton editButton = new JButton("Edit");
            editButton.setAlignmentX( Component.LEFT_ALIGNMENT );
            editButton.addActionListener(this);
            JButton deleteButton = new JButton("Delete");
            deleteButton.setAlignmentX( Component.LEFT_ALIGNMENT );
            deleteButton.addActionListener(this);
            buttonBox.add(editButton);
            buttonBox.add(deleteButton);

            add(entryLabelsBox);
            add(textArea);
            add(buttonBox);
            //add(editButton);
            //add(deleteButton);
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
            // create and display a JournalDataEntryPanel populated with the data from this journal entry
            JournalDataEntryPanel journalDataEntryPanel = new JournalDataEntryPanel();
            journalDataEntryPanel.setDate(date);
            journalDataEntryPanel.setDuration(Integer.toString(duration));
            journalDataEntryPanel.setEntry(entryText);

            int result = JOptionPane.showConfirmDialog(null, journalDataEntryPanel,
                    "Edit journal entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                // get data from journal entry panel
                String date = journalDataEntryPanel.getDate();
                String dateFormattedForMysql = Utility.convertToMysqlDateFormat(date);
                String duration = journalDataEntryPanel.getDuration();
                String entry = journalDataEntryPanel.getEntry();
                dao.updateJournalEntry(entryID, dateFormattedForMysql, duration, entry);
            }
        }
    } // end JournalEntryPanel class

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
                // get data from journal entry panel
                String date = journalDataEntryPanel.getDate();
                String dateFormattedForMysql = Utility.convertToMysqlDateFormat(date);
                String duration = journalDataEntryPanel.getDuration();
                String entry = journalDataEntryPanel.getEntry();
                dao.addJournalEntry(journalID, dateFormattedForMysql, duration, entry);
            }
        }
    }
}
