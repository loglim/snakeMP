package cz.loglim.smp.dto.logic;

import cz.loglim.smp.dto.utils.Vector2;

import java.util.Random;

public enum Direction {

    // Enum values
    none(0),
    right(1),
    up(2),
    left(3),
    down(4);

    // Constants
    public static final Direction DEFAULT = Direction.right;

    // Public
    Direction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Direction parseFromInt(int value) {
        for (Direction direction : values()) {
            if (direction.value == value) {
                return direction;
            }
        }
        return DEFAULT;

        //return values()[value];
    }

    // Private
    private final int value;

    public Vector2 toVector() {
        switch (this) {
            case right: {
                return Vector2.RIGHT;
            }
            case up: {
                return Vector2.UP;
            }
            case left: {
                return Vector2.LEFT;
            }
            case down: {
                return Vector2.DOWN;
            }
        }
        return Vector2.ZERO;
    }

    public static Direction findDirectionByVector(Vector2 vector) {
        if (vector.getX() != 0) {
            return vector.getX() == 1 ? Direction.right : Direction.left;
        } else if (vector.getY() != 0) {
            return vector.getY() == 1 ? Direction.down : Direction.up;
        }
        return Direction.none;
    }

    public static Direction getRandom() {
        return Direction.values()[new Random().nextInt(Direction.values().length - 1) + 1];
    }
}
