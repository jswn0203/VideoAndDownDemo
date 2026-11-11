package com.example.videoanddowndemo.ascii;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 字符画动画渲染控件：黑底绿字，等宽字体，
 * 自动根据控件大小缩放字号，使一帧完整显示在屏幕内。
 */
public class AsciiView extends View {

    private static final int DEFAULT_FG_COLOR = 0xFF33FF66;
    private static final int DEFAULT_BG_COLOR = 0xFF000000;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String[] lines;

    public AsciiView(Context context) {
        super(context);
        init();
    }

    public AsciiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AsciiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setColor(DEFAULT_FG_COLOR);
    }

    /** 设置当前帧（'\n' 分隔的多行字符画），传 null 清屏 */
    public void setFrame(String frame) {
        if (frame == null || frame.length() == 0) {
            lines = null;
        } else {
            lines = frame.split("\n");
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(DEFAULT_BG_COLOR);
        if (lines == null || lines.length == 0) {
            return;
        }
        int rows = lines.length;
        int cols = 0;
        for (String line : lines) {
            cols = Math.max(cols, line.length());
        }
        if (cols == 0) {
            return;
        }
        float w = getWidth();
        float h = getHeight();

        // 先在基准字号下测量单个字符的宽高比例
        float baseSize = 100f;
        paint.setTextSize(baseSize);
        float charW = paint.measureText("M") / baseSize;
        Paint.FontMetrics fm = paint.getFontMetrics();
        float lineH = (fm.descent - fm.ascent) / baseSize;

        float size = Math.min(w / (cols * charW), h / (rows * lineH));
        if (size <= 0f || Float.isNaN(size) || Float.isInfinite(size)) {
            return;
        }
        paint.setTextSize(size);
        float cw = charW * size;
        Paint.FontMetrics fm2 = paint.getFontMetrics();
        float lh = fm2.descent - fm2.ascent;
        float baseline = -fm2.ascent;
        float left = (w - cols * cw) / 2f;
        float top = (h - rows * lh) / 2f;

        for (int i = 0; i < rows; i++) {
            canvas.drawText(lines[i], left, top + baseline + i * lh, paint);
        }
    }
}
