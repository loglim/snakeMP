package cz.loglim.smp.client.gui;

import cz.loglim.smp.client.interfaces.IDrawable;
import cz.loglim.smp.dto.logic.Grid;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GGrid implements IDrawable {

    // Constants
    public static final int CELL_SIZE = 16;
    private static final Color GRID_COLOR = Color.color(1, 1, 1, 0.15);

    // Private
    private Grid grid;

    GGrid(Grid grid) {
        this.grid = grid;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(1);
        for (int i = 0; i < grid.getW(); i++) {
            // Vertical lines
            int x = i * GGrid.CELL_SIZE;
            gc.strokeLine(x, 0, x, grid.getH() * CELL_SIZE);
        }
        for (int i = 0; i < grid.getH(); i++) {
            // Horizontal lines
            int y = i * GGrid.CELL_SIZE;
            gc.strokeLine(0, y, grid.getW() * CELL_SIZE, y);
        }
    }
}
