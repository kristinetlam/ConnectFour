// This file holds all the game logic (placing a chip, winning, etc.)

package com.example.connectfour;

import java.io.*;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConnectFourGame implements Serializable {

    private static final int COLUMNS = 7;
    private static final int ROWS = 6;

    /* Implement hashmap data structure: HashMap<key,value>
        The keys are the column indices and the values are stacks
        Each key will point to a value which is a stack that holds the chips in that specific column
     */
    private HashMap<Integer, Stack<Integer>> game;

    // Create C4 grid
    public ConnectFourGame() {
        game = new HashMap<>();
        for (int col = 0; col < COLUMNS; col++) {
            game.put(col, new Stack<>());
        }
    }

    public HashMap<Integer, Stack<Integer>> getGame() {
        return game;
    }
    private String aiDifficulty;

    public void setAIDifficulty(String difficulty) {
        this.aiDifficulty = difficulty;
    }

    public String getAIDifficulty() {
        return this.aiDifficulty;
    }

    public int makeRandomMove() {
        Random rand = new Random();
        int column;
        do {
            column = rand.nextInt(COLUMNS);
        } while (game.get(column).size() >= ROWS);
        return column;
    }

    // Place a chip into the stack, where the chip sinks to the bottom
    public boolean placeChip(int column, int player) {
        Stack<Integer> stack = game.get(column); // feed key to get value (specific stack)

        if (stack.size() >= ROWS) { // check if full
            return false;
        }

        stack.push(player);
        return true;
    }

    // WINNING GAME LOGIC -->

    private boolean gameWon = false;

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }


    // Check if the last move made by the player resulted in a win
    public boolean checkForWin(int lastColumnPlayed, int player) {
        // Check vertically, horizontally, and both diagonal directions
        gameWon = checkVertical(lastColumnPlayed, player) || checkHorizontal(player) ||
                checkDiagonal(player);

        return gameWon;
    }

    private boolean checkVertical(int column, int player) {
        Stack<Integer> stack = game.get(column);

        if (stack.size() < 4) { // if there's not enough chips to win
            return false;
        }

        // Check the top 4 chips in the current column
        int count = 0;
        for (int i = stack.size() - 1; i >= 0 && i >= stack.size() - 4; i--)
        {
            if (stack.get(i) == player) {
                count++;
            } else {
                break;
            }
        }
        return count >= 4;
    }

    private boolean checkHorizontal(int player) {

        for (int row = 0; row < ROWS; row++) {
            int count = 0;

            for (int col = 0; col < COLUMNS; col++)
            {
                count = (getPlayerAt(col, row) == player) ? (count + 1) : 0;
                if (count >= 4) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkDiagonal(int player) {
        // Check for both left diagonal and right diagonal
        return checkDiagonalDirection(player, 1) || checkDiagonalDirection(player, -1);
    }

    private boolean checkDiagonalDirection(int player, int direction) {
        for (int col = 0; col < COLUMNS; col++) {
            for (int row = 0; row < ROWS; row++) {
                int count = 0;
                for (int i = 0; i < 4; i++) {
                    int currentRow = row + i;
                    int currentCol = col + (i * direction);
                    if (currentRow >= ROWS || currentCol < 0 || currentCol >= COLUMNS) {
                        break;
                    }
                    if (getPlayerAt(currentCol, currentRow) == player) {
                        count++;
                    } else {
                        break;
                    }
                }
                if (count >= 4) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getPlayerAt(int col, int row) {
        Stack<Integer> stack = game.get(col);
        if (row < stack.size()) {
            return stack.get(row);
        }
        return -1; // Indicates no player
    }

    // AI LOGIC IMPLEMENTATIONS
    public int makeAIMove(int aiPlayer, int humanPlayer) {
        // Check for a winning move for the AI
        for (int col = 0; col < COLUMNS; col++) {
            if (canWinNextMove(col, aiPlayer)) {
                return col;
            }
        }

        // Check for a blocking move
        for (int col = 0; col < COLUMNS; col++) {
            if (canWinNextMove(col, humanPlayer)) {
                return col;
            }
        }

        // Otherwise, make a strategic move
        return makeStrategicMove(aiPlayer);
    }

    private boolean canWinNextMove(int column, int player) {
        // Simulate placing a chip in the column and check for a win
        Stack<Integer> stack = game.get(column);

        if (stack.size() >= ROWS) {
            return false;
        }

        stack.push(player);
        boolean canWin = checkForWin(column, player);
        stack.pop();

        return canWin;
    }

    private int makeStrategicMove(int player) {

        int[] preferredColumns = {3, 2, 4, 1, 5, 0, 6};
        for (int col : preferredColumns) {
            if (game.get(col).size() < ROWS) {
                return col;
            }
        }
        return 0; // Default fallback, should not normally reach here
    }


    public void resetGame() {
        for (int col = 0; col < COLUMNS; col++) {

            Stack<Integer> stack = game.get(col);
            if (stack.empty()) {
                continue;
            }
            stack.clear();
        }
    }

    private static final long serialVersionUID = 1L;

    public void saveGame(String filename) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = now.format(formatter);
        String filePath = filename + "_" + timestamp + ".dat";

        // save this.game to load the hashmap later by writing the obj
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(this.game);
        }
    }

    public void loadGame(String filePath) throws IOException, ClassNotFoundException {
        // load in the game obj from file
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            this.game = (HashMap<Integer, Stack<Integer>>) in.readObject();
        }
    }

}