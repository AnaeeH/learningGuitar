package com.herbillon.guitar.ui.music;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.herbillon.guitar.R;

public class SheetView extends View {

    private int nbMeasures = 8;
    private int time1 = 4;
    private int time2 = 4;
    private int nbRaws = 5;
    private float measureWidth = 300f;
    private float lineSpacing = 30f;
    private float startOffset = 0f;

    private static final float LEFT_MARGIN = 100f;
    private static final float UP_MARGIN = 80f;
    private static final int EMPTY_MEASURES_AFTER = 2;

    private Paint rawPaint;
    private Paint textPaint;
    private Paint barPaint;
    private Paint measuresPaint;

    private int colorLine;
    private int colorBar;

    public SheetView(Context context) {
        super(context);
        init(context);
    }

    public SheetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setStartOffset(float offset) {
        this.startOffset = offset;
        requestLayout();
        invalidate();
    }

    public float getMeasureWidth() {
        return measureWidth;
    }

    private void init(Context context) {
        colorLine = ContextCompat.getColor(context, R.color.dark_line);
        colorBar  = ContextCompat.getColor(context, R.color.dark_text);
        int colorSecondary = ContextCompat.getColor(context, R.color.dark_textSecondary);

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        measureWidth = dm.widthPixels / 3f;

        rawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rawPaint.setColor(colorLine);
        rawPaint.setStrokeWidth(5f);

        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(colorSecondary);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(colorSecondary);
        textPaint.setTextSize(40f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        measuresPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        measuresPaint.setColor(colorBar);
        measuresPaint.setTextSize(60f);
        measuresPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setMusique(int nbMeasures, int time1, int time2, int nbRaws) {
        this.nbMeasures = nbMeasures;
        this.time1 = time1;
        this.time2 = time2;
        this.nbRaws = nbRaws;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float totalWidth = startOffset + LEFT_MARGIN + (measureWidth * (nbMeasures + EMPTY_MEASURES_AFTER)) + 20f;
        int availableHeight = MeasureSpec.getSize(heightMeasureSpec);
        float totalHeight = availableHeight > 0 ? availableHeight : UP_MARGIN + (lineSpacing * (nbRaws - 1)) + UP_MARGIN;

        setMeasuredDimension((int) totalWidth, (int) totalHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = startOffset;
        float height = getHeight();

        float rangeHeight = height * 0.6f;
        float yStart = (height - rangeHeight) / 2f;
        float yFin = yStart + rangeHeight;
        float lineSpacing = rangeHeight / (nbRaws - 1);

        float xTotal = left + measureWidth * nbMeasures;

        for (int i = 0; i < nbRaws; i++) {
            float y = yStart + i * lineSpacing;
            canvas.drawLine(0, y, xTotal, y, rawPaint);
        }

        barPaint.setStrokeWidth(8f);
        canvas.drawLine(left, yStart, left, yFin, barPaint);
        barPaint.setStrokeWidth(6f);

        float xMeasure = left - 40f;
        float yCenter = yStart + rangeHeight / 2f;
        canvas.drawText(String.valueOf(time1), xMeasure, yCenter - 30f, measuresPaint);
        canvas.drawText(String.valueOf(time2), xMeasure, yCenter + 30f, measuresPaint);

        for (int i = 1; i <= nbMeasures; i++) {
            float xBar = left + i * measureWidth;
            canvas.drawLine(xBar, yStart, xBar, yFin, barPaint);

            float xCentre = left + (i - 1) * measureWidth + measureWidth / 2f;
            canvas.drawText("" + i, xCentre, yFin + 40f, textPaint);
        }

        float xEnd = left + nbMeasures * measureWidth;
        barPaint.setStrokeWidth(2f);
        canvas.drawLine(xEnd, yStart, xEnd, yFin, barPaint);
        barPaint.setStrokeWidth(8f);
        canvas.drawLine(xEnd + 8f, yStart, xEnd + 8f, yFin, barPaint);
        barPaint.setStrokeWidth(2f);
    }
}