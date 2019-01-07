package cz.loglim.smp.server;

import cz.loglim.smp.dto.logic.Direction;
import cz.loglim.smp.dto.logic.Grid;
import cz.loglim.smp.dto.logic.GridField;
import cz.loglim.smp.dto.logic.Player;
import cz.loglim.smp.dto.Protocol;
import cz.loglim.smp.dto.utils.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cz.loglim.smp.dto.Protocol.*;

public class GameSession implements Runnable {

    // Constants
    private static final int DEFAULT_ROOM_WIDTH = 32;
    private static final int DEFAULT_ROOM_HEIGHT = 26;
    private static final Logger log = LoggerFactory.getLogger(GameSession.class);

    // Private
    // - Game info
    private Thread thread;
    private Game game;
    private List<PlayerConnection> playerConnections;
    private Protocol protocol;
    private String roomName;
    private String winnerName;
    private static boolean showGridInConsole;
    private static int playerLimit;
    private static int targetScore;
    private static int initialSize;
    private int playerCount;
    private int lastPlayerCount;

    GameSession(int roomId) {
        // Setup protocol and lists
        protocol = new Protocol(Protocol.Phase.queue);
        playerConnections = new ArrayList<>();
        roomName = String.format("Room %d", roomId);

        // Create new gameData instance
        game = new Game();
        game.setGridSize(DEFAULT_ROOM_WIDTH, DEFAULT_ROOM_HEIGHT);
        game.setPlayerLimit(playerLimit);
        game.setInitialSize(initialSize);
        game.setTargetScore(targetScore);

        // Create and start a new thread for this session
        thread = new Thread(this);
        thread.start();
        log("> [OK] A new session has started!");
        log.info("> [OK] A new session has started!");
    }

    synchronized void addPlayer(Socket socket) {
        // Remove include broken connections from list
        for (int i = 0; i < playerConnections.size(); i++) {
            if (!playerConnections.get(i).checkConnection()) {
                playerConnections.remove(i);
                i--;
            }
        }

        PlayerConnection connection = new PlayerConnection(socket);
        connection.setId(playerConnections.size());
        playerConnections.add(connection);
        protocol.setPhase(Protocol.Phase.identification);
        playerCount++;
        log(String.format("> [OK] PlayerInfo %d connected", connection.getId()));
    }

    boolean isFull() {
        return playerConnections.size() == game.getPlayerLimit();
    }

    private synchronized void identifyPlayers() {
        for (PlayerConnection connection : playerConnections) {
            if (!connection.isIdentified()) {
                identifyUser(connection);
            }
        }
    }

    @Override
    public void run() {
        // Protocol
        loop:
        while (true) {
            switch (protocol.getPhase()) {
                case identification: {
                    identifyPlayers();
                    break;
                }
                case queue: {
                    awaitPlayerConnection();
                    break;
                }
                case gameStart: {
                    sendInitialData();
                    game.setState(Game.State.playing);
                    protocol.setPhase(Phase.gameInProgress);
                    break;
                }
                case gameInProgress: {
                    nextGameStep();
                    break;
                }
                case results: {
                    thread.interrupt();
                    break loop;
                }
            }

            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("Thread error: {}", e.toString());
            }
        }

