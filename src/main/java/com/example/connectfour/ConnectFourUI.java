package com.example.connectfour;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;

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

    public void startNewGame() {
        game = new ConnectFourGame();
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
                stackPane.setOnMouseClicked(event -> playerTurns(column, circle));

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

    // Chip placement along with player turns
    private void playerTurns(int column, Circle circle)
    {
        boolean success = game.placeChip(column, currentPlayer); // check if we can place a chip in that column

        if (success) {
            updateUI();
            // if currentPlayer == 1, then stay 1. else, go to player 2
            currentPlayer = (currentPlayer == 1) ? 2 : 1; // Switch to the other player
        }
        else {
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void saveGame() {
        // Implement save functionality
    }

    private void loadGame() {
        // Implement load functionality
    }

    public void resetGame() {
        game.resetGame(); // reset hashmap
        resetBoardUI();
    }

    private void resetBoardUI() {
        Platform.runLater(() -> {
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLUMNS; col++) {
                    StackPane stackPane = (StackPane) getNodeFromGridPane(gridPane, col, row);
                    if (stackPane != null) {
                        Circle circle = (Circle) stackPane.getChildren().get(0);
                        circle.setFill(EMPTY_SLOT);
                    }
                }
            }
        });
    }

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
