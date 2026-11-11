package com.example.videoanddowndemo.ascii;

/**
 * 视频帧 -> ASCII 字符画的核心编码逻辑。
 * 纯 Java 实现，不依赖 Android 运行时，方便单元测试。
 */
public class AsciiCodec {

    /** 经典的由暗到亮字符梯度 */
    public static final String DEFAULT_RAMP = " .:-=+*#%@";

    private AsciiCodec() {
    }

    /**
     * 根据源图宽高与目标列数，计算字符画的行数。
     * 终端字符的高约为宽的 2 倍，所以行数要按 0.5 倍压缩，保持画面不变形。
     *
     * @param srcW 源图宽
     * @param srcH 源图高
     * @param cols 字符画列数
     * @return 字符画行数（>= 2）
     */
    public static int targetRows(int srcW, int srcH, int cols) {
        if (srcW <= 0 || srcH <= 0 || cols <= 0) {
            throw new IllegalArgumentException("invalid src size or cols");
        }
        int rows = Math.round((float) srcH / srcW * cols * 0.5f);
        return Math.max(2, rows);
    }

    /**
     * 把一帧像素编码成 ASCII 字符画。
     *
     * @param pixels ARGB 像素数组（Bitmap.getPixels 的结果）
     * @param width  像素数组宽
     * @param height 像素数组高
     * @param cols   字符画列数
     * @param rows   字符画行数
     * @param ramp   由暗到亮的字符梯度
     * @return 每行以 '\n' 分隔的字符画文本
     */
    public static String encode(int[] pixels, int width, int height,
                                int cols, int rows, String ramp) {
        if (pixels == null || width <= 0 || height <= 0
                || cols <= 0 || rows <= 0 || ramp == null || ramp.length() == 0) {
            throw new IllegalArgumentException("invalid encode arguments");
        }
        if (pixels.length < width * height) {
            throw new IllegalArgumentException("pixels too small");
        }
        StringBuilder sb = new StringBuilder(cols * (rows + 1));
        for (int r = 0; r < rows; r++) {
            int y0 = r * height / rows;
            int y1 = Math.max((r + 1) * height / rows, y0 + 1);
            for (int c = 0; c < cols; c++) {
                int x0 = c * width / cols;
                int x1 = Math.max((c + 1) * width / cols, x0 + 1);
                long sum = 0;
                long n = 0;
                for (int y = y0; y < y1 && y < height; y++) {
                    int base = y * width;
                    for (int x = x0; x < x1 && x < width; x++) {
                        int p = pixels[base + x];
                        int lum = (77 * ((p >> 16) & 0xFF)
                                + 151 * ((p >> 8) & 0xFF)
                                + 28 * (p & 0xFF)) >> 8;
                        sum += lum;
                        n++;
                    }
                }
                int avg = n > 0 ? (int) (sum / n) : 0;
                int idx = Math.min(ramp.length() - 1, avg * ramp.length() / 256);
                sb.append(ramp.charAt(idx));
            }
            if (r < rows - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
