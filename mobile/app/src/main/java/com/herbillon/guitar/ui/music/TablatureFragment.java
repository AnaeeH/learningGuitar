package com.herbillon.guitar.ui.music;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.google.android.material.appbar.MaterialToolbar;
import com.herbillon.guitar.GuitarApp;
import com.herbillon.guitar.R;
import com.herbillon.guitar.databinding.FragmentTablatureBinding;
import com.herbillon.guitar.model.Measure;
import com.herbillon.guitar.network.GuitarAPI;

import org.json.JSONException;

import java.util.List;

public class TablatureFragment extends Fragment implements Observer {
    private FragmentTablatureBinding binding;
    private GuitarAPI guitarAPI;
    private int musicId;
    private List<Measure> measures;

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

        GuitarAPI.dataTablature = null;

        guitarAPI.addObserver(this);
        guitarAPI.fetchTablature(musicId);
        return view;
    }

    private void processDatas() {
        try {
            measures = MusicParser.parseMeasures(GuitarAPI.dataTablature);
            int tempo = GuitarAPI.dataTablature.getInt("tempo");
            String timeSignature = GuitarAPI.dataTablature.getString("time_signature");
            GuitarAPI.dataTablature = null;

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

    private void refreshUI() {
        if (measures == null) return;
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
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
        processDatas();
        refreshUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        guitarAPI.removeObserver(this);
        binding = null;
    }
}