        System.out.println(String.format("> [OK] [%s] Finished", thread.getName()));
        log.info(String.format("> [%s] Finished", thread.getName()));
    }

    // Phase identification
    private void identifyUser(PlayerConnection connection) {
        // Request connection´s identification
        log(String.format("> Identifying connection %d...", connection.getId()));
        log.info(String.format("> Identifying connection %d...", connection.getId()));
        connection.post(TAG_IDENTIFICATION_REQUEST, true);
        String name = connection.get();

        // Make sure provided name is unique for this session
        String pattern = "";
        for (PlayerConnection p : playerConnections) {
            if (p.getName() != null && p.getName().equals(name)) {
                pattern += "I";
                name += String.format(" [%s]", pattern);
            }
        }

        // Update connection
        connection.setName(name);
        log(String.format("> PlayerInfo identified as \"%s\"", connection.getName()));
        log.info(String.format("> PlayerInfo identified as \"%s\"", connection.getName()));
        connection.post(">> Welcome " + connection.getName(), true);

        // Send game room info
        connection.post(roomName, false);
        connection.post("" + playerLimit, false);

        connection.setIdentified();
        protocol.setPhase(Protocol.Phase.queue);
    }

    // Phase queue
    private void awaitPlayerConnection() {
        if (lastPlayerCount != playerConnections.size()) {
            lastPlayerCount = playerConnections.size();
            notifyAllClients(String.format("%s%d", TAG_QUEUE_SIZE, lastPlayerCount));
            System.out.println("Queue size is " + lastPlayerCount);
        }

        if (lastPlayerCount == game.getPlayerLimit()) {
            notifyAllClients(TAG_QUEUE_COMPLETE);
            protocol.setPhase(Protocol.Phase.gameStart);
        }
    }

    // Phase gameData start
    private void sendInitialData() {
        // Setup room
        game.spawnObstacles();
        for (PlayerConnection connection : playerConnections) {
            game.createPlayer(connection.getName());
        }

        // DEBUG ONLY - show gameData room info
        for (Player player : game.getPlayers()) {
            System.out.println(String.format("> PlayerInfo [%d] %s; x = %d, y = %d", player.getId(), player.getName(),
                    player.getPosition().getX(), player.getPosition().getY()));
            log.debug(String.format("> PlayerInfo [%d] %s; x = %d, y = %d", player.getId(), player.getName(),
                    player.getPosition().getX(), player.getPosition().getY()));
        }

        // Send current player limit
        notifyAllClients("" + game.getPlayerLimit());

        // Send appropriate ids to all
        for (PlayerConnection connection : playerConnections) {
            connection.post(TAG_PLAYER_ID + connection.getId(), true);
        }

        // Send initial game data to all
        notifyAllClients(game.serializeInitialData());
        System.out.println("> [OK] Initial data sent");
        log.info("> [OK] Initial data sent");
    }

    // Phase gameData in progress
    private void nextGameStep() {
        System.out.println("> Next step...");
        log.info("> Next step...");

        // Check the number of actively connected players
        playerCount = playerConnections.size();
        for (PlayerConnection playerConnection : playerConnections) {
            if (playerConnection.isDisconnected()) {
                playerCount--;
            }
        }

        // Make sole player the winner (others are disconnected - not a SP game)
        if (playerCount == 1 && playerLimit > 1) {
            finishGame();
        }
        // When no players are actively connected, finish the session
        else if (playerCount == 0) {
            protocol.setPhase(Phase.results);
        }

        // Receive player directions
        receivePlayerDirections();

        // Update gameData
        game.update();

        // Print grid to console
        if (showGridInConsole) {
            Grid g = game.getGrid();
            for (int y = 0; y < g.getH(); y++) {
                for (int x = 0; x < g.getW(); x++) {
                    GridField f = g.getField(x, y);
                    if (f.compare(GridField.Type.player)) {
                        System.out.print("O");
                    } else if (f.compare(GridField.Type.obstacle)) {
                        System.out.print("X");
                    } else if (f.compare(GridField.Type.food)) {
                        System.out.print("#");
                    } else {
                        System.out.print(" ");
                    }
                }
                System.out.println();
            }
        }

        // Send current gameData data
        sendGameData();

        // Finish gameData session, send results
        if (checkVictoryCondition()) {
            finishGame();
        }
    }

    private void finishGame() {
        // Find score of each player, add it uniquely to score list
        List<Integer> scoreList = new ArrayList<>();
        for (PlayerConnection connection : playerConnections) {
            // Skip disconnected players
            if (connection.isDisconnected()) continue;

            int score = game.getPlayerScore(connection.getId());
            if (!scoreList.contains(score)) {
                scoreList.add(score);
            }
        }

        // Sort score list and reverse is to descending order
        scoreList.sort(Integer::compareTo);
        Collections.reverse(scoreList);

        // Send each player its result position
        for (PlayerConnection connection : playerConnections) {
            connection.sendResults(game, scoreList);
        }
        protocol.setPhase(Phase.results);
        System.out.println(String.format("> Game complete, player [%s] has won!", winnerName));
        log.info(String.format("> Game complete, player [%s] has won!", winnerName));
    }

    private void receivePlayerDirections() {
        for (PlayerConnection playerConnection : playerConnections) {
            if (playerConnection.isDisconnected()) continue;

            String input = playerConnection.get();
            if (input == null) {
                System.out.println(
                        String.format("> [WRN] PlayerInfo [%s] has disconnected!", playerConnection.getName()));
                log.warn(String.format("> PlayerInfo [%s] has disconnected!", playerConnection.getName()));
                playerConnection.markDisconnected();
                game.removePlayer(playerConnection.getId());
                return;
            }

            Direction direction = Serialization.deserializeDirection(input);
            game.updatePlayerDirection(playerConnection.getId(), direction);
        }
    }

    // Phase gameData in progress
    private boolean checkVictoryCondition() {
        for (Player player : game.getPlayers()) {
            if (game.checkTargetScore(player.getScore())) {
                winnerName = player.getName();
                return true;
            }
        }

        return false;
    }

    // Phase gameData in progress
    private void sendGameData() {
        // Serialize data and store it in local variables
        String serializedFood = game.serializeFood();
        String[] serializedPlayers = game.serializePlayers();

        // Send food data
        notifyAllClients(serializedFood);

        // Send all players data
        for (String playerData : serializedPlayers) {
            notifyAllClients(playerData);
        }
    }

    private void notifyAllClients(String message) {
        notifyAllClients(null, message);
    }

    private void notifyAllClients(String[] messages) {
        for (String message : messages) {
            notifyAllClients(null, message);
        }
    }

    /**
     * Notify all clients except for one provided
     *
     * @param playerConnection player connection which should be skipped
     * @param message          message for the clients
     */
    private void notifyAllClients(PlayerConnection playerConnection, String message) {
        for (PlayerConnection connection : playerConnections) {
            if (connection.equals(playerConnection)) continue;

            connection.post(message, false);
        }
    }

    static void setShowGridInConsole() {
        GameSession.showGridInConsole = true;
    }

    private void log(String message) {
        System.out.println(String.format("> [%s] > %s", roomName, message));
    }

    static void setPlayerLimit(int playerLimit) {
        GameSession.playerLimit = playerLimit;
    }

    static void setTargetScore(int targetScore) {
        GameSession.targetScore = targetScore;
    }

    static void setInitialSize(int initialSize) {
        GameSession.initialSize = initialSize;
    }
}
