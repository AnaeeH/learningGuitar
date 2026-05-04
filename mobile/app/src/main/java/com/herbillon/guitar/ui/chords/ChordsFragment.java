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

import com.herbillon.guitar.GuitarApp;
import com.herbillon.guitar.MainActivity;
import com.herbillon.guitar.Refreshable;
import com.herbillon.guitar.databinding.FragmentChordsBinding;
import com.herbillon.guitar.model.Chord;
import com.herbillon.guitar.network.GuitarAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChordsFragment extends Fragment implements Observer, Refreshable {
    private FragmentChordsBinding binding;
    private GuitarAPI guitarAPI;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChordsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.recyclerView.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();

        GuitarApp app = (GuitarApp) requireActivity().getApplication();
        guitarAPI = app.guitarAPI;
        guitarAPI.addObserver(this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            onRefresh();
        });

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChords(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        return view;
    }

    private void filterChords(String query) {
        List<Chord> allChords = processDatas();
        if (query.isEmpty()) {
            refreshUI(allChords);
        } else {
            List<Chord> filtered = new ArrayList<>();
            for (Chord chord : allChords) {
                if (chord.getName().toLowerCase().startsWith(query.toLowerCase()) ||
                        chord.getLabel().toLowerCase().startsWith(query.toLowerCase())) {
                    filtered.add(chord);
                }
            }
            refreshUI(filtered);
        }
    }

    private List<Chord> processDatas() {
        JSONArray datas = GuitarAPI.dataChords;
        if (datas == null){ return null;}
        List<Chord> chords = new ArrayList<>();
        try {
            for (int i = 0; i < datas.length(); i++) {
                JSONObject chord = datas.getJSONObject(i);
                JSONObject note = chord.getJSONObject("note");
                String label = note.getString("label") + (chord.getBoolean("isMajor") ? " majeur" : " mineur");
                chords.add(new Chord(
                        chord.getInt("id"),
                        chord.getString("name"),
                        label.substring(0, 1).toUpperCase() + label.substring(1),
                        chord.getString("diagram")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chords;
    }

    private void refreshUI(List<Chord> chords) {
        if (chords == null){ return; }
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
        binding.recyclerView.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();

        String query = binding.searchInput.getText().toString();
        filterChords(query);
    }

    @Override
    public void onRefresh() {
        binding.recyclerView.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();
        int selectedId = binding.chipGroup.getCheckedChipId();
        String filter = "";
        if (selectedId == binding.chipMajor.getId()) {
            filter = "major";
        } else if (selectedId == binding.chipMinor.getId()) {
            filter = "minor";
        }
        guitarAPI.fetchChords(filter);
    }
}