package cz.loglim.smp.client.utils;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;

public class FxUtil {

    private static final Color FPS_BG_COLOR = Color.color(0, 0, 0, 0.2);
    private static final Color FPS_FG_COLOR = Color.color(1, 1, 1, 0.4);

    public static void drawFps(GraphicsContext gc, int x, int y, int fpsValue) {
        Paint paintBackup = gc.getFill();
        gc.setFill(FPS_BG_COLOR);
        gc.fillRect(x, y, 64, 24);
        gc.setFill(FPS_FG_COLOR);
        int xOffset = 8;
        int yOffset = 16;
        gc.setFont(new Font(12));
        gc.fillText("FPS = " + fpsValue, x + xOffset, y + yOffset);
        gc.setFill(paintBackup);
    }

    /**
     * Sets the transform for the GraphicsContext to rotate around a pivot point.
     *
     * @param gc    the graphics context the transform to applied to.
     * @param angle the angle of rotation.
     * @param px    the x pivot co-ordinate for the rotation (in canvas co-ordinates).
     * @param py    the y pivot co-ordinate for the rotation (in canvas co-ordinates).
     */
    private static void rotate(GraphicsContext gc, double angle, double px, double py) {
        Rotate r = new Rotate(angle, px, py);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
    }

    /**
     * Draws an image on a graphics context.
     * <p>
     * The image is drawn at (tlpx, tlpy) rotated by angle pivoted around the point:
     * (tlpx + image.getWidth() / 2, tlpy + image.getHeight() / 2)
     *
     * @param gc    the graphics context the image is to be drawn on.
     * @param angle the angle of rotation.
     * @param x     the top left x co-ordinate where the image will be plotted (in canvas co-ordinates).
     * @param y     the top left y co-ordinate where the image will be plotted (in canvas co-ordinates).
     */
    public static void drawRotatedImage(GraphicsContext gc, Image image, double angle, double x, double y, double width, double height) {
        gc.save(); // saves the current state on stack, including the current transform
        rotate(gc, angle, x + width / 2, y + height / 2); // + image.getWidth() / 2, y + image.getHeight() / 2);
        gc.drawImage(image, x, y, width, height);
        gc.restore(); // back to original state (before rotation)
    }
}
