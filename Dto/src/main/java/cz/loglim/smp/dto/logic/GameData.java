package cz.loglim.smp.dto.logic;

import cz.loglim.smp.dto.utils.Serialization;
import cz.loglim.smp.dto.utils.Vector2;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cz.loglim.smp.dto.Protocol.TAG_PLAYER_ID;

public class GameData {

    // Public
    public enum State {
        ready,
        playing,
        gameOver,
        disconnected
    }

    // Private
    protected final List<Player> players;
    protected final List<Food> foodList;
    protected final List<Obstacle> obstacles;
    protected State state;
    protected Grid grid;
    private static int localPlayerId;
    private int targetScore;
    private int initialSize;
    private int playerLimit;

    public GameData() {
        state = State.ready;
        players = new ArrayList<>();
        foodList = new ArrayList<>();
        obstacles = new ArrayList<>();
    }

    public void setGridSize(int gridW, int gridH) {
        grid = new Grid(gridW, gridH);
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public String[] serializePlayers() {
        String[] serializedPlayers = new String[players.size()];
        for (int i = 0; i < players.size(); i++) {
            serializedPlayers[i] = players.get(i).serialize();
        }
        return serializedPlayers;
    }

    public String serializeFood() {
        return Serialization.serializeVectorCollection(
                foodList.stream().map(GridField::getPosition).collect(Collectors.toList()));
    }

    public void deserializeFood(String input) {
        foodList.clear();
        Serialization.deserializeVectorCollection(input).stream().map(v -> new Food(v.getX(), v.getY())).forEach(
                foodList::add);
    }

    public String serializeObstacles() {
        return Serialization.serializeVectorCollection(
                obstacles.stream().map(GridField::getPosition).collect(Collectors.toList()));
    }

    public void deserializeObstacles(String input) {
        Serialization.deserializeVectorCollection(input).stream().map(v -> new Obstacle(v.getX(), v.getY())).forEach(
                obstacles::add);
    }

    public static GameData deserialize(BufferedReader in) throws IOException {
        // Get room resolution
        String[] input = new String[]{
                in.readLine(),
                in.readLine(),
                in.readLine(),
                in.readLine()
        };

        // Get current server player limit
        int playerLimit = Integer.parseInt(Objects.requireNonNull(input[0]));

        GameData data = new GameData();

        // Get this player's id
        localPlayerId = Integer.parseInt(Objects.requireNonNull(input[1]).substring(TAG_PLAYER_ID.length()));

        Vector2 resolution = Vector2.deserialize(input[2]);
        data.setGridSize(resolution.getX(), resolution.getY());
        data.setPlayerLimit(playerLimit);

        // Get obstacle positions
        data.deserializeObstacles(input[3]);

        // Get all players data
        for (int i = 0; i < data.playerLimit; i++) {
            Player player = Player.deserialize(Objects.requireNonNull(in.readLine()), data.getGrid());
            player.gameData = data;
            data.players.add(player);
        }

        System.out.println("All server data received ok!");

        return data;
    }

    /**
     * Serializes obstacles and players
     *
     * @return serialized initial data
     */
    public String[] serializeInitialData() {
        String[] output = new String[players.size() + 2];
        output[0] = new Vector2(grid.getW(), grid.getH()).serialize();
        output[1] = serializeObstacles();
        for (int i = 0; i < players.size(); i++) {
            output[i + 2] = players.get(i).serialize();
        }
        return output;
    }

    boolean isFoodAhead(Player player, int detectionDistance) {
        for (int i = 0; i < detectionDistance; i++) {
            Vector2 position = player.nextField(1 + i);
            for (Food food : foodList) {
                if (food.getPosition().equals(position)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isObstacleAhead(Player player, int detectionDistance) {
        for (int i = 0; i < detectionDistance; i++) {
            Vector2 position = player.nextField(1 + i);
            for (Obstacle obstacle : obstacles) {
                if (obstacle.getPosition().equals(position)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void updatePlayer(int id, Player playerData) {
        players.get(id).update(playerData);
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    int getInitialSize() {
        return initialSize;
    }

    public boolean checkTargetScore(int score) {
        return score >= targetScore;
    }

    public void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public void setTargetScore(int targetScore) {
        this.targetScore = targetScore;
    }

    public final List<Obstacle> getObstacles() {
        return obstacles;
    }

    public final List<Food> getFoodList() {
        return foodList;
    }

    public final List<Player> getPlayers() {
        return players;
    }

    public int playerCount() {
        return players.size();
    }

    public static int getLocalPlayerId() {
        return localPlayerId;
    }

    public Grid getGrid() {
        return grid;
    }
}
