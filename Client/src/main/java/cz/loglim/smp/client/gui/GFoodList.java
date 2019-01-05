package cz.loglim.smp.client.gui;

import cz.loglim.smp.client.interfaces.IDrawable;
import cz.loglim.smp.dto.logic.Food;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class GFoodList implements IDrawable {

    // Constants
    private static final int FOOD_PADDING = 3;

    // Private
    private List<Food> foodList;

    GFoodList(List<Food> foodList) {
        this.foodList = foodList;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.YELLOWGREEN);
        for (Food food : foodList) {
            gc.fillOval(food.getPosition().getX() * GGrid.CELL_SIZE + FOOD_PADDING,
                    food.getPosition().getY() * GGrid.CELL_SIZE + FOOD_PADDING,
                    GGrid.CELL_SIZE - 2 * FOOD_PADDING,
                    GGrid.CELL_SIZE - 2 * FOOD_PADDING);
        }
    }
}
