package main;

import gui.JournalsPanel;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Learning Journal");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel content = new JournalsPanel();
        window.setContentPane(content);
        window.pack();
        window.setVisible(true);
    }
}
