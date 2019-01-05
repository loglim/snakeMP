package cz.loglim.smp.client;

import cz.loglim.smp.client.controllers.GameRoomController;
import cz.loglim.smp.client.net.ServerConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    // Private
    private static Main instance;
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        Main.primaryStage = primaryStage;
        primaryStage.setTitle("Snake MP");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("Image/icon.png"));
        loadScene("Layout/Lobby.fxml");

        // Make sure to be disconnected from the server before exit
        primaryStage.setOnCloseRequest(event -> {
            ServerConnection.disconnect();
            /*Platform.exit();
            System.exit(0);*/
        });

        primaryStage.show();

        // Close splash screen
        final SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash != null) {
            splash.close();
        }
    }

    public static void loadScene(String layoutUri) {
        instance.loadScn(layoutUri);
    }

    private void loadScn(String layoutUri) {
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource(layoutUri)));
        try {
            root = loader.load();
        } catch (IOException e) {
            System.out.println("Can't load scene " + layoutUri);
            e.printStackTrace();
        }

        if (layoutUri.toLowerCase().contains("gameData")) {
            GameRoomController controller = loader.getController();
            primaryStage.setOnHiding(event -> controller.stopGame());
        }

        Scene currentScene = null;
        if (root != null) {
            currentScene = new Scene(root, 640, 480);
        }
        primaryStage.setScene(currentScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
