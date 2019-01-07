package cz.loglim.smp.server;

import cz.loglim.smp.dto.logic.*;
import cz.loglim.smp.dto.utils.Serialization;
import cz.loglim.smp.dto.utils.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

import static cz.loglim.smp.dto.Protocol.*;

public class Game extends GameData {

    private static final Logger log = LoggerFactory.getLogger(Game.class);

    Game() {
        super();
    }

    private void checkPlayerScore(Player player) {
        if (checkTargetScore(player.getScore())) {
            state = State.gameOver;
        }
    }

    void update() {
        System.out.println("> Updating game...");
        log.info("> Updating game...");
        if (state != State.playing) return;

        // Update direction of players
        players.forEach(Player::updateDirection);

        // Check collisions of players
        checkCollisions();

        // Update the players
        players.forEach(Player::update);

        // Make sure there is enough foodList in the room
        spawnFood();
    }

    void createPlayer(String playerName) {
        Vector2 pos = grid.getRandomPositionUnobstructed(PLAYER_SAFE_DISTANCE);
        Player player = new Player(playerName, pos.getX(), pos.getY(), this, players.size());
        players.add(player);
    }

    void updatePlayerDirection(int id, Direction direction) {
        for (Player player : players) {
            if (player.getId() == id) {
                player.turn(direction);
            }
        }
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    void spawnObstacles() {
        for (int i = 0; i < OBSTACLE_COUNT; i++) {
            Vector2 pos = grid.getRandomPositionUnobstructed(1);
            Obstacle obstacle = new Obstacle(pos.getX(), pos.getY());
            grid.setField(pos, obstacle);
            obstacles.add(obstacle);
        }
    }

    private void spawnFood() {
        if (foodList.size() >= FOOD_LIMIT) return;

        Vector2 pos = grid.getRandomPositionUnobstructed(FOOD_SAFE_DISTANCE);
        Food food = new Food(pos.getX(), pos.getY());
        foodList.add(food);
        grid.setField(pos, food);
    }

    private void collectFood(Food food) {
        int pos = -1;
        for (int i = 0; i < foodList.size(); i++) {
            if (foodList.get(i).getPosition().equals(food.getPosition())) {
                pos = i;
                break;
            }
        }
        if (pos == -1) return;

        foodList.remove(pos);
        food.setEmpty();
    }

    private void checkCollisions() {
        int playerCount = players.size();

        Vector2[] nextPositions = new Vector2[playerCount];
        // Mark all players next position
        for (int i = 0; i < playerCount; i++) {
            Vector2 position = players.get(i).getPosition().add(players.get(i).getCurrentDirection().toVector());
            nextPositions[i] = grid.overflowPosition(position);
        }

        for (int i = 0; i < playerCount; i++) {
            if (players.get(i).isDisconnected() || players.get(i).isRespawning()) continue;

            Vector2 playerPosition = nextPositions[i];
            // Check player-player collisions
            for (int j = 0; j < playerCount; j++) {
                // Don't compare to player to itself and dead (disconnected) players
                if (i == j || players.get(j).isDisconnected()) continue;

                if (playerPosition.equals(nextPositions[j]) || players.get(j).getTrail().hasPoint(playerPosition, 1)) {
                    // Collision with other player detected
                    players.get(i).respawn();
                    break;
                }
            }

            for (Obstacle obstacle : obstacles) {
                if (obstacle.getPosition().equals(playerPosition)) {
                    // Collision with obstacle detected
                    players.get(i).respawn();
                    break;
                }
            }

            for (Food food : foodList) {
                if (food.getPosition().equals(playerPosition)) {
                    // Collect food
                    players.get(i).increaseLength();
                    checkPlayerScore(players.get(i));
                    collectFood(food);
                    break;
                }
            }
        }
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

    int getPlayerScore(int playerId) {
        for (Player player : players) {
            if (player.getId() == playerId) {
                return player.getScore();
            }
        }
        return -1;
    }

    void removePlayer(int playerId) {
        for (Player player : players) {
            if (player.getId() == playerId) {
                player.setDisconnected(false);
                return;
            }
        }
    }

    public Grid getGrid() {
        return grid;
    }
}
