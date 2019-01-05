package cz.loglim.smp.client.utils;

import javafx.scene.paint.Color;

public class ColorUtil {

    public static Color toneDown(Color c, double percentage) {
        if (c == null) return null;

        return new Color(c.getRed() * percentage, c.getGreen() * percentage, c.getBlue() * percentage, c.getOpacity());
    }

    public static Color blendColor(Color c1, Color c2) {
        if (c1 == null || c2 == null) return null;

        double r = (c1.getRed() + c2.getRed()) / 2;
        double g = (c1.getGreen() + c2.getGreen()) / 2;
        double b = (c1.getBlue() + c2.getBlue()) / 2;
        double a = (c1.getOpacity() + c2.getOpacity()) / 2;

        return new Color(r, g, b, a);
    }

}
