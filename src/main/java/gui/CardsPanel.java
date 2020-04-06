package gui;

import java.awt.CardLayout;
import javax.swing.JPanel;

/**
 * This panel contains the "cards" (the screens in the GUI) that
 * can be flipped between.
 */
public class CardsPanel extends JPanel {
    
    private CardLayout cardLayout;
    
    public CardsPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);
    }
    
    public void addCard(JPanel card, String name) {
        add(card, name);
    }
    
    public void switchToCard(String cardName) {
        cardLayout.show(this,cardName);
    }
}
