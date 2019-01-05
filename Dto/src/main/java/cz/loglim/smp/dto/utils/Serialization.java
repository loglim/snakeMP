package cz.loglim.smp.dto.utils;

import cz.loglim.smp.dto.logic.Direction;

import java.util.ArrayList;
import java.util.List;

import static cz.loglim.smp.dto.Protocol.TAG_PLAYER_DIRECTION;

public class Serialization {

    private static final String SEPARATOR_A = "#";

    public static String serializeVectorCollection(List<Vector2> collection) {
        StringBuilder output = new StringBuilder();
        for (Vector2 vector : collection) {
            output.append(String.format("%s%s", vector.serialize(), SEPARATOR_A));
        }
        return output.toString();
    }

    public static List<Vector2> deserializeVectorCollection(String input) {
        List<Vector2> collection = new ArrayList<>();
        if (!input.contains(SEPARATOR_A)) return collection;

        String[] parts = input.split(SEPARATOR_A);
        for (String part : parts) {
            collection.add(Vector2.deserialize(part));
        }

        return collection;
    }

    public static String serializeDirection(Direction direction) {
        return String.format("%s%d", TAG_PLAYER_DIRECTION, direction.getValue());
    }

    public static Direction deserializeDirection(String input) {
        String part = input.substring(TAG_PLAYER_DIRECTION.length());
        int num = Integer.parseInt(part);
        return Direction.parseFromInt(num);
    }

}
