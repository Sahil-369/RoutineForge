package com.routineforge.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircularProgressView extends View {

    private Paint bgPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF oval;
    private int progress = 0;
    private int strokeWidth = 20;

    public CircularProgressView(Context context) { super(context); init(); }
    public CircularProgressView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public CircularProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle); init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(strokeWidth);
        bgPaint.setColor(0x33FFFFFF);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setColor(0xFF4CAF50);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        oval = new RectF();
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        updateColor();
        invalidate();
    }

    private void updateColor() {
        if (progress >= 80) progressPaint.setColor(0xFF4CAF50);       // Green
        else if (progress >= 50) progressPaint.setColor(0xFFFF9800);  // Orange
        else progressPaint.setColor(0xFFFF5252);                       // Red
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float padding = strokeWidth / 2f + 4;
        oval.set(padding, padding, w - padding, h - padding);

        // Background arc
        canvas.drawArc(oval, -90, 360, false, bgPaint);

        // Progress arc
        float sweep = (progress / 100f) * 360f;
        canvas.drawArc(oval, -90, sweep, false, progressPaint);

        // Text
        float cx = w / 2f;
        float cy = h / 2f;
        textPaint.setTextSize(w * 0.22f);
        canvas.drawText(progress + "%", cx, cy + textPaint.getTextSize() * 0.35f, textPaint);

        // Label
        textPaint.setTextSize(w * 0.10f);
        textPaint.setColor(0x99FFFFFF);
        canvas.drawText("Today", cx, cy + textPaint.getTextSize() * 5f, textPaint);
        textPaint.setColor(0xFFFFFFFF);
    }
}
