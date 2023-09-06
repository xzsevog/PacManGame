import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Menu implements ActionListener {

   JFrame frame = new JFrame();
   JPanel panel = new JPanel();
   JButton newGame = new JButton("New Game");
   JButton highScore = new JButton("High Score");
   JButton exit = new JButton("Exit");
   ImageIcon icon = new ImageIcon("pacman.png");



    Menu(){

        newGame.setIcon(icon);
        newGame.setContentAreaFilled(false);
        newGame.setBackground(Color.LIGHT_GRAY);
        newGame.setBorderPainted(false);
        newGame.setOpaque(true);



        newGame.addActionListener(this);
        highScore.addActionListener(this);
        exit.addActionListener(this);


        panel.setLayout(new BorderLayout());
        panel.add(newGame,BorderLayout.CENTER);
        panel.add(highScore,BorderLayout.PAGE_START);
        panel.add(exit,BorderLayout.PAGE_END);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==newGame){
            frame.dispose();
            new GameBoard(); // czy gdzie tam bedzie cala gra
        } else if (e.getSource()==highScore) {
            frame.dispose();
            new HighScoreList();
        } else if (e.getSource()==exit) {
            frame.dispose();
        }
    }
}
