package com.emarketplace.user;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageCompressionServiceTest {
    @Test
    void keepsProfileImageAtOrBelowFiftyKilobytes() throws Exception {
        BufferedImage source = new BufferedImage(1800, 1800, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < source.getHeight(); y++) for (int x = 0; x < source.getWidth(); x++) source.setRGB(x, y, (x * 73856093) ^ (y * 19349663));
        ByteArrayOutputStream input = new ByteArrayOutputStream();
        ImageIO.write(source, "png", input);
        assertTrue(ImageCompressionService.compress(input.toByteArray()).length <= 50 * 1024);
    }
}
