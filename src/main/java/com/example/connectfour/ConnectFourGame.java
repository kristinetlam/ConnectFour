// This file holds all the game logic (placing a chip, winning, etc.)

package com.example.connectfour;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ConnectFourGame implements Serializable {

    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final long serialVersionUID = 1L;

    public static class Move implements Serializable {
        private final int player;
        private final int column;

        public Move(int player, int column) {
            this.player = player;
            this.column = column;
        }

        public int getPlayer() {
            return player;
        }

        public int getColumn() {
            return column;
        }
    }

    /* Implement hashmap data structure: HashMap<key,value>
        The keys are the column indices and the values are stacks
        Each key will point to a value which is a stack that holds the chips in that specific column
     */
    private HashMap<Integer, Stack<Integer>> game;
    private List<Move> move_log = new ArrayList<>();

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
        move_log.add(new Move(player, column));
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

    public boolean isBoardFull() {
        for (Map.Entry<Integer, Stack<Integer>> entry : game.entrySet()) {
            if (entry.getValue().size() < ROWS) {
                return false; // If any column is not full, return false
            }
        }
        return true; // All columns are full
    }

    // Check if the last move made by the player resulted in a win
    public boolean checkForWin(int lastColumnPlayed, int player) {
        // check all directions
        gameWon = checkVertical(lastColumnPlayed, player) || checkHorizontal(player) ||
                checkDiagonal(player);

        return gameWon;
    }

    private boolean checkVertical(int column, int player) {
        Stack<Integer> stack = game.get(column);

        if (stack.size() < 4) { // if there's not enough chips to win
            return false;
        }

        // Check the top 4 chips in the current column, since it'll never be in the middle or bottom of a stack
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

                    // if out of range, break
                    if (currentRow >= ROWS || currentCol < 0 || currentCol >= COLUMNS) {
                        break;
                    }
                    if (getPlayerAt(currentCol, currentRow) == player) { // if exact chip is player's chip
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
            return stack.get(row); // returns exact chip
        }
        return -1; // indicates no player
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
        // simulate placing a chip in the column and check for a win
        Stack<Integer> stack = game.get(column);

        if (stack.size() >= ROWS) { // if full
            return false;
        }

        stack.push(player);
        boolean canWin = checkForWin(column, player);
        stack.pop(); // pop to ensure simulation

        return canWin;
    }

    private int makeStrategicMove(int player) {

        int[] preferredColumns = {3, 2, 4, 1, 5, 0, 6}; // highest to lowest winning chance columns
        for (int col : preferredColumns) {
            if (game.get(col).size() < ROWS) {
                return col;
            }
        }
        return 0;
    }


    public void resetGame() {
        move_log.clear();

        for (int col = 0; col < COLUMNS; col++) {

            Stack<Integer> stack = game.get(col);
            if (stack.empty()) {
                continue;
            }
            stack.clear();
        }
    }

    public void saveGame(String filename) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = now.format(formatter);
        String filePath = filename + "_" + timestamp + ".txt";

        // for every move in move_log, write to the txt file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Move move : this.move_log) {
                writer.write("Player " + move.getPlayer() + ", Column " + move.getColumn());
                writer.newLine();
            }
        }
    }

    public void loadGame(String filePath) throws IOException, ClassNotFoundException {
            resetGame();

            // open txt file and parse the lines to load the game
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    // split into player and column using ", "
                    String[] parts = line.split(", ");
                    if (parts.length == 2) {
                        String playerPart = parts[0]; // "Player X"
                        String columnPart = parts[1]; // "Column Y"

                        int player = Integer.parseInt(playerPart.replace("Player ", ""));
                        int column = Integer.parseInt(columnPart.replace("Column ", ""));

                        placeChip(column, player);
                    }
                }
            }
        }
    }
