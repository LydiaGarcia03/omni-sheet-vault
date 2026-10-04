package dev.omnisheetvault.api.character;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Turns an uploaded file into the portrait that is stored: it must decode as a PNG or JPEG
 * of sane size, and it is always re-encoded as a centre-cropped square PNG. Re-encoding
 * drops metadata (EXIF location included) and anything that isn't pixels.
 */
final class PortraitImage {

    static final String CONTENT_TYPE = "image/png";
    static final int MAX_SIZE = 512;
    static final int MAX_SOURCE_SIDE = 4096;
    private static final Set<String> ACCEPTED_FORMATS = Set.of("png", "jpeg");

    private PortraitImage() {
    }

    static byte[] normalize(byte[] upload) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(upload))) {
            Iterator<ImageReader> readers = input == null ? null : ImageIO.getImageReaders(input);
            if (readers == null || !readers.hasNext()) {
                throw new InvalidPortraitException("The portrait must be a PNG or JPEG image.");
            }
            ImageReader reader = readers.next();
            try {
                if (!ACCEPTED_FORMATS.contains(reader.getFormatName().toLowerCase(Locale.ROOT))) {
                    throw new InvalidPortraitException("The portrait must be a PNG or JPEG image.");
                }
                reader.setInput(input, true, true);
                if (reader.getWidth(0) > MAX_SOURCE_SIDE || reader.getHeight(0) > MAX_SOURCE_SIDE) {
                    throw new InvalidPortraitException("The portrait must be at most " + MAX_SOURCE_SIDE + " pixels on each side.");
                }
                return encode(squareCrop(reader.read(0)));
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new InvalidPortraitException("The portrait could not be read as an image.");
        }
    }

    private static BufferedImage squareCrop(BufferedImage source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        int target = Math.min(side, MAX_SIZE);
        BufferedImage square = new BufferedImage(target, target, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = square.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, target, target, x, y, x + side, y + side, null);
        } finally {
            graphics.dispose();
        }
        return square;
    }

    private static byte[] encode(BufferedImage image) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes);
        return bytes.toByteArray();
    }
}
