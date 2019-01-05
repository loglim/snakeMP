package cz.loglim.smp.client.gui;

import cz.loglim.smp.dto.logic.Obstacle;
import cz.loglim.smp.client.interfaces.IDrawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GObstacle implements IDrawable {

    private Obstacle obstacle;

    GObstacle(Obstacle obstacle) {
        this.obstacle = obstacle;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(obstacle.getPosition().getX() * GGrid.CELL_SIZE + 1,
                obstacle.getPosition().getY() * GGrid.CELL_SIZE + 1,
                GGrid.CELL_SIZE - 2,
                GGrid.CELL_SIZE - 2);
    }
}
