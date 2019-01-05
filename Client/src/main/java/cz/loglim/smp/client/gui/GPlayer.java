package cz.loglim.smp.client.gui;

import cz.loglim.smp.client.utils.ColorUtil;
import cz.loglim.smp.dto.logic.Player;
import cz.loglim.smp.client.interfaces.IDrawable;
import cz.loglim.smp.dto.utils.Vector2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class GPlayer implements IDrawable {

    // Constants
    private static final int BODY_WIDTH = 8;
    private static final int OVAL_PADDING = 2;
    private static final int NARROW_COUNT = 2;
    private static final double TONGUE_LENGTH = 0.7;

    // Private
    private Color ColorBody;
    private Color ColorEven;
    private Color ColorOdd;
    private Color ColorHead;
    private Player player;

    GPlayer(Player player, Color baseColor, Color toneColor) {
        this.player = player;
        ColorEven = baseColor;
        ColorOdd = ColorUtil.toneDown(baseColor, 0.3);
        ColorBody = ColorUtil.blendColor(baseColor, toneColor);
        ColorHead = Color.BLACK;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (player.isDisconnected()) return;

        List<Vector2> points = player.getTrail().getPoints();
        Vector2 headPos = points.get(points.size() - 1);
        int partIndex = 0;
        for (int i = 1; i < points.size(); i++) {
            if (partIndex <= NARROW_COUNT) {
                drawPointsConnector(gc, points.get(i - 1), points.get(i), BODY_WIDTH - (NARROW_COUNT - partIndex) * 2);
                partIndex++;
            } else {
                drawPointsConnector(gc, points.get(i - 1), points.get(i), BODY_WIDTH);
            }
        }

        boolean odd = false;
        partIndex = 0;
        for (int i = 1; i < points.size(); i++) {
            Vector2 p = points.get(i);
            gc.setFill(odd ? ColorOdd : ColorEven);
            odd = !odd;
            if (partIndex < NARROW_COUNT) {
                drawPoint(gc, p, OVAL_PADDING + (NARROW_COUNT - partIndex));
                partIndex++;
            } else {
                drawPoint(gc, p, OVAL_PADDING);
            }
        }

        if (player.isRespawning()) {
            // Draw head
            gc.setFill(ColorHead);
            drawPoint(gc, headPos, 1);
        } else {
            // Draw tongue
            Vector2 nextPosition = headPos.add(player.getCurrentDirection().toVector());
            if (player.isFoodAhead(2)) {
                drawTongue(gc, headPos, nextPosition);
            }

            // Draw head
            gc.setFill(ColorHead);
            drawPoint(gc, headPos, OVAL_PADDING);

            // Draw eyes
            if (player.isObstacleAhead(1)) {
                drawEyes(gc, headPos, nextPosition, 4, true);
            } else if (player.isObstacleAhead(2)) {
                drawEyes(gc, headPos, nextPosition, 5, false);
            } else {
                drawEyes(gc, headPos, nextPosition, 4, false);
            }

            //gc.drawImage(new Image("Image/head.png"), headPos.getX(), headPos.getY(), GGrid.CELL_SIZE, GGrid.CELL_SIZE);
            /*double angle = 0;
            switch (d) {
                case right:
                    angle = 0;
                    break;
                case up:
                    angle = 270;
                    break;
                case left:
                    angle = 180;
                    break;
                case down:
                    angle = 90;
                    break;
            }
            FxUtil.drawRotatedImage(gc, new Image("Image/head.png"), angle, headPos.getX() * GGrid.CELL_SIZE, headPos.getY() * GGrid.CELL_SIZE, GGrid.CELL_SIZE, GGrid.CELL_SIZE);*/
        }
    }

    private void drawPoint(GraphicsContext gc, Vector2 vector2, int padding) {
        int size = GGrid.CELL_SIZE;
        gc.fillOval(vector2.getX() * size + padding, vector2.getY() * size + padding, size - 2 * padding,
                size - 2 * padding);
    }

    private void drawEyes(GraphicsContext gc, Vector2 from, Vector2 to, double size, boolean closed) {
        double x = (0.5 + from.getX()) * GGrid.CELL_SIZE;
        double y = (0.5 + from.getY()) * GGrid.CELL_SIZE;
        double offset = 5;

        // Draw eyes' white
        if (closed) {
            gc.setStroke(Color.WHITESMOKE);
            gc.setLineWidth(1);
            if (from.getX() == to.getX()) {
                gc.strokeOval(x + offset - size, y - size / 2, size, size);
                gc.strokeOval(x - offset, y - size / 2, size, size);
            } else {
                gc.strokeOval(x - size / 2, y + offset - size, size, size);
                gc.strokeOval(x - size / 2, y - offset, size, size);
            }
        } else {
            gc.setFill(Color.WHITESMOKE);
            if (from.getX() == to.getX()) {
                gc.fillOval(x + offset - size, y - size / 2, size, size);
                gc.fillOval(x - offset, y - size / 2, size, size);
            } else {
                gc.fillOval(x - size / 2, y + offset - size, size, size);
                gc.fillOval(x - size / 2, y - offset, size, size);
            }
        }

        if (!closed) {
            // Draw eyes' iris
            gc.setFill(Color.BLACK);
            offset = 4;
            size /= 2;
            if (from.getX() == to.getX()) {
                gc.fillOval(x + offset - size, y - size / 2, size, size);
                gc.fillOval(x - offset, y - size / 2, size, size);
            } else {
                gc.fillOval(x - size / 2, y + offset - size, size, size);
                gc.fillOval(x - size / 2, y - offset, size, size);
            }
        }
    }

    private void drawTongue(GraphicsContext gc, Vector2 from, Vector2 to) {
        double x = (0.5 + from.getX()) * GGrid.CELL_SIZE;
        double y = (0.5 + from.getY()) * GGrid.CELL_SIZE;
        int sizeX = to.getX() * GGrid.CELL_SIZE - from.getX() * GGrid.CELL_SIZE;
        int sizeY = to.getY() * GGrid.CELL_SIZE - from.getY() * GGrid.CELL_SIZE;

        gc.setLineWidth(3);
        gc.strokeLine(x, y, x + sizeX * TONGUE_LENGTH, y + sizeY * TONGUE_LENGTH);
    }

    private void drawPointsConnector(GraphicsContext gc, Vector2 p1, Vector2 p2, int width) {
        // Prevent drawing line across whole room when player moves through wall
        if (Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY()) > 2) return;

        gc.setStroke(ColorBody);
        gc.setLineWidth(width);
        int size = GGrid.CELL_SIZE;
        gc.strokeLine((p1.getX() + 0.5) * size, (p1.getY() + 0.5) * size, (p2.getX() + 0.5) * size,
                (p2.getY() + 0.5) * size);
    }
}
