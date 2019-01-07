package cz.loglim.smp.client.controllers;

import cz.loglim.smp.client.Main;
import cz.loglim.smp.client.gui.GGrid;
import cz.loglim.smp.client.gui.GameView;
import cz.loglim.smp.client.net.ServerConnection;
import cz.loglim.smp.dto.logic.Direction;
import cz.loglim.smp.dto.logic.GameData;
import cz.loglim.smp.dto.logic.Player;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

// Logging note: log output prefixed with ">" means local, ">>" means sent to remote, "<<" means received from remote
// status codes in "[]" are OK ~ Success, WRN ~ Warning, ERR ~ Error

public class GameRoomController {

    @FXML
    Canvas gameCanvas;
    @FXML
    Button returnButton;
    @FXML
    Label messageLabel, hintLabel,
            playerLabel1, playerLabel2,
            playerLabel3, playerLabel4,
            playerLabel5, playerLabel6,
            playerLabel7, playerLabel8;

    // Constants
    private static final KeyCode KEY_LEFT = KeyCode.LEFT;
    private static final KeyCode KEY_RIGHT = KeyCode.RIGHT;
    private static final KeyCode KEY_UP = KeyCode.UP;
    private static final KeyCode KEY_DOWN = KeyCode.DOWN;
    private static final Logger log = LoggerFactory.getLogger(GameRoomController.class);

    // Private
    private static AnimationTimer updateTimer;
    private static GameView gameView;
    private static Label[] playerLabels;

    public GameRoomController() {
        gameView = new GameView();
    }

    @FXML
    void initialize() {
        // Setup player labels
        playerLabels = new Label[]{playerLabel1, playerLabel2, playerLabel3, playerLabel4, playerLabel5, playerLabel6, playerLabel7, playerLabel8};

        ServerConnection.receiveInitialData();
        setup();

        int w = ServerConnection.getGameData().getGrid().getW();
        int h = ServerConnection.getGameData().getGrid().getH();

        gameView.setGridSize(w, h);
        gameView.setResolution(w * GGrid.CELL_SIZE, h * GGrid.CELL_SIZE);
        gameView.setGameData(ServerConnection.getGameData());
        hintLabel.setText(
                String.format("Use keys {%s, %s, %s, %s} to move snake in target direction", KEY_LEFT, KEY_RIGHT,
                        KEY_UP, KEY_DOWN));

        System.out.println("> Game room initialization [OK]");
        log.info("Game room initialization [OK]");
    }

    private void setup() {
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();

            if (code == KEY_LEFT) {
                ServerConnection.setTargetDirection(Direction.left);
            } else if (code == KEY_RIGHT) {
                ServerConnection.setTargetDirection(Direction.right);
            } else if (code == KEY_UP) {
                ServerConnection.setTargetDirection(Direction.up);
            } else if (code == KEY_DOWN) {
                ServerConnection.setTargetDirection(Direction.down);
            }
        });

        showAllPlayersInfo();
        gameCanvas.setWidth(ServerConnection.getGameData().getGrid().getW() * GGrid.CELL_SIZE);
        gameCanvas.setHeight(ServerConnection.getGameData().getGrid().getH() * GGrid.CELL_SIZE);
        startAnimation();
    }

    public void stopGame() {
        updateTimer.stop();
        ServerConnection.disconnect();
        System.out.println("> Game stopped [OK]");
        log.info("Game stopped [OK]");
    }

    private void startAnimation() {
        updateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gameUpdate();
            }
        };
        updateTimer.start();
    }

    private void gameUpdate() {
        if (!ServerConnection.isConnected()) {
            updateTimer.stop();
            showResults();
            Platform.runLater(() -> returnButton.setVisible(true));
        }

        // Wait for the server connection to receive new data
        if (!ServerConnection.hasReceivedData()) {
            return;
        }

        // Update UI
        gameCanvas.requestFocus();
        gameView.draw(gameCanvas.getGraphicsContext2D());
        showAllPlayersInfo();

        if (ServerConnection.getGameData().getState() == GameData.State.gameOver) {
            updateTimer.stop();
            showResults();
            Platform.runLater(() -> returnButton.setVisible(true));
        }
    }

    private void showResults() {
        GameData.State state = ServerConnection.getGameData().getState();
        if (!ServerConnection.isConnected() || state == GameData.State.disconnected) {
            showErrorMessage("Warning:\nConnection to\nserver lost!");
            log.warn("Connection to server lost!");
        } else if (state == GameData.State.gameOver) {
            if (ServerConnection.getResultPosition() == 1) {
                showGameMessage(
                        String.format("Congratulations, %s,\nYou have won!", ServerConnection.getPlayerNickname()),
                        true);
                log.info("You have won! {}", ServerConnection.getPlayerNickname());
            } else {
                int pos = ServerConnection.getResultPosition();
                String posText = pos == 2 ? "2nd" : String.format("%dth", pos);
                showGameMessage(
                        String.format("Game over!\n%s, you are %s", ServerConnection.getPlayerNickname(), posText),
                        false);
                log.info("Game over");
            }
        }
        messageLabel.setVisible(true);
    }

    private void showErrorMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.RED);
        messageLabel.getStyleClass().add("error");
    }

    private void showGameMessage(String message, boolean positive) {
        messageLabel.setText(message);
        messageLabel.setTextFill(positive ? Color.GREENYELLOW : Color.INDIANRED);
        messageLabel.getStyleClass().add(positive ? "good" : "bad");
    }

    private void showAllPlayersInfo() {
        // Init player score info
        List<Player> tmpPlayerList = new ArrayList<>();
        Player localPlayer = null;
        for (Player player : ServerConnection.getGameData().getPlayers()) {
            if (player.getId() == GameData.getLocalPlayerId()) {
                localPlayer = player;
            } else {
                tmpPlayerList.add(player);
            }
        }
        tmpPlayerList.add(0, localPlayer);

        for (int i = 0; i < tmpPlayerList.size(); i++) {
            if (tmpPlayerList.get(i) == null) continue;

            showPlayerScore(tmpPlayerList.get(i), i);
        }
    }

    private void showPlayerScore(Player player, int id) {
        playerLabels[id].setVisible(true);
        String name = player.getName();
        if (name.length() > 12) {
            name = name.substring(0, 10) + "..";
        }
        playerLabels[id].setText(String.format("%-12s %d", name, player.getScore()));
        Color color = GameView.getPlayerColor(player.getId());
        playerLabels[id].setStyle(
                String.format(
                        "-fx-background-color: rgba(%s, %s, %s, 1); -fx-effect: dropshadow(gaussian, rgba(%s, %s, %s, 1), 12, 0, 0, 0);",
                        color.getRed() * 160, color.getGreen() * 160, color.getBlue() * 160,
                        color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255));

        if (player.isDisconnected()) {
            playerLabels[id].setDisable(true);
        }
    }

    public void onReturnButton() {
        ServerConnection.disconnect();
        Main.loadScene("Layout/Lobby.fxml");
    }

}
