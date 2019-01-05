package cz.loglim.smp.dto.logic;

import cz.loglim.smp.dto.utils.Vector2;

import java.util.ArrayList;
import java.util.List;

public class Trail {

    // Private
    private Grid grid;
    private int maxLength;
    private List<Vector2> path;

    Trail(int maxLength, Grid grid) {
        this.maxLength = maxLength;
        this.grid = grid;
        path = new ArrayList<>();
    }

    private Trail(List<Vector2> path, Grid grid) {
        this.path = path;
        this.grid = grid;
        this.maxLength = path.size();
    }

    void addPoint(Vector2 vector2) {
        if (path.size() == maxLength) {
            // Empty that grid position
            grid.setField(path.get(0), new GridField(path.get(0).getX(), path.get(0).getY()));

            // Remove oldest entry
            grid.setField(path.get(0).getX(), path.get(0).getY());
            path.remove(0);
        }
        path.add(vector2);
        grid.setField(vector2, new GridField(vector2.getX(), vector2.getY(), GridField.Type.player));
    }

    void increaseLength() {
        maxLength += 1;
    }

    public List<Vector2> getPoints() {
        List<Vector2> pts = new ArrayList<>();
        for (Vector2 vector2 : path) {
            pts.add(vector2.getClone());
        }
        return pts;
    }

    Vector2 getLast() {
        if (path.size() == 0) {
            return Vector2.MIN;
        }
        return path.get(path.size() - 1);
    }

    /**
     * Removes all associated trail fields from provided grid
     *
     * @param grid grid from which the trail should be removed
     */
    void clear(Grid grid) {
        for (Vector2 position : path) {
            grid.setField(position.getX(), position.getY());
        }
        path.clear();
    }

    static Trail deserialize(String input, Grid grid) {
        List<Vector2> path = new ArrayList<>();
        for (String part : input.split(";")) {
            path.add(Vector2.deserialize(part));
        }
        return new Trail(path, grid);
    }

    String serialize() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0, pathSize = path.size(); i < pathSize; i++) {
            stringBuilder.append(path.get(i).serialize());
            if (i < pathSize - 1) {
                stringBuilder.append(";");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Determines whether path contains particular point
     * @param point particular point
     * @param trailOffset Skip specified number of trailing points
     * @return returns whether trail contains particular point
     */
    public boolean hasPoint(Vector2 point, int trailOffset) {
        int size = path.size();
        for (int i = trailOffset; i < size; i++) {
            Vector2 v = path.get(i);
            if (v.equals(point)) {
                return true;
            }
        }
        return false;
    }

    int getLength() {
        return path.size();
    }
}
