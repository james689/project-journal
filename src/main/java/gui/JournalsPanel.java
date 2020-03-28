package gui;

import core.DataAccessObject;
import java.awt.CardLayout;
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
    private CardsPanel cardsPanel;
    private JournalViewPanel journalViewPanel;

    public JournalsPanel(CardsPanel cardsPanel, JournalViewPanel journalViewPanel) {
        this.cardsPanel = cardsPanel;
        this.journalViewPanel = journalViewPanel;
        
        dao = DataAccessObject.getInstance();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        journalsTableModel = new JournalsTableModel();
        journalViewPanel.addDataChangeListener(journalsTableModel);
        journalsTable = new JTable(journalsTableModel);
        JScrollPane journalsTableScrollPane = new JScrollPane(journalsTable);
        journalsTable.setPreferredScrollableViewportSize(new Dimension(900, 300));

        JPanel sortByPanel = new JPanel();
        sortByComboBox = new JComboBox(DataAccessObject.SortJournalBy.values());
        sortByComboBox.setSelectedItem(journalsTableModel.getDataSortingMethod());
        sortByComboBox.addActionListener(new SortByComboBoxListener());
        sortByPanel.add(new JLabel("Sort By: "));
        sortByPanel.add(sortByComboBox);

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
        add(journalsTableScrollPane);
        add(buttonsPanel);
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
            int selectedRow = journalsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "no journal selected");
                return;
            }

            dao.deleteJournal(journalsTableModel.getJournalID(selectedRow));
            journalsTableModel.updateData(); // table model needs to be told the
            // data it is to display has changed
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
            // data it is to display has changed
            //System.out.println("sort by = " + sortBy);
        }
    }
}
