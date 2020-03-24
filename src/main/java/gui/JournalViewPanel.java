package gui;

import core.DataAccessObject;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JournalViewPanel extends JPanel {
    
    private CardsPanel cardsPanel;
    private int journalID; // id of journal the panel will display
    
    private JLabel nameLabel;
    private JLabel durationLabel;
    private JLabel numEntriesLabel;
    
    public JournalViewPanel(CardsPanel cardsPanel) {
        this.cardsPanel = cardsPanel;
        journalID = -1;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        nameLabel = new JLabel("tetris journal");
        durationLabel = new JLabel("duration: 8 hours 30 mins");
        numEntriesLabel = new JLabel("# entries: 6");
        
        JButton backToJournalsPanelButton = new JButton("back to journals");
        backToJournalsPanelButton.addActionListener(new BackToJournalsPanelButtonListener());
        
        add(nameLabel);
        add(durationLabel);
        add(numEntriesLabel);
        add(backToJournalsPanelButton);
    }
    
    public void setJournalID(int journalID) {
        this.journalID = journalID;
        
        DataAccessObject dao = DataAccessObject.getInstance();
        ResultSet journalData = dao.getJournalData(journalID);
        
        // redraw panel with new journal data
        try {
            while (journalData.next()) {
                nameLabel.setText("Name: " + journalData.getString("journal_name"));
                durationLabel.setText("Duration: " + Integer.toString(journalData.getInt("total_duration")));
                numEntriesLabel.setText("Num entries: " + journalData.getString("num_entries"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class BackToJournalsPanelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            cardsPanel.switchToCard("journals");
        }
    }
}
