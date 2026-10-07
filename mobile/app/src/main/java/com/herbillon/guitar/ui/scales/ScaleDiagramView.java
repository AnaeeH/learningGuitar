package com.herbillon.guitar.ui.scales;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.herbillon.guitar.R;
import com.herbillon.guitar.model.ScaleNote;
import com.herbillon.guitar.model.ScalePosition;


public class ScaleDiagramView extends View {
    private static final int STRING_COUNT = 6;
    private static final int FRET_COUNT = 5;

    private int colorString;
    private int colorNote;
    private int colorRoot;

    private ScalePosition position;

    private final Paint stringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint notePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rootPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ScaleDiagramView(Context context) {
        super(context);
        init();
    }

    public ScaleDiagramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScaleDiagramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        colorString = Color.parseColor("#52525B");
        colorNote   = ContextCompat.getColor(getContext(), R.color.app_accent);
        colorRoot   = ContextCompat.getColor(getContext(), R.color.app_accent_soft);

        stringPaint.setColor(colorString);
        stringPaint.setStyle(Paint.Style.STROKE);

        notePaint.setColor(colorNote);
        notePaint.setStyle(Paint.Style.FILL);

        rootPaint.setColor(colorRoot);
        rootPaint.setStyle(Paint.Style.FILL);
    }

    public void setPosition(ScalePosition position) {
        this.position = position;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (position == null || position.getNotes() == null) return;

        int width = getWidth();
        int height = getHeight();

        float padX = width * 0.12f;
        float padTop = height * 0.08f;
        float padBottom = height * 0.08f;

        float gridWidth = width - 2 * padX;
        float gridHeight = height - padTop - padBottom;

        float stringSpacing = gridWidth / (STRING_COUNT - 1);
        float fretSpacing = gridHeight / FRET_COUNT;

        stringPaint.setStrokeWidth(Math.max(2f, width * 0.012f));

        for (int i = 0; i < STRING_COUNT; i++) {
            float x = padX + i * stringSpacing;
            canvas.drawLine(x, padTop, x, padTop + gridHeight, stringPaint);
        }

        for (int i = 0; i <= FRET_COUNT; i++) {
            float y = padTop + i * fretSpacing;
            canvas.drawLine(padX, y, padX + gridWidth, y, stringPaint);
        }

        float noteRadius = Math.min(stringSpacing, fretSpacing) * 0.30f;
        float rootRadius = noteRadius * 1.15f;

        for (ScaleNote note : position.getNotes()) {
            float cx = padX + (STRING_COUNT - note.getString()) * stringSpacing;
            float cy = padTop + (note.getFret() - 0.5f) * fretSpacing;

            if (note.getIsRoot()) {
                canvas.drawCircle(cx, cy, rootRadius, rootPaint);
            } else {
                canvas.drawCircle(cx, cy, noteRadius, notePaint);
            }
        }
    }
}