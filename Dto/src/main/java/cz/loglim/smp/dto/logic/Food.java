package cz.loglim.smp.dto.logic;

import cz.loglim.smp.dto.utils.Vector2;

public class Food extends GridField {

    Food(Vector2 position) {
        super(position.getX(), position.getY(), Type.food);
    }

    public Food(int x, int y) {
        super(x, y, Type.food);
    }

}
