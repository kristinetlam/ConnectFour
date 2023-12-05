package com.example.connectfour;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Stack;

public class ConnectFourUI extends Application {

    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int CIRCLE_DIAMETER = 80;
    private static final Color EMPTY_SLOT = Color.rgb(200, 200, 200, 0.5);
    private static final Color BOARD_COLOR = Color.rgb(31, 58, 147);

    private ConnectFourGame game;
    private GridPane gridPane;
    private Stage mainStage;

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        startNewGame();
    }

    private void showAIDifficultyMenu() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Choose AI Difficulty");
        alert.setHeaderText("Select the difficulty level of the AI");

        ButtonType buttonEasy = new ButtonType("Easy");
        ButtonType buttonHard = new ButtonType("Hard");

        alert.getButtonTypes().setAll(buttonEasy, buttonHard);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonEasy) {
            game.setAIDifficulty("easy");
        } else {
            game.setAIDifficulty("hard");
        }
    }

    public void startNewGame() {
        game = new ConnectFourGame();
        showAIDifficultyMenu();
        currentPlayer = 1;

        gridPane = createGridPane();
        VBox buttonBox = createButtonBox();
        HBox centerBox = new HBox(gridPane, buttonBox);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setSpacing(50);

        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(20));
        borderPane.setCenter(centerBox);

        Scene scene = new Scene(borderPane, (CIRCLE_DIAMETER + 10) * COLUMNS + 300, (CIRCLE_DIAMETER + 10) * ROWS + 150);
        mainStage.setScene(scene);
        mainStage.show();
    }

    private GridPane createGridPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(20);
        grid.setPadding(new Insets(15));
        grid.setBackground(new Background(new BackgroundFill(BOARD_COLOR, new CornerRadii(10), Insets.EMPTY)));

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                Circle circle = new Circle(CIRCLE_DIAMETER / 2);
                circle.setFill(EMPTY_SLOT);

                StackPane stackPane = new StackPane(circle);
                stackPane.setAlignment(Pos.CENTER);
                stackPane.setStyle("-fx-background-color: transparent;");

                final int column = col;
                stackPane.setOnMouseClicked(event -> playerTurns(column));

                grid.add(stackPane, col, row);
            }
        }

        return grid;
    }

    private VBox createButtonBox() {
        VBox buttonBox = new VBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnSave = new Button("Save");
        Button btnLoad = new Button("Load");
        Button btnReset = new Button("Reset");

        btnSave.setOnAction(event -> saveGame());
        btnLoad.setOnAction(event -> loadGame());
        btnReset.setOnAction(event -> resetGame());

        buttonBox.getChildren().addAll(btnSave, btnLoad, btnReset);
        BorderPane.setMargin(buttonBox, new Insets(0, 0, 0, 20));

        return buttonBox;
    }

    private static final Color PLAYER_ONE_COLOR = Color.rgb(185, 0, 0);
    private static final Color PLAYER_TWO_COLOR = Color.rgb(255, 223, 0);
    private int currentPlayer = 1;

    private volatile boolean playerMoved = false;

    // Chip placement with player turns, winning game logic, and AI interlinked with UI
    private void playerTurns(int column) {

        if (game.isGameWon()) {
            showAlert("Game Over", "The game has already been won. Please start a new game or reset.");
            return;
        }

        // Check for draw condition after each turn
        if (game.isBoardFull() && !game.isGameWon()) {
            showAlert("Game Over", "The game is a draw!");
            return;
        }


        // Human player's turn
        boolean success = game.placeChip(column, currentPlayer);
        if (success) {
            updateUI();

            if (game.checkForWin(column, currentPlayer)) {
                showAlert("Game Over", "Player " + currentPlayer + " wins!");
                return;
            }
            playerMoved = true;
            currentPlayer = 2; // Switch to AI player

            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    if (playerMoved) {
                        Platform.runLater(() -> {
                            int aiMove = 0;
                            if (game.getAIDifficulty().equals("easy")) {
                                aiMove = game.makeRandomMove();
                            } else {
                                aiMove = game.makeAIMove(2, 1);
                            }
                            // AI's turn
                            game.placeChip(aiMove, currentPlayer);
                            updateUI();

                            if (game.checkForWin(aiMove, currentPlayer)) {
                                showAlert("Game Over", "AI wins!");
                                return;
                            }
                            currentPlayer = 1; // Switch back to human player
                            playerMoved = false;
                        });
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } else {
            showAlert("Column Full", "You cannot place a chip here as the column is full. Please try again.");
        }

    }

    // Update the game whenever a chip is placed
    private void updateUI() {
        for (int col = 0; col < COLUMNS; col++)
        {
            Stack<Integer> stack = game.getGame().get(col);

            for (int row = 0; row < stack.size(); row++) {
                int player = stack.get(row);
                StackPane pane = (StackPane) gridPane.getChildren().get((ROWS - 1 - row) * COLUMNS + col);

                Circle circle = (Circle) pane.getChildren().get(0);
                circle.setFill(player == 1 ? PLAYER_ONE_COLOR : PLAYER_TWO_COLOR);
            }
        }
    }

    // Create alert tab that opens when a player wins, validation error, save/load, etc.
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void saveGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Game");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Connect Four Saves", "*.txt"));
        fileChooser.setInitialFileName("C4Save_" + getCurrentTimestamp() + ".txt"); // Suggested file name

        File file = fileChooser.showSaveDialog(mainStage);
        if (file != null) {
            try {
                game.saveGame(file.getAbsolutePath());
                showAlert("Game Saved", "Your game has been saved successfully.");
            } catch (IOException ex) {
                showAlert("Save Error", "Error saving the game: " + ex.getMessage());
            }
        }
    }

    private String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return now.format(formatter);
    }

    private void loadGame() {
        game.resetGame();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Game");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Connect Four Saves", "*.txt"));

        File file = fileChooser.showOpenDialog(mainStage);
        if (file != null) {
            try {
                game.loadGame(file.getAbsolutePath());
                updateUI();
                showAlert("Game Loaded", "Your game has been loaded successfully.");
            } catch (IOException | ClassNotFoundException ex) {
                showAlert("Load Error", "Error loading the game: " + ex.getMessage());
            }
        }
    }

    public void resetGame() {
        game.resetGame(); // reset hashmap and move log
        resetBoardUI(); // then reset UI itself
        game.setGameWon(false);
    }

    private void resetBoardUI() {
        Platform.runLater(() -> {

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLUMNS; col++)
                {
                    StackPane stackPane = (StackPane) getNodeFromGridPane(gridPane, col, row);

                    // if chip is there, empty it
                    if (stackPane != null)
                    {
                        Circle circle = (Circle) stackPane.getChildren().get(0);
                        circle.setFill(EMPTY_SLOT);
                    }
                }
            }
        });
    }

    // check if there is a chip in column by looping through the grid pane (UI)
    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == col &&
                    GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}