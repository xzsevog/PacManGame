import javax.swing.*;

public class MazeGenerator {
    public static Integer size;

    public static int[][]  createMaze() {
        while (true) {
            size =  Integer.parseInt(JOptionPane.showInputDialog(null, "Enter maze size (10-50):"));
            try {
                if (size < 10 || size > 50) {
                    throw new NumberFormatException();
                }
                break;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid integer between 10 and 50.");
            }
        }

        int[][] maze = new int[size][size];

        // Set 1 in the first row
        for (int col = 0; col < size; col++) {
            maze[0][col] = 1;
        }

// Set 1 in the last row
        for (int col = 0; col < size; col++) {
            maze[size-1][col] = 1;
        }

// Set 1 in the first column
        for (int row = 0; row < size; row++) {
            maze[row][0] = 1;
        }

// Set 1 in the last column
        for (int row = 0; row < size; row++) {
            maze[row][size - 1] = 1;
        }


        // Fill remaining cells with 2's
        for (int i = 1; i < size-1 ; i++) {
            for (int j = 1; j < size-1 ; j++) {
                maze[i][j] = 2;
            }
        }

        // fill remaining cells with 1's every second index
        for (int i = 2; i < size-1 ; i += 2) {
            for (int j = 2; j < size-1 ; j += 2) {
                maze[i][j] = 1;
            }
        }

        int PACMAN = 3;
        maze[1][1] = PACMAN;

        // Place enemies at (size/2, size/2-1) and (size/2, size/2+1)
        int middle = size / 2;
        int ENEMY = 4;
        maze[middle][middle] = ENEMY;
        maze[middle][middle - 1] = ENEMY;
        maze[middle][middle + 1] = ENEMY;

        // Print maze
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(maze[i][j] + " ");
            }
            System.out.println();
        }
    return maze;}

}


