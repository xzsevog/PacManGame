import javax.swing.*;

public class main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()-> new Menu()); // konieczne ze wzgledow bezpieczenstwa i flow aplikacji
//        SwingUtilities.invokeLater(()-> new GameEndWindow());
//        SwingUtilities.invokeLater(()-> new HighScoreList());
    }
}
