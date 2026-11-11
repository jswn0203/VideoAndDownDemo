package com.example.videoanddowndemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.videoanddowndemo.ascii.AsciiCodec;

import org.junit.Test;

public class AsciiCodecTest {

    private static final String RAMP = AsciiCodec.DEFAULT_RAMP;

    private static int[] solidPixels(int width, int height, int gray) {
        int[] pixels = new int[width * height];
        int argb = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
        java.util.Arrays.fill(pixels, argb);
        return pixels;
    }

    @Test
    public void blackFrameMapsToFirstRampChar() {
        String art = AsciiCodec.encode(solidPixels(20, 20, 0), 20, 20, 4, 4, RAMP);
        assertEquals(4, art.split("\n").length);
        for (String line : art.split("\n")) {
            assertEquals(4, line.length());
            assertEquals("    ", line);
        }
    }

    @Test
    public void whiteFrameMapsToLastRampChar() {
        String art = AsciiCodec.encode(solidPixels(20, 20, 255), 20, 20, 3, 2, RAMP);
        assertEquals("@@@@@@", art.replace("\n", ""));
    }

    @Test
    public void brighterPixelsMapToDenserChars() {
        // 左半黑、右半白，第一行应该前半最暗字符、后半最亮字符
        int w = 10, h = 10;
        int[] pixels = new int[w * h];
        int dark = 0xFF000000;
        int white = 0xFFFFFFFF;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixels[y * w + x] = x < w / 2 ? dark : white;
            }
        }
        String art = AsciiCodec.encode(pixels, w, h, 2, 1, RAMP);
        assertEquals(2, art.length());
        assertEquals(' ', art.charAt(0));
        assertEquals('@', art.charAt(1));
    }

    @Test
    public void targetRowsKeepsAspectRoughly() {
        // 16:9 视频、100 列 -> 行数约 100 * 9/16 * 0.5 = 28
        int rows = AsciiCodec.targetRows(1920, 1080, 100);
        assertEquals(28, rows);
        assertTrue(AsciiCodec.targetRows(1080, 1920, 100) >= 50);
    }

    @Test
    public void targetRowsRejectsInvalidInput() {
        try {
            AsciiCodec.targetRows(0, 10, 10);
            fail("should throw");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void encodeRejectsInvalidInput() {
        try {
            AsciiCodec.encode(new int[4], 10, 10, 4, 4, RAMP);
            fail("should throw");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void encodeLineCountMatchesRows() {
        String art = AsciiCodec.encode(solidPixels(32, 32, 128), 32, 32, 8, 5, RAMP);
        assertEquals(5, art.split("\n").length);
        assertEquals(8 * 5 + 4, art.length());
    }
}
