import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Arc2D;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class GameBoard  implements KeyListener{
    private final int rows;
    private final int cols;
    private int[][] maze;
    private JTable table;
    private JLabel scoreLabel;
    private JLabel lifesLabel;
    private static int score = 0;
    private static int lifes = 3;
    int scoreToWin;
    MazeTableModel mazeTableModel;


    Object lock = new Object();
    PacManMovementThread pacManMovementThread;


    public GameBoard() {
        this.maze = MazeGenerator.createMaze();
        this.rows = maze.length;
        this.cols = maze[0].length;
        this.mazeTableModel = new MazeTableModel();
        this.pacManMovementThread = new PacManMovementThread(this.maze,this.mazeTableModel, lock);
        scoreCounter(this.maze);
        System.out.println("score to win is " + this.scoreToWin);


        JFrame frame = new JFrame();
        JPanel pan = new JPanel();
        frame.setTitle("PacMan");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setBackground(Color.BLUE);

        // create JTable to display the maze
        table = new JTable(mazeTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setGridColor(Color.WHITE);
        table.setShowGrid(true);
        table.setDefaultEditor(Object.class, null); // disable editing
        table.setDefaultRenderer(Object.class, new CustomCellRenderer());
        Border border = BorderFactory.createLineBorder(Color.BLUE, 2);
        table.setBorder(border);
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setVisible(false);
        scoreLabel = new JLabel("Score: " + 0);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(scoreLabel, BorderLayout.SOUTH);
        lifesLabel = new JLabel("Lifes: " + 3);
        lifesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        frame.add(lifesLabel, BorderLayout.NORTH);


        frame.add(table, BorderLayout.CENTER);
        //set the table always as the size of the frame
        JScrollPane scrollPane = new JScrollPane(table);
        pan.add(new JScrollPane(table), BorderLayout.CENTER);
        scrollPane.setPreferredSize(new Dimension(5000, 5000));


        // set size and show
        frame.add(pan);
        frame.pack();
        frame.setVisible(true);
        table.setFocusable(true); // Set panel to be focusable to receive key events
//        table.addKeyListener(pacManMovementThread);
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                GameBoard.this.keyPressed(e);
            }});
        table.requestFocus();


        createGhosts();
        pacManMovementThread.start();
