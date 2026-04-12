package com.herbillon.guitar.ui.music;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.herbillon.guitar.model.Measure;

import java.util.List;

public abstract class AbstractMusicFragment extends Fragment {

    protected int tempo;
    protected int time1;
    protected int time2;
    protected List<Measure> measures;
    protected boolean hasPickupMeasure;

    private ValueAnimator scrollAnimator;
    private float scrollPosition = 0f;
    private float cursorOffsetPx = 0f;
    private boolean isPlaying = false;
    private long totalDurationMs = 0;
    private String totalDurationFormatted = "0:00";

    protected abstract HorizontalScrollView getScrollView();
    protected abstract SheetView getSheetView();
    protected abstract View getBtnPlay();
    protected abstract View getBtnSkipBack();
    protected abstract View getBtnSkipForward();
    protected abstract SeekBar getSeekBar();
    protected abstract TextView getTextDuration();
    protected abstract View getPlaybackCursor();


    private float getTotalScrollable() {
        int total = measures.size() + (hasPickupMeasure ? 1 : 0);
        return getSheetView().getMeasureWidth() * total;
    }

    private String formatTime(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private void updateProgress(float progression) {
        getSeekBar().setProgress((int) (progression * 100));
        int elapsedSeconds = (int) (totalDurationMs * progression / 1000);
        getTextDuration().setText(formatTime(elapsedSeconds) + " / " + totalDurationFormatted);
    }

    protected void calculateTotalDuration() {
        float secondsPerMeasure = time1 * (60f / tempo);
        int total = measures.size() + (hasPickupMeasure ? 1 : 0);
        int totalSeconds = (int) (secondsPerMeasure * total);
        totalDurationMs = totalSeconds * 1000L;
        totalDurationFormatted = formatTime(totalSeconds);
    }

    protected void setupControls() {
        getSeekBar().setMax(100);
        updateProgress(0f);

        getPlaybackCursor().post(() -> {
            cursorOffsetPx = getScrollView().getWidth() / 3.5f;
            getPlaybackCursor().setTranslationX(cursorOffsetPx);
            getSheetView().setStartOffset(cursorOffsetPx);
        });

        getBtnPlay().setOnClickListener(v -> {
            if (isPlaying) {
                stopScroll();
                ((Button) getBtnPlay()).setText("▶");
            } else {
                startScroll();
                ((Button) getBtnPlay()).setText("⏸");
            }
        });

        getBtnSkipBack().setOnClickListener(v -> skipSeconds(-10));
        getBtnSkipForward().setOnClickListener(v -> skipSeconds(10));

        getSeekBar().setOnSeekBarChangeListener(buildSeekBarListener());
    }

    private SeekBar.OnSeekBarChangeListener buildSeekBarListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                float progression = progress / 100f;
                scrollPosition = getTotalScrollable() * progression;
                getScrollView().scrollTo((int) scrollPosition, 0);
                updateProgress(progression);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopScroll();
                ((Button) getBtnPlay()).setText("▶");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPlaying) startScroll();
            }
        };
    }

    private void skipSeconds(int seconds) {
        float totalPixels = getTotalScrollable();
        float pixelsPerMs = totalPixels / totalDurationMs;
        scrollPosition = Math.max(0f, Math.min(scrollPosition + seconds * 1000f * pixelsPerMs, totalPixels));

        getScrollView().scrollTo((int) scrollPosition, 0);
        updateProgress(scrollPosition / totalPixels);

        if (isPlaying) {
            stopScroll();
            startScroll();
        }
    }

    protected void startScroll() {
        float totalScrollable = getTotalScrollable();
        float progression = scrollPosition / totalScrollable;
        long timeRemainingMs = (long) (totalDurationMs * (1f - progression));

        scrollAnimator = ValueAnimator.ofFloat(scrollPosition, totalScrollable);
        scrollAnimator.setDuration(timeRemainingMs);
        scrollAnimator.setInterpolator(new LinearInterpolator());
        scrollAnimator.addUpdateListener(anim -> {
            scrollPosition = (float) anim.getAnimatedValue();
            getScrollView().scrollTo((int) scrollPosition, 0);
            updateProgress(scrollPosition / totalScrollable);
        });

        scrollAnimator.start();
        isPlaying = true;
    }

    protected void stopScroll() {
        if (scrollAnimator != null) {
            scrollAnimator.cancel();
            scrollAnimator = null;
        }
        isPlaying = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScroll();
    }
}