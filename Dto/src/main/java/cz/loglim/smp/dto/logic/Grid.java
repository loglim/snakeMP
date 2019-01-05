package cz.loglim.smp.dto.logic;

import cz.loglim.smp.dto.utils.Vector2;

public class Grid {

    // Constants
    private static final int REPETITION_FAILSAFE_COUNT = 50000;

    // Private
    private GridField[][] data;
    private final int w;
    private final int h;

    Grid(int gridW, int gridH) {
        w = gridW;
        h = gridH;
        data = new GridField[w][h];

        // Fill whole grid with empty grid fields
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                data[x][y] = new GridField(x, y);
            }
        }
    }

    public GridField getField(Vector2 position) {
        if (isOutOfBounds(position)) return GridField.OUT_OF_BOUNDS;

        return data[position.getX()][position.getY()];
    }

    public GridField getField(int x, int y) {
        if (isOutOfBounds(x, y)) return GridField.OUT_OF_BOUNDS;

        return data[x][y];
    }

    public void setField(Vector2 position, GridField field) {
        if (isOutOfBounds(position)) return;

        data[position.getX()][position.getY()] = field;
    }

    void setField(int x, int y) {
        if (isOutOfBounds(x, y)) return;

        data[x][y] = GridField.EMPTY;
    }

    public Vector2 overflowPosition(Vector2 position) {
        int x = position.getX();
        int y = position.getY();

        if (x >= w) {
            x = 0;
        }
        if (y >= h) {
            y = 0;
        }
        if (x < 0) {
            x = w - 1;
        }
        if (y < 0) {
            y = h - 1;
        }

        return new Vector2(x, y);
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || y < 0 || x >= w || y >= h;
    }

    private boolean isOutOfBounds(Vector2 position) {
        return isOutOfBounds(position.getX(), position.getY());
    }

    private boolean isUnobstructed(int x, int y, int detectionDistance) {
        for (int i = x - detectionDistance; i < x + detectionDistance + 1; i++) {
            for (int j = y - detectionDistance; j < y + detectionDistance + 1; j++) {
                GridField field = getField(i, j);
                if (!field.compare(GridField.Type.empty)) {
                    return false; // Field is obstructed
                }
            }
        }
        return true; // Field is not obstructed
    }

    private Vector2 getRandomPosition() {
        return new Vector2(Shared.getRandom().nextInt(w), Shared.getRandom().nextInt(h));
    }

    public Vector2 getRandomPositionUnobstructed(int detectionDistance) {
        int tries = 0;
        Vector2 pos;
        do {
            pos = getRandomPosition();

            // Live-lock fail-safe
            if (tries++ > REPETITION_FAILSAFE_COUNT && detectionDistance > 0) {
                tries = 0;
                detectionDistance--;
            }
        }
        while (!isUnobstructed(pos.getX(), pos.getY(), detectionDistance));

        return pos;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }
}
