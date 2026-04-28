package com.herbillon.guitar.ui.musics;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.herbillon.guitar.GuitarApp;
import com.herbillon.guitar.R;
import com.herbillon.guitar.Refreshable;
import com.herbillon.guitar.databinding.FragmentMusicsBinding;
import com.herbillon.guitar.model.Chord;
import com.herbillon.guitar.model.Music;
import com.herbillon.guitar.network.GuitarAPI;
import com.herbillon.guitar.ui.chords.ChordAdapter;
import com.herbillon.guitar.utils.MusicConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicsFragment extends Fragment implements Observer, Refreshable {
    private FragmentMusicsBinding binding;
    private GuitarAPI guitarAPI;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMusicsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        GuitarApp app = (GuitarApp) requireActivity().getApplication();
        guitarAPI = app.guitarAPI;
        guitarAPI.addObserver(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupSpinner(binding.spinnerStatus, MusicConstants.STATUS_VALUES);
        setupSpinner(binding.spinnerDifficulty, MusicConstants.DIFFICULTY_VALUES);

        ChipGroup.OnCheckedStateChangeListener listener = (group, checkedIds) -> onRefresh();
        binding.chipGroupFavorite.setOnCheckedStateChangeListener(listener);
        binding.chipGroupRiff.setOnCheckedStateChangeListener(listener);

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMusics(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void setupSpinner(Spinner spinner, String[][] values) {
        List<String> labels = new ArrayList<>();
        for (String[] entry : values) labels.add(entry[1]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_chip_selected,
                labels
        );
        adapter.setDropDownViewResource(R.layout.spinner_chip_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { onRefresh(); }
            public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private String getSpinnerValue(Spinner spinner, String[][] values) {
        int pos = spinner.getSelectedItemPosition();
        return pos == 0 ? null : values[pos][0];
    }

    private void filterMusics(String query) {
        List<Music> allMusics = processDatas();
        if (query.isEmpty()) {
            refreshUI(allMusics);
        } else {
            List<Music> filtered = new ArrayList<>();
            for (Music music : allMusics) {
                if (music.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(music);
                } else if (music.getArtist().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(music);
                }
            }
            refreshUI(filtered);
        }
    }

    private List<Music> processDatas() {
        JSONArray datas = GuitarAPI.dataSongs;
        if (datas == null){
            return null;
        }
        List<Music> musics = new ArrayList<>();
        try {
            for (int i = 0; i < datas.length(); i++) {
                JSONObject music = datas.getJSONObject(i);
                musics.add(new Music(
                        music.getInt("id"),
                        music.getString("title"),
                        music.getString("artist"),
                        music.getBoolean("favorite"),
                        music.getString("difficulty"),
                        music.getString("status")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return musics;
    }

    private void refreshUI(List<Music> musics) {
        if (musics == null){ return; }

        int selectedIdFavorite = binding.chipGroupFavorite.getCheckedChipId();
        int selectedIdRiff = binding.chipGroupRiff.getCheckedChipId();
        boolean inFavorite = selectedIdFavorite == binding.chipFavorite.getId();
        boolean inRiff = selectedIdRiff == binding.chipRiff.getId();
        MusicAdapter adapter = new MusicAdapter(
                musics,
                (music) -> guitarAPI.patchMusicFavorite(music.getId(), music.getFavorite(), inFavorite, inRiff),
                (music)  -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("musicId", music.getId());
                    bundle.putString("musicTitle", music.getTitle());
                    bundle.putString("musicArtist", music.getArtist());
                    NavHostFragment.findNavController(this).navigate(com.herbillon.guitar.R.id.actionMusicsFragToMusicFrag, bundle);
                } );

        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        guitarAPI.removeObserver(this);
        binding = null;
    }

    @Override
    public void onChanged(Object o) {
        String query = binding.searchInput.getText().toString();
        filterMusics(query);
    }

    @Override
    public void onRefresh() {
        int selectedIdFavorite = binding.chipGroupFavorite.getCheckedChipId();
        int selectedIdRiff = binding.chipGroupRiff.getCheckedChipId();
        boolean favorite = selectedIdFavorite == binding.chipFavorite.getId();
        boolean riff = selectedIdRiff == binding.chipRiff.getId();
        String status = getSpinnerValue(binding.spinnerStatus, MusicConstants.STATUS_VALUES);
        String difficulty = getSpinnerValue(binding.spinnerDifficulty, MusicConstants.DIFFICULTY_VALUES);

        guitarAPI.fetchMusics(favorite, riff, status, difficulty);
    }
}