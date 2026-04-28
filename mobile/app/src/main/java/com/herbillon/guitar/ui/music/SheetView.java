package com.herbillon.guitar.ui.music;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.herbillon.guitar.R;
import com.herbillon.guitar.model.Beat;
import com.herbillon.guitar.model.Measure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SheetView extends View {

    // Musical data
    private int nbMeasures = 8;
    private int time1 = 4;
    private int time2 = 4;
    private List<Measure> measures;
    private int nbRaws = 5;
    private boolean hasPickupMeasure = false;

    // Dimensions
    private float measureWidth = 300f;
    private float startOffset = 0f;

    private static final float LEFT_MARGIN = 100f;
    private static final float UP_MARGIN = 80f;
    private static final int EMPTY_MEASURES_AFTER = 2;
    private static final float STAFF_HEIGHT_RATIO = 0.6f;

    // Paint
    private Paint rawPaint;
    private Paint textPaint;
    private Paint barPaint;
    private Paint fretPaint;
    private Paint bgPaint;
    private Paint chordNamePaint;

    // Colors
    private int colorSecondary;
    private int colorChords;


    public SheetView(Context context) {
        super(context);
        init(context);
    }

    public SheetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        int colorLine = ContextCompat.getColor(context, R.color.dark_line);
        int colorBar  = ContextCompat.getColor(context, R.color.dark_text);
        int colorBackground = ContextCompat.getColor(context, R.color.dark_background);
        colorSecondary = ContextCompat.getColor(context, R.color.dark_textSecondary);
        colorChords = ContextCompat.getColor(context, R.color.dark_mainColor);

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        measureWidth = dm.widthPixels / 3f;

        rawPaint = buildPaint(colorLine, 5f);
        barPaint = buildPaint(colorSecondary, 2f);
        textPaint = buildTextPaint(colorSecondary, 40f);
        fretPaint = buildTextPaint(colorBar,30f);

        bgPaint = buildPaint(colorBackground, 0f);
        bgPaint.setStyle(Paint.Style.FILL);

        chordNamePaint = buildTextPaint(colorSecondary, 30f);
        chordNamePaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private Paint buildPaint(int color, float strokeWidth) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStrokeWidth(strokeWidth);
        return p;
    }

    private Paint buildTextPaint(int color, float textSize) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTextSize(textSize);
        p.setTextAlign(Paint.Align.CENTER);
        return p;
    }


    public void setMusique(List<Measure> measures, int nbMeasures, int time1, int time2, int nbRaws) {
        this.measures = measures;
        this.nbMeasures = nbMeasures;
        this.time1 = time1;
        this.time2 = time2;
        this.nbRaws = nbRaws;
        requestLayout();
        invalidate();
    }

    public void setStartOffset(float offset) {
        this.startOffset = offset;
        requestLayout();
        invalidate();
    }

    public void setHasPickupMeasure(boolean hasPickupMeasure) {
        this.hasPickupMeasure = hasPickupMeasure;
        requestLayout();
        invalidate();
    }

    public void setMeasures(List<Measure> measures) {
        this.measures = measures;
        this.nbMeasures = measures.size();
        requestLayout();
        invalidate();
    }

    public float getMeasureWidth() {
        return measureWidth;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalMeasures = totalMeasures();
        float totalWidth = startOffset + LEFT_MARGIN + (measureWidth * (totalMeasures + EMPTY_MEASURES_AFTER)) + 20f;
        int availableHeight = MeasureSpec.getSize(heightMeasureSpec);
        float totalHeight = availableHeight > 0 ? availableHeight : UP_MARGIN * 2 + 30f * (nbRaws - 1);

        setMeasuredDimension((int) totalWidth, (int) totalHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = startOffset;
        float rangeHeight = getHeight() * STAFF_HEIGHT_RATIO;
        float yStart = (getHeight() - rangeHeight) / 2f;
        float yFin = yStart + rangeHeight;
        float lineSpacing = rangeHeight / (nbRaws - 1);
        int totalMeasures = totalMeasures();
        float xTotal = left + measureWidth * totalMeasures;

        fretPaint.setTextSize(lineSpacing * 0.9f);
        chordNamePaint.setTextSize(lineSpacing * 1.8f);

        drawStaff(canvas, left, yStart, yFin, lineSpacing, totalMeasures, xTotal);
        drawTablature(canvas, left, yStart, lineSpacing);
    }

    private void drawStaff(Canvas canvas, float left, float yStart, float yFin,
                           float lineSpacing, int totalMeasures, float xTotal) {
        // horizontal raw's
        for (int i = 0; i < nbRaws; i++) {
            float y = yStart + i * lineSpacing;
            canvas.drawLine(0, y, xTotal, y, rawPaint);
        }

        // start bar
        barPaint.setStrokeWidth(8f);
        canvas.drawLine(left, yStart, left, yFin, barPaint);

        // measure bar
        barPaint.setStrokeWidth(6f);
        for (int i = 1; i <= totalMeasures; i++) {
            float xBar = left + i * measureWidth;
            canvas.drawLine(xBar, yStart, xBar, yFin, barPaint);
        }

        // end bar
        float xEnd = left + totalMeasures * measureWidth;
        barPaint.setStrokeWidth(2f);
        canvas.drawLine(xEnd, yStart, xEnd, yFin, barPaint);
        barPaint.setStrokeWidth(8f);
        canvas.drawLine(xEnd + 8f, yStart, xEnd + 8f, yFin, barPaint);
        barPaint.setStrokeWidth(2f);
    }

    private void drawTablature(Canvas canvas, float left, float yStart, float lineSpacing) {
        if (measures == null) return;

        int measureDuration = getMeasureTotalDuration();

        for (int m = 0; m < measures.size(); m++) {
            int drawIndex = hasPickupMeasure ? m + 1 : m;
            Measure measure = measures.get(m);
            float xMeasureStart = left + drawIndex * measureWidth;

            // Regroup beats by position
            Map<Integer, List<Beat>> beatsByPosition = new LinkedHashMap<>();
            for (Beat beat : measure.beats) {
                if (beat.isRest || beat.string == -1) continue;
                beatsByPosition.computeIfAbsent(beat.position, k -> new ArrayList<>()).add(beat);
            }

            for (Map.Entry<Integer, List<Beat>> entry : beatsByPosition.entrySet()) {
                float x = xMeasureStart + ((float) entry.getKey() / measureDuration) * measureWidth;
                List<Beat> group = entry.getValue();
                Beat first = group.get(0);
                boolean isChord = first.harmonyText != null && !first.harmonyText.isEmpty();

                if (isChord) {
                    drawChord(canvas, x, yStart, lineSpacing, group, first.harmonyText, first.strumDirection);
                } else {
                    for (Beat beat : group) {
                        drawSingleNote(canvas, x, yStart, lineSpacing, beat);
                    }
                }
            }
        }
    }

    private void drawChord(Canvas canvas, float x, float yStart, float lineSpacing,
                           List<Beat> beats, String harmonyText, String strumDirection) {
        int minString = Integer.MAX_VALUE;
        int maxString = Integer.MIN_VALUE;
        for (Beat beat : beats) {
            minString = Math.min(minString, beat.string);
            maxString = Math.max(maxString, beat.string);
        }

        float yTop    = yStart + (minString - 1) * lineSpacing;
        float yBottom = yStart + (maxString - 1) * lineSpacing;
        float padding = lineSpacing * 0.4f;

        // Vertical bar
        barPaint.setStrokeWidth(25f);
        barPaint.setColor(colorChords);
        barPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(x, yTop + padding, x, yBottom - padding, barPaint);
        barPaint.setColor(colorSecondary);
        barPaint.setStrokeCap(Paint.Cap.BUTT);
        barPaint.setStrokeWidth(2f);

        // Chord name
        chordNamePaint.setTextSize(lineSpacing * 0.6f);
        canvas.drawText(harmonyText, x, yStart - lineSpacing * 0.6f, chordNamePaint);
        chordNamePaint.setTextSize(lineSpacing * 1.8f);

        // Strum direction
        if (strumDirection != null) {
            String symbol = "down".equals(strumDirection) ? "↓" : "↑";
            canvas.drawText(symbol, x, yBottom + lineSpacing * 1.4f, chordNamePaint);
        }
    }

    private void drawSingleNote(Canvas canvas, float x, float yStart, float lineSpacing, Beat beat) {
        if (beat.tied) return;

        float y = yStart + (beat.string - 1) * lineSpacing;
        String label = String.valueOf(beat.fret);
        float halfW = fretPaint.measureText(label) / 2f + lineSpacing * 0.25f;
        float halfH = lineSpacing * 0.4f;

        // Note background
        Paint noteBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        noteBg.setColor(colorChords);
        noteBg.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(x - halfW, y - halfH, x + halfW, y + halfH,
                halfH * 0.5f, halfH * 0.5f, noteBg);

        // Note
        Paint noteText = new Paint(Paint.ANTI_ALIAS_FLAG);
        noteText.setColor(colorSecondary);
        noteText.setTextSize(fretPaint.getTextSize() / 1.8f);
        noteText.setTextAlign(Paint.Align.CENTER);
        noteText.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText(label, x, y + fretPaint.getTextSize() / 4.8f, noteText);
    }

    private int totalMeasures() {
        return nbMeasures + (hasPickupMeasure ? 1 : 0);
    }

    private int getMeasureTotalDuration() {
        if (measures == null || measures.isEmpty()) return time1 * 2;
        int max = 0;
        for (Measure m : measures) {
            for (Beat b : m.beats) {
                max = Math.max(max, b.position + b.duration);
            }
        }
        return max > 0 ? max : time1 * 2;
    }

    private void drawPartition(Canvas canvas, float left, float yStart, float lineSpacing) {
        //
    }
}