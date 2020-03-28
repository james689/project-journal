package gui;

import core.DataAccessObject;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class JournalViewPanel extends JPanel {
    
    private CardsPanel cardsPanel;
    private List<JournalDataChangeListener> listeners;
    private int journalID; // id of journal the panel will display
    
    private JLabel nameLabel, durationLabel, numEntriesLabel;
    private JScrollPane journalEntriesPanelScrollPane;
    private JPanel journalEntriesPanel; // holds all of the journal entries
    
    public JournalViewPanel(CardsPanel cardsPanel) {
        this.cardsPanel = cardsPanel;
        listeners = new ArrayList<>();
        journalID = -1;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel metaDataPanel = new JPanel();
        metaDataPanel.setBorder(BorderFactory.createTitledBorder("Meta data"));
        nameLabel = new JLabel("tetris journal");
        durationLabel = new JLabel("duration: 8 hours 30 mins");
        numEntriesLabel = new JLabel("# entries: 6");
        metaDataPanel.add(nameLabel);
        metaDataPanel.add(durationLabel);
        metaDataPanel.add(numEntriesLabel);
        
        journalEntriesPanel = new JPanel();
        journalEntriesPanel.setBorder(BorderFactory.createTitledBorder("Entries"));
        journalEntriesPanel.setLayout(new BoxLayout(journalEntriesPanel, BoxLayout.Y_AXIS));
        
        journalEntriesPanelScrollPane = new JScrollPane(journalEntriesPanel);
        
        JButton backToJournalsPanelButton = new JButton("back to journals");
        backToJournalsPanelButton.addActionListener(new BackToJournalsPanelButtonListener());
        
        add(metaDataPanel);
        //add(journalEntriesPanel);
        add(journalEntriesPanelScrollPane);
        add(backToJournalsPanelButton);
    }
    
    public void addDataChangeListener(JournalDataChangeListener l) {
        listeners.add(l);
    }
    
    public void setJournalID(int journalID) {
        this.journalID = journalID;
        
        refreshData();
    }
    
    private void refreshData() {
        DataAccessObject dao = DataAccessObject.getInstance();
        ResultSet journalMetaData = dao.getJournalMetaData(journalID);
        ResultSet journalEntriesData = dao.getJournalEntries(journalID, DataAccessObject.SortJournalEntryBy.DATE_ASC);
        
        populateJournalMetaDataLabels(journalMetaData);
        populateJournalEntriesPanel(journalEntriesData);
    }
    
    class BackToJournalsPanelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            cardsPanel.switchToCard("journals");
        }
    }
    
    private void populateJournalEntriesPanel(ResultSet journalEntriesData) {
        // clear out the previous contents of the journal entries panel
        journalEntriesPanel.removeAll();
        
        try {
            while (journalEntriesData.next()) {
                int entryID = journalEntriesData.getInt("id");
                String date = journalEntriesData.getString("date");
                int duration = journalEntriesData.getInt("duration");
                String entry = journalEntriesData.getString("entry");
                
                journalEntriesPanel.add(new JournalEntryPanel(entryID,date,duration,entry));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void populateJournalMetaDataLabels(ResultSet journalMetaData) {
        try {
            while (journalMetaData.next()) {
                nameLabel.setText("Name: " + journalMetaData.getString("journal_name"));
                durationLabel.setText("Duration: " + Integer.toString(journalMetaData.getInt("total_duration")));
                numEntriesLabel.setText("Num entries: " + journalMetaData.getString("num_entries"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            
            JTextArea textArea = new JTextArea();
            textArea.setText(entryText);
            textArea.setEditable(false);
            add(textArea);
            
            String borderTitle = "Entry id: " + entryID + " Date: " + date + " Duration: " + duration;
            setBorder(BorderFactory.createTitledBorder(borderTitle));
            
            JButton editButton = new JButton("Edit");
            editButton.addActionListener(this);
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(this);
            add(editButton);
            add(deleteButton);
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Edit")) {
                System.out.println("edit button for id " + entryID);
            } else if (e.getActionCommand().equals("Delete")) {
                System.out.println("delete button for id " + entryID);
                delete();
            }
        }
        
        private void delete() {
            // prompt user to make sure they really do want to delete this journal entry
            // prompt here
            // delete the journal entry from the database
            DataAccessObject dao = DataAccessObject.getInstance();
            dao.deleteJournalEntry(entryID);
            // either refresh the data for the entire panel or just remove this specific JournalViewPanel 
            refreshData();
            // inform any listeners (e.g. the JournalsTableModel for the JournalsPanel) that this journal's
            // data has been changed.
            for (JournalDataChangeListener listener : listeners) {
                listener.dataChanged();
            }
        }
    }
}
