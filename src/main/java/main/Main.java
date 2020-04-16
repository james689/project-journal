package main;

import gui.CardsPanel;
import gui.JournalEntriesScreen;
import gui.JournalsScreen;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main {

    public static void main(String[] args) {
        JFrame window = new JFrame("Project Journals");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardsPanel cardsPanel = new CardsPanel();
        JournalEntriesScreen journalEntriesScreen = new JournalEntriesScreen(cardsPanel);
        JPanel journalsScreen = new JournalsScreen(cardsPanel, journalEntriesScreen);
        
        cardsPanel.addCard(journalsScreen, "journals");
        cardsPanel.addCard(journalEntriesScreen, "journal view");

        window.setContentPane(cardsPanel);
        window.pack();
        window.setVisible(true);
    }
}
