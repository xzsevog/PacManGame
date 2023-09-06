import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameEndWindow extends JFrame implements Serializable {

    private static final long serialVersionUID = 1L;


    JTextField nicknameField;
    JPanel panel;
    JLabel messageLabel;
    JButton okButton;
    List<String> players;


    //    Action keyStrokeClosing;
    public GameEndWindow() {

        super("Game Over");

        ImageIcon icon = new ImageIcon("ghost.png");
        JLabel iconLabel = new JLabel(icon);


        players = new ArrayList<String>();

        // Load players list from file, if it exists
        try {
            FileInputStream fileIn = new FileInputStream("players.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            players = (List<String>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            // If file does not exist, do nothing
        } catch (ClassNotFoundException c) {
            System.out.println("Players class not found");
            c.printStackTrace();
            return;
        }


        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 200);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        panel = new JPanel();
        messageLabel = new JLabel("Enter your nickname:");
        nicknameField = new JTextField(20);
        okButton = new JButton("OK");

        panel.add(iconLabel);
        panel.add(messageLabel);
        panel.add(nicknameField);
        panel.add(okButton);
        this.add(panel);


        getContentPane().add(panel);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String nickname = nicknameField.getText();
                players.add(nickname);
                dispose();
                Menu menu = new Menu();
                System.out.println(players);

                // Save players list to file
                try {
                    FileOutputStream fileOut = new FileOutputStream("players.ser");
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(players);
                    out.close();
                    fileOut.close();
                } catch (IOException i) {
                    i.printStackTrace();
                }
            }
        });
    }
}