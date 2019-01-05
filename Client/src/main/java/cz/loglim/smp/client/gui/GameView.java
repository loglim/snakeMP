package cz.loglim.smp.client.gui;

import cz.loglim.smp.client.interfaces.IDrawable;
import cz.loglim.smp.dto.logic.GameData;
import cz.loglim.smp.dto.logic.Obstacle;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class GameView extends GameData {

    // Constants
    private static final Color BORDER_COLOR = Color.DARKGRAY;
    private static final Color[] BASE_COLORS = {Color.BLUE, Color.RED, Color.GREEN, Color.PURPLE, Color.YELLOW, Color.BROWN, Color.STEELBLUE, Color.SPRINGGREEN};
    private static final Color[] TONE_COLORS = {Color.GRAY, Color.ORANGE, Color.YELLOW, Color.GREY, Color.WHITESMOKE, Color.DARKRED, Color.LIGHTBLUE, Color.DARKGREEN};

    // Private
    private List<IDrawable> drawables;
    private double width;
    private double height;

    public GameView() {
        super();
        drawables = new ArrayList<>();
    }

    public void setResolution(double width, double height) {
        this.width = width;
        this.height = height;
        drawables.add(new GGrid(getGrid()));
        getObstacles().forEach(obstacle -> drawables.add(new GObstacle(obstacle)));
    }

    public void draw(GraphicsContext gc) {
        // Draw background
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, width, height);

        // Draw all drawables
        drawables.forEach(a -> a.draw(gc));

        // Draw debug info
        /*int fps = (int) (1 / deltaTime);
        FxUtil.drawFps(gc, 8, 8, fps);*/

        // Draw room border
        gc.setLineWidth(2);
        gc.setFill(Color.TRANSPARENT);
        gc.setStroke(BORDER_COLOR);
        gc.strokeRect(1, 1, width - 2, height - 2);
    }

    public void setGameData(GameData gameData) {
        // Setup graphical instances | players
        for (int i = 0; i < gameData.playerCount(); i++) {
            drawables.add(new GPlayer(gameData.getPlayers().get(i), BASE_COLORS[i], TONE_COLORS[i]));
        }

        // Setup graphical instances | obstacles
        for (Obstacle obstacle : gameData.getObstacles()) {
            drawables.add(new GObstacle(obstacle));
        }

        // Setup graphical instances | food
        drawables.add(new GFoodList(gameData.getFoodList()));
    }

    public static Color getPlayerColor(int id) {
        return BASE_COLORS[id];
    }

}
