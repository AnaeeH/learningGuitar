package com.herbillon.guitar.ui.music;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.google.android.material.appbar.MaterialToolbar;
import com.herbillon.guitar.GuitarApp;
import com.herbillon.guitar.MainActivity;
import com.herbillon.guitar.R;
import com.herbillon.guitar.databinding.FragmentTablatureBinding;
import com.herbillon.guitar.model.Beat;
import com.herbillon.guitar.network.GuitarAPI;

import org.json.JSONException;

public class TablatureFragment extends AbstractMusicFragment implements Observer {
    private FragmentTablatureBinding binding;
    private GuitarAPI guitarAPI;
    private int musicId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicId = getArguments().getInt("musicId");

        GuitarApp app = (GuitarApp) requireActivity().getApplication();
        guitarAPI = app.guitarAPI;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTablatureBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.horizontalScrollView.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();
        GuitarAPI.dataTablature = null;

        guitarAPI.addObserver(this);
        guitarAPI.fetchTablature(musicId);
        return view;
    }

    private void processDatas() {
        try {
            measures = MusicParser.parseMeasures(GuitarAPI.dataTablature);
            tempo = GuitarAPI.dataTablature.getInt("tempo");
            String timeSignature = GuitarAPI.dataTablature.getString("time_signature");
            isRiff = GuitarAPI.dataTablature.getBoolean("riff");
            time1 = Integer.parseInt(timeSignature.split("/")[0]);
            time2 = Integer.parseInt(timeSignature.split("/")[1]);
            GuitarAPI.dataTablature = null;

            findRepeatBounds();

            Log.d("Tablature", measures.size() + " mesures reçues");

            MaterialToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
            MenuItem tempoItem = toolbar.getMenu().findItem(R.id.action_tempo);
            if (tempoItem != null && tempoItem.getActionView() != null) {
                TextView textTempo = tempoItem.getActionView().findViewById(R.id.textTempo);
                textTempo.setText(tempo + " BPM · " + timeSignature);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean firstMeasureHasNotes() {
        if (measures == null || measures.isEmpty()) return false;
        for (Beat beat : measures.get(0).beats) {
            if (!beat.isRest && beat.string != -1) return true;
        }
        return false;
    }

    private void refreshUI() {
        if (measures == null) return;
        ((MainActivity) requireActivity()).hideLoading();
        binding.horizontalScrollView.setVisibility(View.VISIBLE);

        hasPickupMeasure = firstMeasureHasNotes();
        binding.sheetView.setHasPickupMeasure(hasPickupMeasure);
        SheetView sheetView = binding.sheetView;
        sheetView.setMusique(measures, measures.size(), time1, time2, 6);
        setupControls();
        loadYoutube("8Z0vr5nV8Io", 14300);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.findViewById(R.id.bottom_nav).setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_VISIBLE
        );

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
    }

    @Override
    public void onChanged(Object o) {
        if (GuitarAPI.dataTablature == null) return;
        binding.horizontalScrollView.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();

        processDatas();
        requireActivity().runOnUiThread(() -> {
            calculateTotalDuration();
            refreshUI();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        guitarAPI.removeObserver(this);
        binding = null;
    }

    @Override
    protected WebView getYoutubeWebView() {
        return binding.youtubeWebView;
    }

    @Override
    protected HorizontalScrollView getScrollView() {
        return binding.horizontalScrollView;
    }

    @Override
    protected SheetView getSheetView() {
        return binding.sheetView;
    }

    @Override
    protected View getBtnPlay() {
        return binding.musicControls.btnPlay;
    }

    @Override
    protected View getBtnSkipBack() {
        return binding.musicControls.btnSkipBack;
    }

    @Override
    protected View getBtnSkipForward() {
        return binding.musicControls.btnSkipForward;
    }

    @Override
    protected SeekBar getSeekBar() {
        return binding.musicControls.seekBar;
    }

    @Override
    protected TextView getTextDuration() {
        return binding.musicControls.textDuration;
    }

    @Override
    protected View getPlaybackCursor() {
        return binding.playbackCursor;
    }

    @Override
    protected View getBtnSpeedDown() {
        return binding.musicControls.btnSpeedDown;
    }

    @Override
    protected View getBtnSpeedUp() {
        return binding.musicControls.btnSpeedUp;
    }

    @Override
    protected TextView getTextSpeed() {
        return binding.musicControls.textSpeed;
    }

    @Override
    protected TextView getTextTempo() {
        MaterialToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        MenuItem tempoItem = toolbar.getMenu().findItem(R.id.action_tempo);
        return tempoItem.getActionView().findViewById(R.id.textTempo);
    }
}
