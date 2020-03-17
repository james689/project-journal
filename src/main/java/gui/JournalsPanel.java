package gui;

import core.DataAccessObject;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class JournalsPanel extends JPanel {

    private DataAccessObject dao;
    private JournalsTableModel journalsTableModel;

    public JournalsPanel() {
        dao = DataAccessObject.getInstance();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        journalsTableModel = new JournalsTableModel();
        JTable journalsTable = new JTable(journalsTableModel);
        JScrollPane journalsTableScrollPane = new JScrollPane(journalsTable);
        journalsTable.setPreferredScrollableViewportSize(new Dimension(900, 300));

        JButton createNewJournalButton = new JButton("Create New Journal");
        createNewJournalButton.addActionListener(new CreateNewJournalButtonListener());
        
        JButton deleteJournalButton = new JButton("Delete journal");
        deleteJournalButton.addActionListener(new DeleteJournalButtonListener());

        add(journalsTableScrollPane);
        add(createNewJournalButton);
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
            journalsTableModel.updateData(); // table model needs to be told there
            // is new data to display
        }
    }
    
    public class DeleteJournalButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            
        }
    }
}
