package cz.loglim.smp.client.net;

import cz.loglim.smp.dto.logic.Direction;
import cz.loglim.smp.dto.logic.GameData;
import cz.loglim.smp.dto.logic.Player;
import cz.loglim.smp.dto.Protocol;
import cz.loglim.smp.dto.utils.Serialization;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static cz.loglim.smp.dto.Protocol.*;

public class ServerConnection implements Runnable {

    // Constants
    private static final Logger log = LoggerFactory.getLogger(ServerConnection.class);

    // Public
    public static IntegerProperty playersInQueue;
    public static BooleanProperty gameStarted;

    // Private
    private static Thread thread;
    private static Socket socket;
    private static Protocol protocol;
    private static BufferedReader input;
    private static PrintWriter output;
    private static ServerConnection instance;
    private static String playerNickname;
    private static GameData gameData;
    private static boolean hasReceivedData;
    private static boolean isConnected;
    private static int resultPosition;
    private static int playerLimit;
    private static Direction targetDirection = Direction.DEFAULT;
    private String roomName;

    public static void setup() {
        if (instance == null) {
            instance = new ServerConnection();
        }
    }

    private ServerConnection() {
        isConnected = false;
        try {
            InetAddress address = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
            socket = new Socket(address, 2018);

            protocol = new Protocol(Protocol.Phase.identification);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            isConnected = true;
        } catch (IOException e) {
            if (e.getMessage().equals("Connection refused: connect")) {
                System.out.println("> Cannot connect to server!");
                log.error("Cannot connect to server!");
            } else {
                e.printStackTrace();
                log.error(e.toString());
            }
        }

        playersInQueue = new SimpleIntegerProperty();
        gameStarted = new SimpleBooleanProperty();

        System.out.println("> Running protocol thread now...");
        log.info("Running protocol thread now");
        thread = new Thread(this);
        thread.start();
    }

    public static void disconnect() {
        if (instance != null) {
            instance.stop();
        }
    }

    private void stop() {
        System.out.println("> Server connection thread stopping...");
        log.info("Server connection thread stopping");
        isConnected = false;

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (protocol == null) {
            protocol = new Protocol(Phase.closed);
        } else {
            protocol.setPhase(Phase.closed);
        }

        instance = null;
    }

