// This file holds all the game logic (placing a chip, winning, etc.)

package com.example.connectfour;

import java.util.HashMap;
import java.util.Stack;

public class ConnectFourGame {

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

    // Place a chip into the stack, where the chip sinks to the bottom
    public boolean placeChip(int column, int player) {
        Stack<Integer> stack = game.get(column); // feed key to get value (specific stack)

        if (stack.size() >= ROWS) { // check if full
            return false;
        }

        stack.push(player);
        return true;
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

}