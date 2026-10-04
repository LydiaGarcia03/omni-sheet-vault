package dev.omnisheetvault.api.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class PortraitImageTest {

    @Test
    void aWidePngBecomesACentredSquareOfAtMost512() throws IOException {
        BufferedImage wide = new BufferedImage(1000, 600, BufferedImage.TYPE_INT_RGB);
        paint(wide, 0, 200, Color.RED);
        paint(wide, 200, 800, Color.GREEN);
        paint(wide, 800, 1000, Color.BLUE);

        BufferedImage stored = read(PortraitImage.normalize(encode(wide, "png")));

        assertThat(stored.getWidth()).isEqualTo(512);
        assertThat(stored.getHeight()).isEqualTo(512);
        assertThat(new Color(stored.getRGB(256, 256))).isEqualTo(Color.GREEN);
        assertThat(new Color(stored.getRGB(2, 256)).getGreen()).isGreaterThan(200);
    }

    @Test
    void aSmallJpegKeepsItsSizeAndIsStoredAsPng() throws IOException {
        byte[] stored = PortraitImage.normalize(encode(new BufferedImage(100, 80, BufferedImage.TYPE_INT_RGB), "jpg"));

        BufferedImage image = read(stored);
        assertThat(image.getWidth()).isEqualTo(80);
        assertThat(image.getHeight()).isEqualTo(80);
        assertThat(stored).startsWith((byte) 0x89, (byte) 'P', (byte) 'N', (byte) 'G');
    }

    @Test
    void rejectsOtherImageFormats() throws IOException {
        byte[] gif = encode(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "gif");

        assertThatThrownBy(() -> PortraitImage.normalize(gif))
                .isInstanceOf(InvalidPortraitException.class)
                .hasMessageContaining("PNG or JPEG");
    }

    @Test
    void rejectsAFileThatIsNotAnImage() {
        assertThatThrownBy(() -> PortraitImage.normalize("not an image".getBytes()))
                .isInstanceOf(InvalidPortraitException.class);
    }

    @Test
    void rejectsAnImageLargerThanTheSourceLimit() throws IOException {
        byte[] huge = encode(new BufferedImage(PortraitImage.MAX_SOURCE_SIDE + 1, 4, BufferedImage.TYPE_INT_RGB), "png");

        assertThatThrownBy(() -> PortraitImage.normalize(huge))
                .isInstanceOf(InvalidPortraitException.class)
                .hasMessageContaining("4096");
    }

    private static void paint(BufferedImage image, int fromX, int toX, Color color) {
        for (int x = fromX; x < toX; x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y, color.getRGB());
            }
        }
    }

    private static byte[] encode(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, format, bytes);
        return bytes.toByteArray();
    }

    private static BufferedImage read(byte[] bytes) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
