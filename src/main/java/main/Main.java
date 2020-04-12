package main;

import gui.CardsPanel;
import gui.JournalViewScreen;
import gui.JournalsScreen;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main {

    public static void main(String[] args) {
        //DataAccessObject dao = DataAccessObject.getInstance();
        /*JFrame window = new JFrame("Learning Journal");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardsPanel cardsPanel = new CardsPanel();
        JournalViewScreen journalViewPanel = new JournalViewScreen(cardsPanel);
        JPanel journalsPanel = new JournalsScreen(cardsPanel, journalViewPanel);
        
        cardsPanel.addCard(journalsPanel, "journals");
        cardsPanel.addCard(journalViewPanel, "journal view");

        window.setContentPane(cardsPanel);
        window.pack();
        window.setVisible(true);*/
        
        JFrame window = new JFrame("Learning Journal");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardsPanel cardsPanel = new CardsPanel();
        JournalViewScreen journalViewPanel = new JournalViewScreen(cardsPanel);
        JPanel journalsPanel = new JournalsScreen(cardsPanel, journalViewPanel);
        
        cardsPanel.addCard(journalsPanel, "journals");
        cardsPanel.addCard(journalViewPanel, "journal view");

        window.setContentPane(cardsPanel);
        window.pack();
        window.setVisible(true);
    }
}
