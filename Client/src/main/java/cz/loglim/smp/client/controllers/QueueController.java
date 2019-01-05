package cz.loglim.smp.client.controllers;

import cz.loglim.smp.client.Main;
import cz.loglim.smp.client.net.ServerConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class QueueController {

    @FXML
    Button returnButton;
    @FXML
    Label queueLabel, nicknameLabel, countLabel, waitLabel;

    private boolean started;

    public QueueController() {
        System.out.println("> Trying to connect to server...");
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
            }
        });
    }

    private void startGame() {
        Platform.runLater(() -> {
            if (!started) {
                queueLabel.setText("Game started!");
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
