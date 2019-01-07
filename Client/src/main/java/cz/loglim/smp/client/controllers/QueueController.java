package cz.loglim.smp.client.controllers;

import cz.loglim.smp.client.Main;
import cz.loglim.smp.client.net.ServerConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueController {

    @FXML
    Button returnButton;
    @FXML
    Label queueLabel, nicknameLabel, countLabel, waitLabel;

    private boolean started;
    private static final Logger log = LoggerFactory.getLogger(QueueController.class);

    public QueueController() {
        System.out.println("> Trying to connect to server...");
        log.info("Trying to connect to server...");
        ServerConnection.setup();

        if (ServerConnection.isConnected()) {
            ServerConnection.playersInQueue.addListener((observable, oldValue, newValue) -> updateQueue());
            ServerConnection.gameStarted.addListener((observable, oldValue, newValue) -> startGame());
        }
    }

    @FXML
    public void initialize() {
        ServerConnection.setup();
        if (!ServerConnection.isConnected()) {
            nicknameLabel.setVisible(false);
            countLabel.setVisible(false);
            waitLabel.setVisible(false);
            queueLabel.setText("Cannot connect to server! Please try again later...");
            log.warn("Cannot connect to server! Please try again later");
        }
        nicknameLabel.setText(ServerConnection.getPlayerNickname());
    }

    private void updateQueue() {
        Platform.runLater(() -> {
            if (ServerConnection.isConnected()) {
                queueLabel.setText(String.format("%d / %d", ServerConnection.playersInQueue.get(),
                        ServerConnection.getPlayerLimit()));
            } else {
                queueLabel.setText("Connection to server lost! Please try again later...");
                log.warn("Connection to server lost! Please try again later!");
            }
        });
    }

    private void startGame() {
        Platform.runLater(() -> {
            if (!started) {
                queueLabel.setText("Game started!");
                log.info("Game started!");
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log.error("Thread error: {}", e.toString());
                }

                Main.loadScene("Layout/GameRoom.fxml");
                started = true;
            }
        });
    }

    public void onReturnButton() {
        ServerConnection.disconnect();
        Main.loadScene("Layout/Lobby.fxml");
    }

}
