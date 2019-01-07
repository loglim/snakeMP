package cz.loglim.smp.dto.logic;

import cz.loglim.smp.dto.utils.Vector2;
import org.slf4j.Logger;

import static cz.loglim.smp.dto.Protocol.PLAYER_SAFE_DISTANCE;
import static cz.loglim.smp.dto.Protocol.TAG_PLAYER_DATA;

public class Player {

    // Private
    private Logger log;
    private int id;
    private int respawnMark;
    private boolean disconnected;
    private String name;
    private Trail trail;
    private Direction requestedDirection;
    private Direction currentDirection;
    GameData gameData;

    public Player(int id) {
        this.id = id;
        disconnected = false;
    }

    public Player(String name, int x, int y, GameData gameData, int id) {
        this(id);
        this.name = name;
        this.gameData = gameData;
        trail = new Trail(gameData.getInitialSize(), gameData.getGrid());
        trail.addPoint(new Vector2(x, y));
        requestedDirection = currentDirection = Direction.right;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public void updateDirection() {
        if (disconnected) return;

        if (requestedDirection != currentDirection) {
            currentDirection = requestedDirection;
        }
    }

    public void update() {
        if (disconnected) return;

        if (respawnMark > 0) {
            respawnMark--;
            if(respawnMark == 0) {
                Grid grid = gameData.getGrid();
                if (trail != null) {
                    trail.clear(grid);
                }
                trail = new Trail(gameData.getInitialSize(), grid);
                Vector2 pos = grid.getRandomPositionUnobstructed(PLAYER_SAFE_DISTANCE);
                setPosition(pos);
            }
            else {
                if(log != null) {
                    log.info("Respawning %s in %d...%n", name, respawnMark);
                }
                setPosition(getPosition());
            }
        } else {
            moveBy(currentDirection.toVector());
        }
    }

    public void turn(Direction direction) {
        // Get difference between current and new direction
        Vector2 difference = direction.toVector().add(currentDirection.toVector());

        // Disallow 180° (inverse) turns
        if (difference.getX() == 0 || difference.getY() == 0) return;

        // Prepare to change direction
        requestedDirection = direction;
    }

    private void moveBy(Vector2 vector) {
        Vector2 position = getPosition().getClone();
        position.shiftBy(vector);
        position = gameData.getGrid().overflowPosition(position);
        trail.addPoint(position);

        if (log != null) {
            log.info(String.format("[%s] Position = %s", name, position));
        }
    }

    private void moveTo(Vector2 newPosition) {
        if (newPosition.equals(getPosition())) return;

        trail.addPoint(newPosition.getClone());
        if(log != null) {
            log.info(String.format("[%s] Position = %s", name, newPosition));
        }
    }

    public String serialize() {
        return String.format("%s%d#%s#%s#%d#%d#%s", TAG_PLAYER_DATA, id, name, disconnected, currentDirection.getValue(),
                respawnMark, trail.serialize());
    }

    public static Player deserialize(String input, Grid grid) {
        if (input == null || input.length() <= TAG_PLAYER_DATA.length())
            return null; // Input too short or invalid input data format

        String[] parts = input.substring(TAG_PLAYER_DATA.length()).split("#");

        Player pd = new Player(Integer.parseInt(parts[0]));
        pd.name = parts[1];
        pd.setDisconnected(Boolean.parseBoolean(parts[2]));
        pd.currentDirection = Direction.values()[Integer.parseInt(parts[3])];
        pd.respawnMark = Integer.parseInt(parts[4]);

        if (parts.length > 5) {
            pd.trail = Trail.deserialize(parts[5], grid);
        }

        return pd;
    }

    public int getId() {
        return id;
    }

    private int getLength() {
        return trail == null ? 0 : trail.getLength();
    }

    void update(Player playerData) {
        moveTo(playerData.getPosition());
        trail = playerData.getTrail();
        disconnected = playerData.disconnected;
        respawnMark = playerData.respawnMark;
        currentDirection = playerData.getCurrentDirection();
    }

    Vector2 nextField(int multiplier) {
        Vector2 position = getPosition();
        for (int i = 0; i < multiplier; i++) {
            position = position.add(currentDirection.toVector());
        }
        return position;
    }

    public boolean isFoodAhead(int detectionDistance) {
        return gameData.isFoodAhead(this, detectionDistance);
    }

    public boolean isObstacleAhead(int detectionDistance) {
        return gameData.isObstacleAhead(this, detectionDistance);
    }

    public Trail getTrail() {
        return trail;
    }

    public Vector2 getPosition() {
        return trail.getLast();
    }

    public String getName() {
        return name;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    private void setPosition(Vector2 newPosition) {
        trail.addPoint(newPosition);
    }

    public void increaseLength() {
        trail.increaseLength();
    }

    public void respawn() {
        if(respawnMark == 0) {
            respawnMark = getLength();
        }
    }

    public boolean isRespawning() {
        return respawnMark > 0;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public int getScore() {
        return respawnMark == 0 ? getLength() : 0;
    }

}
