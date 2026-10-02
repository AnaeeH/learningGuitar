package com.herbillon.guitar.ui.chords;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.herbillon.guitar.R;
import com.herbillon.guitar.model.Chord;
import com.herbillon.guitar.model.ChordPosition;


public class ChordDiagramView extends View {
    private static final int STRING_COUNT = 6;
    private static final int FRET_COUNT = 4;

    private int colorString;
    private int colorNut;
    private int colorBarre;
    private int colorFinger;
    private int colorMuted;

    private Chord chord;

    private final Paint stringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barrePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fingerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mutedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ChordDiagramView(Context context) {
        super(context);
        init();
    }

    public ChordDiagramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChordDiagramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        colorString = Color.parseColor("#52525B");
        colorNut    = ContextCompat.getColor(getContext(), R.color.app_accent_soft);
        colorBarre  = ContextCompat.getColor(getContext(), R.color.app_accent);
        colorFinger = ContextCompat.getColor(getContext(), R.color.app_accent);
        colorMuted  = Color.parseColor("#52525B");

        stringPaint.setColor(colorString);
        stringPaint.setStyle(Paint.Style.STROKE);

        nutPaint.setColor(colorNut);
        nutPaint.setStyle(Paint.Style.FILL);

        barrePaint.setColor(colorBarre);
        barrePaint.setStyle(Paint.Style.FILL);

        fingerPaint.setColor(colorFinger);
        fingerPaint.setStyle(Paint.Style.FILL);

        mutedPaint.setColor(colorMuted);
        mutedPaint.setStyle(Paint.Style.STROKE);
        mutedPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setChord(Chord chord) {
        this.chord = chord;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (chord == null) return;

        int width = getWidth();
        int height = getHeight();

        float padX = width * 0.12f;
        float padTop = height * 0.18f;
        float padBottom = height * 0.08f;

        float gridWidth = width - 2 * padX;
        float gridHeight = height - padTop - padBottom;

        float stringSpacing = gridWidth / (STRING_COUNT - 1);
        float fretSpacing = gridHeight / FRET_COUNT;

        stringPaint.setStrokeWidth(Math.max(2f, width * 0.012f));
        mutedPaint.setStrokeWidth(Math.max(3f, width * 0.02f));


        for (int i = 0; i < STRING_COUNT; i++) {
            float x = padX + i * stringSpacing;
            canvas.drawLine(x, padTop, x, padTop + gridHeight, stringPaint);
        }

        for (int i = 0; i <= FRET_COUNT-1; i++) {
            float y = padTop + i * fretSpacing;
            canvas.drawLine(padX, y, padX + gridWidth, y, stringPaint);
        }

        if (!chord.hasBarre()) {
            float nutHeight = fretSpacing * 0.18f;
            RectF nutRect = new RectF(
                    padX - stringPaint.getStrokeWidth() / 2,
                    padTop - nutHeight / 2,
                    padX + gridWidth + stringPaint.getStrokeWidth() / 2,
                    padTop + nutHeight / 2
            );
            canvas.drawRoundRect(nutRect, nutHeight / 2, nutHeight / 2, nutPaint);
        }

        if (chord.hasBarre()) {
            int barreFret = chord.getBarreFret();
            int fromString = chord.getBarreFromString();
            int toString = chord.getBarreToString();

            float xFrom = padX + (STRING_COUNT - toString) * stringSpacing;
            float xTo = padX + (STRING_COUNT - fromString) * stringSpacing;
            float yCenter = padTop + (barreFret - 0.5f) * fretSpacing;
            float barreHeight = fretSpacing * 0.35f;

            RectF barreRect = new RectF(
                    xFrom - barreHeight * 0.4f,
                    yCenter - barreHeight / 2,
                    xTo + barreHeight * 0.4f,
                    yCenter + barreHeight / 2
            );
            canvas.drawRoundRect(barreRect, barreHeight / 2, barreHeight / 2, barrePaint);
        }

        float circleRadius = Math.min(stringSpacing, fretSpacing) * 0.32f;
        if (chord.getPositions() != null) {
            for (ChordPosition pos : chord.getPositions()) {
                float cx = padX + (STRING_COUNT - pos.getString()) * stringSpacing;
                float cy = padTop + (pos.getFret() - 0.5f) * fretSpacing;
                canvas.drawCircle(cx, cy, circleRadius, fingerPaint);
            }
        }

        if (chord.getMutedStrings() != null) {
            float xSize = circleRadius * 0.55f;
            float yCenter = padTop - padTop * 0.35f;
            for (int string : chord.getMutedStrings()) {
                float cx = padX + (STRING_COUNT - string) * stringSpacing;
                canvas.drawLine(cx - xSize, yCenter - xSize, cx + xSize, yCenter + xSize, mutedPaint);
                canvas.drawLine(cx + xSize, yCenter - xSize, cx - xSize, yCenter + xSize, mutedPaint);
            }
        }
    }
}