package com.herbillon.guitar.ui.music;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.herbillon.guitar.model.Measure;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMusicFragment extends Fragment {

    // Music
    protected int currentTempo = -1;
    protected int tempo;
    protected int time1;
    protected int time2;
    protected String videoId = null;
    protected int startMs = -1;
    protected List<Measure> measures;
    protected boolean hasPickupMeasure;

    // Riff
    protected boolean isRiff = false;
    private int repeatStartIndex = -1;
    private int repeatEndIndex = -1;
    private boolean repeatAppended = false;

    // Animation
    private ValueAnimator scrollAnimator;
    private float scrollPosition = 0f;
    private float cursorOffsetPx = 0f;
    private boolean isPlaying = false;
    private boolean isStopping = false;
    private long totalDurationMs = 0;
    private String totalDurationFormatted = "0:00";

    // Getter
    protected abstract WebView getYoutubeWebView();
    protected abstract HorizontalScrollView getScrollView();
    protected abstract SheetView getSheetView();
    protected abstract View getBtnPlay();
    protected abstract View getBtnSkipBack();
    protected abstract View getBtnSkipForward();
    protected abstract SeekBar getSeekBar();
    protected abstract TextView getTextDuration();
    protected abstract View getPlaybackCursor();
    protected abstract View getBtnSpeedDown();
    protected abstract View getBtnSpeedUp();
    protected abstract TextView getTextSpeed();
    protected abstract TextView getTextTempo();

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
        float secondsPerMeasure = time1 * (60f / currentTempo);
        int total = measures.size() + (hasPickupMeasure ? 1 : 0);
        int totalSeconds = (int) (secondsPerMeasure * total);
        totalDurationMs = totalSeconds * 1000L;
        totalDurationFormatted = formatTime(totalSeconds);
    }

    protected void setupControls() {
        getSeekBar().setMax(100);
        updateProgress(0f);
        currentTempo = tempo;
        setupYoutube();

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
                // Restart from the beginning
                if (!isRiff && scrollPosition >= getTotalScrollable()) {
                    scrollPosition = 0f;
                    getScrollView().scrollTo(0, 0);
                    updateProgress(0f);
                }
                startScroll();
                ((Button) getBtnPlay()).setText("⏸");
            }
        });

        getBtnSkipBack().setOnClickListener(v -> skipSeconds(-10));
        getBtnSkipForward().setOnClickListener(v -> skipSeconds(10));

        getBtnSpeedDown().setOnClickListener(v -> changeTempo(-5));
        getBtnSpeedUp().setOnClickListener(v -> changeTempo(5));

        getSeekBar().setOnSeekBarChangeListener(buildSeekBarListener());
    }

    private void setupYoutube() {
        WebSettings settings = getYoutubeWebView().getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        getYoutubeWebView().setWebChromeClient(new WebChromeClient());
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

    private void changeTempo(int bpm) {
        currentTempo += bpm;
        getTextTempo().setText(currentTempo + " BPM · " + time1 + "/" + time2);
        getTextSpeed().setText(Math.round((double) currentTempo / tempo * 100) + "%");

        calculateTotalDuration();
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

        // Riff
        scrollAnimator.addUpdateListener(anim -> {
            scrollPosition = (float) anim.getAnimatedValue();
            getScrollView().scrollTo((int) scrollPosition, 0);
            updateProgress(scrollPosition / getTotalScrollable());

            if (isRiff && !repeatAppended) {
                float lastMeasureStart = (measures.size() - 1) * getSheetView().getMeasureWidth();
                if (scrollPosition >= lastMeasureStart) {
                    repeatAppended = true;
                    appendRepeatMeasures();
                }
            }
        });
        scrollAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isStopping) {
                    isStopping = false;
                    return;
                }
                if (isPlaying && isRiff) {
                    appendRepeatMeasures();
                    startScroll();
                } else {
                    isPlaying = false;
                    ((Button) getBtnPlay()).setText("▶");
                }
            }
        });

        scrollAnimator.start();
        isPlaying = true;
        playYoutube();

//        long measureDurationMs = (long) (time1 * (60.0 / currentTempo) * 1000);
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            if (isPlaying) playYoutube();
//        }, measureDurationMs);
    }

    protected void stopScroll() {
        if (scrollAnimator != null) {
            isStopping = true;
            scrollAnimator.cancel();
            scrollAnimator = null;
        }
        isPlaying = false;
        pauseYoutube();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScroll();
    }

    protected void loadYoutube() {
        double startExact = startMs / 1000.0;

        String html = "<html>"
                + "<head><meta name='referrer' content='strict-origin'></head>"
                + "<body><iframe id='yt' src='https://www.youtube.com/embed/" + videoId
                + "?autoplay=0"
                + "&controls=1&playsinline=1&enablejsapi=1'>"
                + "</iframe>"
                + "</body></html>";

        getYoutubeWebView().loadDataWithBaseURL("https://com.herbillon.guitar",
                html, "text/html", "utf-8", null);
    }

    private void playYoutube() {
        double currentYtTime = startMs / 1000.0 + (scrollPosition / getTotalScrollable()) * (totalDurationMs / 1000.0);

        getYoutubeWebView().evaluateJavascript(
                "var f = document.querySelector('iframe');" +
                        "f.contentWindow.postMessage('{\"event\":\"command\",\"func\":\"seekTo\",\"args\":[" + currentYtTime + ", true]}', '*');" +
                        "f.contentWindow.postMessage('{\"event\":\"command\",\"func\":\"playVideo\",\"args\":\"\"}', '*');",
                null);
    }

    private void pauseYoutube() {
        getYoutubeWebView().evaluateJavascript(
                "document.querySelector('iframe').contentWindow.postMessage(" +
                        "'{\"event\":\"command\",\"func\":\"pauseVideo\",\"args\":\"\"}', '*');", null);
    }


    // Riff
    protected void findRepeatBounds() {
        for (int i = 0; i < measures.size(); i++) {
            if (measures.get(i).repeatStart) repeatStartIndex = i;
            if (measures.get(i).repeatEnd) repeatEndIndex = i;
        }
    }

    private void appendRepeatMeasures() {
        if (repeatStartIndex == -1 || repeatEndIndex == -1) return;
        List<Measure> toAdd = measures.subList(repeatStartIndex, repeatEndIndex + 1);
        measures.addAll(new ArrayList<>(toAdd));
        getSheetView().setMeasures(measures);
        getSheetView().invalidate();
        calculateTotalDuration();
        repeatAppended = false;
    }

}