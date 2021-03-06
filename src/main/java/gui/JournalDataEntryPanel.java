package gui;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * This panel is used for inputting a new journal entry
 * or editing an existing journal entry. In the main program,
 * an instance of this data entry panel is created and put inside
 * a JDialog box when creating/editing a journal entry.
 */
public class JournalDataEntryPanel extends JPanel {
    
    private JTextField dateField;
    private JTextField durationField;
    private JTextArea entryTextArea;
    
    public JournalDataEntryPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        dateField = new JTextField(10);
        durationField = new JTextField(10);
        
        entryTextArea = new JTextArea(15,30);
        entryTextArea.setLineWrap(true);
        JScrollPane entryTextAreaScrollPane = new JScrollPane(entryTextArea);
        entryTextAreaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        entryTextAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        add(new JLabel("Date"));
        add(dateField);
        add(new JLabel("Duration (mins)"));
        add(durationField);
        add(new JLabel("Entry"));
        add(entryTextAreaScrollPane);
    }
    
    public String getDate() {
        return dateField.getText();
    }
    
    public void setDate(String date) {
        dateField.setText(date);
    }
    
    public String getDuration() {
        return durationField.getText();
    }
    
    public void setDuration(String duration) {
        durationField.setText(duration);
    }
    
    public String getEntry() {
        return entryTextArea.getText();
    }
    
    public void setEntry(String entry) {
        entryTextArea.setText(entry);
    }
}
