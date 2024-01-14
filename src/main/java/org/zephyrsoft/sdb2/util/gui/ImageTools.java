package org.zephyrsoft.sdb2.util.gui;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageTools {
    public static Image rotate(Image image, int degreesToRotateRight) {
        BufferedImage bufferedImage = toBufferedImage(image);
        double sin = Math.abs(Math.sin(Math.toRadians(degreesToRotateRight)));
        double cos = Math.abs(Math.cos(Math.toRadians(degreesToRotateRight)));
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);
        BufferedImage result = new BufferedImage(newWidth, newHeight, Transparency.TRANSLUCENT);
        Graphics2D g = result.createGraphics();
        g.translate((newWidth - width) / 2, (newHeight - height) / 2);
        g.rotate(Math.toRadians(degreesToRotateRight), (double) width / 2, (double) height / 2);
        g.drawRenderedImage(bufferedImage, null);
        g.dispose();
        return result;
    }

    public static Image scale(Image image, double factor) {
        return image.getScaledInstance((int) (image.getWidth(null) * factor), (int) (image.getHeight(null) * factor), Image.SCALE_FAST);
    }

    private static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage bufferedImage) {
            return bufferedImage;
        } else {
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return bufferedImage;
        }
    }
}
