package com.example.videoanddowndemo.ascii;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

/**
 * 从视频文件中抽取若干帧并转成 ASCII 字符画帧序列。
 * 抽帧在后台线程进行，回调统一切回主线程。
 */
public class AsciiVideoProcessor {

    /** 最多抽取的帧数，防止长视频占用过多内存/时间 */
    private static final int MAX_FRAMES = 120;
    /** 抽帧间隔下限（毫秒），避免短视频抽太密 */
    private static final long MIN_INTERVAL_MS = 40;
    /** 抽帧间隔上限（毫秒），保证动画连贯 */
    private static final long MAX_INTERVAL_MS = 150;

    public interface Callback {
        /** 抽帧进度 */
        void onProgress(int done, int total);

        /** 抽帧完成，frames 为每帧字符画文本 */
        void onDone(List<String> frames, long frameIntervalMs);

        /** 失败 */
        void onError(String message);
    }

    private volatile boolean cancelled;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Thread worker;

    /**
     * 开始解析视频。
     *
     * @param path 视频文件路径
     * @param cols 字符画列数
     */
    public void start(final String path, final int cols, final Callback callback) {
        cancel();
        cancelled = false;
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                process(path, cols, callback);
            }
        }, "ascii-processor");
        worker.start();
    }

    /** 取消正在进行的解析 */
    public void cancel() {
        cancelled = true;
        Thread t = worker;
        if (t != null && t.isAlive()) {
            t.interrupt();
        }
        worker = null;
    }

    private void process(String path, int cols, Callback callback) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            try {
                retriever.setDataSource(path);
            } catch (Exception e) {
                postError(callback, "无法读取视频文件");
                return;
            }
            String durStr = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationMs = durStr != null ? Long.parseLong(durStr) : 0;
            if (durationMs <= 0) {
                postError(callback, "无法获取视频时长");
                return;
            }

            long intervalMs = Math.max(MIN_INTERVAL_MS,
                    Math.min(MAX_INTERVAL_MS, durationMs / MAX_FRAMES));
            int frameCount = (int) Math.min(MAX_FRAMES, durationMs / intervalMs);
            if (frameCount < 1) {
                frameCount = 1;
            }

            List<String> frames = new ArrayList<>(frameCount);
            for (int i = 0; i < frameCount; i++) {
                if (cancelled || Thread.currentThread().isInterrupted()) {
                    return;
                }
                long timeUs = (durationMs - 1) * i * 1000L / frameCount;
                Bitmap src = null;
                try {
                    src = retriever.getFrameAtTime(timeUs,
                            MediaMetadataRetriever.OPTION_CLOSEST);
                } catch (Exception ignore) {
                    // 单帧失败直接跳过
                }
                if (src != null && src.getWidth() > 0 && src.getHeight() > 0) {
                    String art = frameToAscii(src, cols);
                    if (art != null) {
                        frames.add(art);
                    }
                    src.recycle();
                }
                postProgress(callback, i + 1, frameCount);
            }

            if (cancelled || frames.isEmpty()) {
                if (!cancelled) {
                    postError(callback, "未能从视频中提取到画面");
                }
                return;
            }
            postDone(callback, frames, intervalMs);
        } finally {
            try {
                retriever.release();
            } catch (Exception ignore) {
            }
        }
    }

    /** 把一帧 Bitmap 缩放到目标分辨率并编码成字符画 */
    private static String frameToAscii(Bitmap src, int cols) {
        int rows = AsciiCodec.targetRows(src.getWidth(), src.getHeight(), cols);
        int cellH = rows * 2; // 用 2 倍行高的像素近似字符块，交由 encode 平均
        int scaledW = cols;
        int scaledH = cellH;
        Bitmap small = null;
        try {
            small = Bitmap.createScaledBitmap(src, scaledW, scaledH, true);
            int[] pixels = new int[scaledW * scaledH];
            small.getPixels(pixels, 0, scaledW, 0, 0, scaledW, scaledH);
            return AsciiCodec.encode(pixels, scaledW, scaledH, cols, rows,
                    AsciiCodec.DEFAULT_RAMP);
        } catch (OutOfMemoryError e) {
            return null;
        } finally {
            if (small != null && small != src) {
                small.recycle();
            }
        }
    }

    private void postProgress(final Callback cb, final int done, final int total) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                cb.onProgress(done, total);
            }
        });
    }

    private void postDone(final Callback cb, final List<String> frames, final long interval) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                cb.onDone(frames, interval);
            }
        });
    }

    private void postError(final Callback cb, final String msg) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                cb.onError(msg);
            }
        });
    }
}
