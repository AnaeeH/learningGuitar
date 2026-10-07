package com.herbillon.guitar.ui.chords;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.herbillon.guitar.GuitarApp;
import com.herbillon.guitar.MainActivity;
import com.herbillon.guitar.R;
import com.herbillon.guitar.Refreshable;
import com.herbillon.guitar.databinding.FragmentChordsBinding;
import com.herbillon.guitar.model.Chord;
import com.herbillon.guitar.model.ChordPosition;
import com.herbillon.guitar.network.GuitarAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChordsFragment extends Fragment implements Observer, Refreshable {
    private FragmentChordsBinding binding;
    private GuitarAPI guitarAPI;

    private List<Chord> allChords = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChordsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.recyclerView.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();

        GuitarApp app = (GuitarApp) requireActivity().getApplication();
        guitarAPI = app.guitarAPI;
        guitarAPI.addObserver(this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            applyFilters();
        });

        EditText searchInput = view.findViewById(R.id.searchInput);
        searchInput.setHint(R.string.search_chord);
        binding.searchBar.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (GuitarAPI.dataChords == null) {
            guitarAPI.fetchChords();
        }
        return view;
    }

    private void processDatas() {
        JSONArray datas = GuitarAPI.dataChords;
        if (datas == null) return;

        allChords = new ArrayList<>();
        try {
            for (int i = 0; i < datas.length(); i++) {
                JSONObject chordJson = datas.getJSONObject(i);
                JSONObject note = chordJson.getJSONObject("note");

                boolean isMajor = chordJson.getBoolean("isMajor");
                String label = note.getString("label") + (isMajor ? " majeur" : " mineur");
                label = label.substring(0, 1).toUpperCase() + label.substring(1);

                Chord chord = new Chord(
                        chordJson.getInt("id"),
                        chordJson.getString("name"),
                        label,
                        isMajor
                );

                if (!chordJson.isNull("barreFret")) {
                    chord.setBarreFret(chordJson.getInt("barreFret"));
                    chord.setBarreFromString(chordJson.getInt("barreFromString"));
                    chord.setBarreToString(chordJson.getInt("barreToString"));
                }

                if (!chordJson.isNull("mutedStrings")) {
                    JSONArray mutedArray = chordJson.getJSONArray("mutedStrings");
                    List<Integer> muted = new ArrayList<>();
                    for (int j = 0; j < mutedArray.length(); j++) {
                        muted.add(mutedArray.getInt(j));
                    }
                    chord.setMutedStrings(muted);
                }

                JSONArray positionsArray = chordJson.getJSONArray("positions");
                List<ChordPosition> positions = new ArrayList<>();
                for (int j = 0; j < positionsArray.length(); j++) {
                    JSONObject pos = positionsArray.getJSONObject(j);
                    positions.add(new ChordPosition(
                            pos.getInt("string"),
                            pos.getInt("fret")
                    ));
                }
                chord.setPositions(positions);

                allChords.add(chord);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        if (allChords.isEmpty()) return;

        int selectedId = binding.chipGroup.getCheckedChipId();
        boolean onlyMajor = selectedId == binding.chipMajor.getId();
        boolean onlyMinor = selectedId == binding.chipMinor.getId();

        List<Chord> result = new ArrayList<>();
        for (Chord chord : allChords) {
            if (onlyMajor && !chord.getIsMajor()) continue;
            if (onlyMinor && chord.getIsMajor()) continue;
            result.add(chord);
        }

        String query = binding.searchBar.searchInput.getText().toString().toLowerCase().trim();
        if (!query.isEmpty()) {
            List<Chord> searchFiltered = new ArrayList<>();
            for (Chord chord : result) {
                if (chord.getName().toLowerCase().contains(query) ||
                        chord.getLabel().toLowerCase().contains(query)) {
                    searchFiltered.add(chord);
                }
            }
            result = searchFiltered;
        }

        refreshUI(result);
    }

    private void refreshUI(List<Chord> chords) {
        ((MainActivity) requireActivity()).hideLoading();
        binding.recyclerView.setVisibility(View.VISIBLE);

        ChordAdapter adapter = new ChordAdapter(chords);
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
        processDatas();
        applyFilters();
    }

    @Override
    public void onRefresh() {
        binding.recyclerView.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();
        guitarAPI.fetchChords();
    }
}