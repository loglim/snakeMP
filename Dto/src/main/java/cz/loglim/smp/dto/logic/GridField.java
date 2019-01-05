package cz.loglim.smp.dto.logic;

import cz.loglim.smp.dto.utils.Vector2;

public class GridField {

    // Constants
    static final GridField OUT_OF_BOUNDS = new GridField(-2, -2, Type.outOfBound);
    static final GridField EMPTY = new GridField(-1, -1, Type.empty);

    // Public
    public enum Type {
        outOfBound,
        empty,
        obstacle,
        player,
        food
    }

    // Private
    private Type type;
    private Vector2 position;

    GridField(int x, int y) {
        position = new Vector2(x, y);
        this.type = Type.empty;
    }

    GridField(int x, int y, Type type) {
        position = new Vector2(x, y);
        this.type = type;
    }

    public Vector2 getPosition() {
        return position;
    }

    boolean isEmpty() {
        return type == Type.empty;
    }

    public void setEmpty() {
        type = Type.empty;
    }

    public boolean compare(Type type) {
        return this.type == type;
    }
}
