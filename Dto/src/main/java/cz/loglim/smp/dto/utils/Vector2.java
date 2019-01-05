package cz.loglim.smp.dto.utils;

public class Vector2 {

    // Constants
    public static final Vector2 MIN = new Vector2(Integer.MIN_VALUE, Integer.MIN_VALUE);
    public static final Vector2 ZERO = new Vector2(0, 0);
    public static final Vector2 LEFT = new Vector2(-1, 0);
    public static final Vector2 RIGHT = new Vector2(1, 0);
    public static final Vector2 UP = new Vector2(0, -1);
    public static final Vector2 DOWN = new Vector2(0, 1);

    // Private
    private int x;
    private int y;

    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String serialize() {
        return String.format("%dx%d", x, y);
    }

    public static Vector2 deserialize(String input) {
        String[] parts = input.split("x");
        return new Vector2(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void shiftBy(Vector2 vector) {
        this.x += vector.getX();
        this.y += vector.getY();
    }

    public Vector2 add(Vector2 addition) {
        return new Vector2(x + addition.getX(), y + addition.getY());
    }

    public Vector2 getClone() {
        return new Vector2(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof Vector2)) return false;

        return ((Vector2) obj).x == x && ((Vector2) obj).y == y;
    }

    @Override
    public String toString() {
        return String.format("[%d x %d]", x, y);
    }
}