    @Override
    public void run() {
        loop:
        while (!thread.isInterrupted()) {
            if (protocol != null) {
                // Protocol
                switch (protocol.getPhase()) {
                    case identification: {
                        identifySelf();
                        protocol.setPhase(Phase.queue);
                        break;
                    }
                    case queue: {
                        waitInQueue();
                        break;
                    }
                    case gameStart: {
                        break;
                    }
                    case gameInProgress: {
                        if (!isConnected) {
                            gameData.setState(GameData.State.disconnected);
                            System.out.println("> Disconnected from server!");
                            log.info("Disconnected from server!");
                            return;
                        }
                        sendCurrentDirection(targetDirection);
                        receiveNextStepGameData();
                        break;
                    }
                    case results: {
                        break loop;
                    }
                    case closed: {
                        thread.interrupt();
                        break loop;
                    }
                }
            } else {
                //thread.interrupt();
                System.out.println("> Protocol is NOT SET [ERR]");
                log.error("Protocol is NOT SET");
                return;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }

        // Dispose old instance reference
        instance = null;
        System.out.println("> Server connection thread finished [OK]");
        log.info("Server connection thread finished");
    }

    public static void receiveInitialData() {
        try {
            gameData = GameData.deserialize(input);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //Randomize initial direction
        setTargetDirection(Direction.getRandom());

        protocol.setPhase(Phase.gameInProgress);
        System.out.println("> Initial data received [OK]");
        log.info("Initial data received");
    }

    private void receiveNextStepGameData() {
        // Get food list
        System.out.println("> Receiving next step...");
        log.info("Receiving next step");
        String[] receivedData = new String[gameData.getPlayerLimit() + 1];
        for (int i = 0; i < receivedData.length; i++) {
            receivedData[i] = get();
            if (receivedData[i] == null) {
                // Connection to server (must have been) lost
                thread.interrupt();
                isConnected = false;
                if (gameData != null) {
                    gameData.setState(GameData.State.disconnected);
                }
                protocol.setPhase(Phase.results);
                playersInQueue.setValue(-1);
                System.out.println("> Connection to server lost!");
                log.warn("Connection to server lost!");

                return;
            }

            if (i == 0) {
                // Check game state change
                String state = receivedData[0];

                if (state.startsWith(TAG_STATE_CHANGE)) {
                    state = state.substring(TAG_STATE_CHANGE.length());

                    if (state.startsWith(TAG_GAME_OVER)) {
                        resultPosition = Integer.parseInt(state.substring(TAG_GAME_OVER.length()));
                        gameData.setState(GameData.State.gameOver);
                        protocol.setPhase(Phase.results);
                        hasReceivedData = true;
                        return;
                    }
                }

                gameData.deserializeFood(state);
            }
        }

        // Receive all players data
        for (int i = 0; i < gameData.getPlayerLimit(); i++) {
            String data = receivedData[i + 1];
            System.out.println(String.format("> Received player data: [%s]", data));
            log.info(String.format("> Received player data: [%s]", data));
            Player playerData = Player.deserialize(data, gameData.getGrid());
            if (playerData == null) return;

            gameData.updatePlayer(i, playerData);
        }

        hasReceivedData = true;
    }

    private static void sendCurrentDirection(Direction direction) {
        post(Serialization.serializeDirection(direction));
    }

    private void identifySelf() {
        System.out.println("> Identifying self...");
        log.info("Identifying self");

        String request = get();
        if (request != null && request.equals(TAG_IDENTIFICATION_REQUEST)) {
            post(playerNickname);
            System.out.println(get());
            roomName = get();
            playerLimit = Integer.parseInt(Objects.requireNonNull(get()));
            System.out.println("> Identification request [OK]");
            log.info("Identification request");
            return;
        }
        System.out.println("> Identification request [ERR]");
        log.error("Identification request");
    }

    private void waitInQueue() {
        String message = get();

        if (message == null) {
            System.out.println("> Waiting in queue...");
            log.info("Waiting in queue");
            return;
        }

        if (message.startsWith(TAG_QUEUE_SIZE)) {
            int size = Integer.parseInt(message.substring(TAG_QUEUE_SIZE.length()));
            playersInQueue.setValue(size);
        } else if (message.startsWith(TAG_QUEUE_COMPLETE)) {
            gameStarted.setValue(true);
            protocol.setPhase(Phase.gameStart);
        }

        System.out.println("> Queue >> " + message);
        log.info("Queue >> " + message);
    }

    public static void setTargetDirection(Direction direction) {
        targetDirection = direction;
    }

    public static void setPlayerNickname(String playerNickname) {
        ServerConnection.playerNickname = playerNickname;
    }

    private static void post(String message) {
        output.println(message);
    }

    public static GameData getGameData() {
        return gameData;
    }

    private static String get() {
        try {
            return input.readLine();
        } catch (IOException e) {
            checkDisconnectException(e);
        }
        return null;
    }

    private static void checkDisconnectException(Exception e) {
        if (e.getClass() == SocketException.class) {
            thread.interrupt();
            isConnected = false;
            if (gameData != null) {
                gameData.setState(GameData.State.disconnected);
            }
            protocol.setPhase(Phase.results);
            playersInQueue.setValue(-1);
            System.out.println("> Connection to server lost!");
            log.info("Connection to server lost!");
        } else {
            e.printStackTrace();
            log.error("Disconnected {}", e.toString());
        }
    }

    public static String getPlayerNickname() {
        return playerNickname;
    }

    public static boolean hasReceivedData() {
        if (hasReceivedData) {
            hasReceivedData = false;
            return true;
        }
        return false;
    }

    public static int getResultPosition() {
        return resultPosition;
    }

    public static int getPlayerLimit() {
        return playerLimit;
    }

    public String getRoomName() {
        return roomName;
    }

    public static boolean isConnected() {
        return isConnected;
    }

}
