import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;

public class HighScoreList extends JFrame {

    JList jList;
    MyListModel myListModel;

    public HighScoreList() {

        List<String> players = null;
        try {
            FileInputStream fileIn = new FileInputStream("players.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            players = (List<String>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }


        myListModel = new MyListModel(players);

        jList = new JList(myListModel);
        JScrollPane jScrollPane = new JScrollPane(jList);
        jList.setCellRenderer(new MyListCellRenderer());
        this.add(jScrollPane);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 200);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }



}

class MyListModel extends AbstractListModel<String> {

    private List<String> items;

    public MyListModel(List<String> items) {
        this.items = items;
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public String getElementAt(int index) {
        return items.get(index);
    }
}

class MyListCellRenderer implements ListCellRenderer<String>{

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        return new JButton(value);
    }
}
