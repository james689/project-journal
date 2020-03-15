package gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class JournalsPanel extends JPanel {
    
    public JournalsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JournalsTableModel journalsTableModel = new JournalsTableModel();
        JTable journalsTable = new JTable(journalsTableModel);
        JScrollPane journalsTableScrollPane = new JScrollPane(journalsTable);
        journalsTable.setPreferredScrollableViewportSize(new Dimension(900, 300));
        
        JButton createNewJournalButton = new JButton("Create New Journal");
        createNewJournalButton.addActionListener(new CreateNewJournalButtonListener());
        
        add(journalsTableScrollPane);
        add(createNewJournalButton);
    }
    
    public class CreateNewJournalButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.out.println("create new journal button clicked");
        }
    }
}
