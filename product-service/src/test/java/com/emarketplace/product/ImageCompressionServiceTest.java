package com.emarketplace.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageCompressionServiceTest {
    @Test
    void compressesLargeImagesToTheDatabaseLimit() throws Exception {
        BufferedImage source = new BufferedImage(2400, 1600, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                source.setRGB(x, y, new Color((x * 31 + y) & 255, (x + y * 17) & 255, (x * 7 + y * 13) & 255).getRGB());
            }
        }
        ByteArrayOutputStream input = new ByteArrayOutputStream();
        ImageIO.write(source, "png", input);

        byte[] compressed = ImageCompressionService.compress(input.toByteArray());

        assertTrue(compressed.length <= 50 * 1024);
        assertEquals(0xff, compressed[0] & 0xff);
        assertEquals(0xd8, compressed[1] & 0xff);
    }
}