//        generateImprovements(this.mazeTableModel);

    }


    @Override
    public void keyPressed(KeyEvent e) {
        this.pacManMovementThread.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Pass the key typed event to the PacManMovementThread
         }
    private void createGhosts() {


        Ghost[] ghosts = {
                new Ghost(this.maze, this.mazeTableModel, this.maze.length/2, this.maze.length/2, lock),
                new Ghost(this.maze, this.mazeTableModel, this.maze.length/2, this.maze.length/2-1, lock),
                new Ghost(this.maze, this.mazeTableModel, this.maze.length/2, this.maze.length/2+1, lock),
        };

        for (Ghost ghost : ghosts) {
            Thread thread = new Thread(ghost);
            thread.start();
        }
    }
    public MazeTableModel getMazeTableModel() {
        return mazeTableModel;
    }

    public void scoreCounter(int[][] maze){
        for (int i = 0; i < maze.length ; i++) {
            for (int j = 0; j <maze[0].length ; j++) {
                if (maze[i][j] == 2){
                    this.scoreToWin+=1;
                }
            }
        }
    }

    public class MazeTableModel extends AbstractTableModel {


        @Override
        public int getRowCount() {
            return rows;
        }

        @Override
        public int getColumnCount() {
            return cols;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return maze[rowIndex][columnIndex];
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            maze[rowIndex][columnIndex] = (int) value;
            fireTableCellUpdated(rowIndex, columnIndex);
            table.repaint();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
    }

    public class CustomCellRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

            // Set the background color of the cell
            if (value instanceof Integer) {
                int intValue = (int) value;
                if (intValue == 1) {
                    c.setBackground(Color.BLUE);
                } else if (intValue == 2) {
                    c.setBackground(Color.WHITE);
                    return new OvalCellRenderer();
                } else if (intValue == 3 ) {
                    c.setBackground(Color.WHITE);
                    return new PacManCellRenderer();
                } else if (intValue == 4) {
                    c.setBackground(Color.WHITE);
                    return new EnemyCellRenderer();
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            if (!(value == null)) {
                setText("");
            }

            return c;
        }

        private class OvalCellRenderer extends JPanel {
            public OvalCellRenderer() {
                setOpaque(false);
            }

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.black);
                g.fillOval(9, 5, getWidth()/3, getHeight()/2 );

            }
        }
    }

    public class PacManMovementThread extends Thread implements KeyListener{
        private final int[][] maze;
        private GameBoard.MazeTableModel mazeTableModel;
        private Object lock;
        private boolean running;
        int newRow = 1;
        int newCol = 1;
        int currentRow = 1;
        int currentCol = 1;
        private boolean updateInProgress = false;



        public PacManMovementThread(int[][] maze, MazeTableModel mazeTableModel, Object lock) {
            this.maze = maze;
            this.mazeTableModel = mazeTableModel;
            this.lock = lock;
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            boolean ctrlPressed = e.isControlDown();
            boolean shiftPressed = e.isShiftDown();
            if (!updateInProgress) {
                updateInProgress = true;

                if (ctrlPressed && shiftPressed && key == KeyEvent.VK_Q) {
                    new Menu();
                } else {
                    switch (key) {
                        case KeyEvent.VK_UP:
                            this.newRow -= 1;
                            break;
                        case KeyEvent.VK_DOWN:
                            this.newRow += 1;
                            break;
                        case KeyEvent.VK_LEFT:
                            this.newCol -= 1;
                            break;
                        case KeyEvent.VK_RIGHT:
                            this.newCol += 1;
                            break;
                        default:
                            // Ignore keys other than arrows
                            updateInProgress = false;
                            return;
                    }
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    if (this.maze[newRow][newCol] ==3) {
                        this.mazeTableModel.setValueAt(3,newRow,newCol);
                    }else{
                        updatePacManPosition();
                    }
                }
                try {
                    Thread.sleep(100); // Adjust the delay as needed
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        private boolean isValidCell(int row, int col) {
            return row >= 0 && row < maze.length && col >= 0 && col < maze[row].length;
        }

        public void updatePacManPosition() {
            if (isValidCell(this.newRow,this.newCol)){
                System.out.println("new row " + this.newRow + " new col" + this.newCol);
                int currentCellValue = this.maze[this.newRow][this.newCol];
                switch (currentCellValue) {
                    case 0 -> { // Empty cell
                        this.mazeTableModel.setValueAt(3, this.newRow, this.newCol);
                        this.mazeTableModel.setValueAt(0, this.currentRow, this.currentCol);
                        PacManCellRenderer.setFacingDirectionAngle(calculateFacingDirectionAngle());
                        PacManCellRenderer.startMouthAnimation();
                        this.currentCol = this.newCol;
                        this.currentRow = this.newRow;
                    }
                    case 1 -> { // Wall
                        PacManCellRenderer.setFacingDirectionAngle(calculateFacingDirectionAngle());
                        PacManCellRenderer.startMouthAnimation();
                        this.newRow = this.currentRow;
                        this.newCol = this.currentCol;
                    }
                    case 2 -> { // Collectible
                        score++;
                        scoreLabel.setText("Score: " + score);
                        PacManCellRenderer.setFacingDirectionAngle(calculateFacingDirectionAngle());
                        PacManCellRenderer.startMouthAnimation();
                        if (score == scoreToWin) {
                            System.exit(0);
                        }
                        this.mazeTableModel.setValueAt(3, this.newRow, this.newCol);
                        this.mazeTableModel.setValueAt(0, this.currentRow, this.currentCol);
                        this.currentCol = this.newCol;
                        this.currentRow = this.newRow;
                    }
                    case 4 -> { // Ghost
                        // Collision with ghost, game over
                        lifes--;
                        if (lifes > 0 ) {
                            JOptionPane.showMessageDialog(null, "You lost a life! You have remining " + lifes, "Lost life", JOptionPane.INFORMATION_MESSAGE);
                            this.mazeTableModel.setValueAt(0, this.currentRow, this.currentCol);
                            this.newRow = 1;
                            this.newCol = 1;
                            this.currentCol = 1;
                            this.currentRow = 1;
                            this.mazeTableModel.setValueAt(3, this.newRow, this.newCol);
                        } else {
                            lifesLabel.setText("Lifes" + lifes);
//
                            JOptionPane.showMessageDialog(null, "Game Over! Your score is " + score, "Game Over", JOptionPane.INFORMATION_MESSAGE);

// Wait for the user to press Enter to proceed
                            JOptionPane.showInputDialog(null, "Press Enter to continue...");

// Exit the program
                            System.exit(0);
                        }
                    }
                }
            }

                // Set focus on the cell representing the Pac-Man's new position

            updateInProgress = false;
            lifesLabel.setText("Lifes: " + lifes);
        }

        private int calculateFacingDirectionAngle() {
            if (this.newRow < this.currentRow) {
                return 135; // Up
            } else if (this.newRow > this.currentRow) {
                return 315; // Down
            } else if (this.newCol < this.currentCol) {
                return 225; // Left
            } else if (this.newCol > this.currentCol) {
                return 45; // Right
            }
            return 0;
        }}


    protected static class PacManCellRenderer extends JPanel {
        private static int changer = 0;
        private  static int mouthAngle = 270;
        private static int facingDirectionAngle = 45;

        public static void setFacingDirectionAngle(int facingDirectionAngle1) {
          facingDirectionAngle = facingDirectionAngle1;
        }

        public static void setMouthAngle(int mouthAngle1) {
            mouthAngle = mouthAngle1;
        }



        @Override
        public void paintComponent(Graphics g ) {
            Graphics2D g2d = (Graphics2D) g;
            int facingDirectionAngle2 = facingDirectionAngle;
            int mouthAngle2 = mouthAngle;
            // Draw the pacman mouth
            g2d.setColor(Color.red);
            g2d.fill(new Arc2D.Double(1, 1, getWidth()-10, getHeight()-3, facingDirectionAngle2, mouthAngle2, Arc2D.PIE));
        }

        public static void startMouthAnimation() {
            if (changer == 0) {
                setMouthAngle(360);
                changer = 1;
            } else if (changer == 1) {
            setMouthAngle(270);
            changer = 0;
        }
    }}


    private static class EnemyCellRenderer extends JPanel {
        private Image ghostImage;

        public EnemyCellRenderer() {
            // Load the ghost image from the file
            try {
                ghostImage = ImageIO.read(new File("ghost.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            setOpaque(false);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
//
            // Draw the ghost image
            if (ghostImage != null) {
                g2d.drawImage(ghostImage, 7, 1, getWidth() - 20, getHeight() , null);
            }
        }
    }


    public static class Ghost implements Runnable {

        private int ghostRow;
        private int ghostCol;
        private int[][] maze;
        private MazeTableModel mazeTableModel;
        private Random random;
        private Object lock;
        int currentRowGhost;
        int currentColGhost;
        boolean hasThePreviousRowBeen0;
        boolean hasThePreviousRowBeen2;

        public Ghost(int[][] maze, MazeTableModel mazeTableModel, int ghostRow, int ghostCol, Object lock ) {
            this.ghostRow = ghostRow;
            this.ghostCol = ghostCol;
            this.maze = maze;
            this.mazeTableModel = mazeTableModel;
            this.random = new Random();
            this.lock = lock;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    moveGhost();
                }
                try {
                    Thread.sleep(1000); // Adjust the delay as needed
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

public void moveGhost() {

    // Generate a random direction (1-4) for the ghost to move
    int direction = random.nextInt(4) + 1;

    // Calculate the new position based on the random direction
    int newRowGhost =  ghostRow;
    int newColGhost = ghostCol;


    switch (direction) {
        case 1: // Move up
            newRowGhost--;
            break;
        case 2: // Move down
            newRowGhost++;
            break;
        case 3: // Move left
            newColGhost--;
            break;
        case 4: // Move right
            newColGhost++;
            break;
    }

    // Check if the new position is valid
    if (isValidMove(newRowGhost, newColGhost)) {
        if (ghostRow == (maze.length/2) && ghostCol == (maze.length/2) || ghostRow == (maze.length/2) && ghostCol == ((maze.length/2)-1) || ghostRow == (maze.length/2) && ghostCol == ((maze.length/2)+1)) {
            this.mazeTableModel.setValueAt(0,ghostRow, ghostCol);
            this.hasThePreviousRowBeen2 = true;
            changeThePreviousCell();
        }else {
            changeThePreviousCell();
        }
        // Update the maze table model
        if (this.maze[newRowGhost][newColGhost] == 2) {
            this.mazeTableModel.setValueAt(4,newRowGhost, newColGhost);
             hasThePreviousRowBeen2 = true;


        } else if (this.maze[newRowGhost][newColGhost] == 0) {
            this.mazeTableModel.setValueAt(4,newRowGhost, newColGhost);
             hasThePreviousRowBeen0 = true;

        }

        ghostRow = newRowGhost;
        ghostCol = newColGhost;
        currentRowGhost = newRowGhost;
        currentColGhost = newColGhost;
    }}


        private void changeThePreviousCell() {

            if (this.hasThePreviousRowBeen0){
                this.mazeTableModel.setValueAt(0, this.currentRowGhost, this.currentColGhost);
                this.hasThePreviousRowBeen0 = false;
                return;
            }
            if (this.hasThePreviousRowBeen2 && !(currentColGhost==0)&&!(currentRowGhost==0)){
                this.mazeTableModel.setValueAt(2,this.currentRowGhost, this.currentColGhost);
                this.hasThePreviousRowBeen2 = false;
                return;
            }

        }

        private boolean isValidMove(int row, int col) {
            int mazeHeight = maze.length;
            int mazeWidth = maze[0].length;

            // Check if the new position is within the maze boundaries
            if (row < 0 || row >= mazeHeight || col < 0 || col >= mazeWidth) {
                return false;
            }

            // Check if the new position is a wall or another ghost
            int cellValue = maze[row][col];
            return cellValue != 1 && cellValue != 4 && cellValue !=3;
        }
    }



    public static void main(String[] args) {
        GameBoard gameBoard = new GameBoard();

    }
}



