package main;

import gui.CardsPanel;
import gui.JournalViewPanel;
import gui.JournalsPanel;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main {

    public static void main(String[] args) {
        JFrame window = new JFrame("Learning Journal");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardsPanel cardsPanel = new CardsPanel();
        JournalViewPanel journalViewPanel = new JournalViewPanel(cardsPanel);
        JPanel journalsPanel = new JournalsPanel(cardsPanel, journalViewPanel);
        
        cardsPanel.addCard(journalsPanel, "journals");
        cardsPanel.addCard(journalViewPanel, "journal view");

        window.setContentPane(cardsPanel);
        window.pack();
        window.setVisible(true);
    }
}
