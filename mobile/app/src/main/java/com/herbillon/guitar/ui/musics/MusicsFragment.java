package com.herbillon.guitar.ui.musics;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.herbillon.guitar.GuitarApp;
import com.herbillon.guitar.Refreshable;
import com.herbillon.guitar.databinding.FragmentMusicsBinding;
import com.herbillon.guitar.model.Chord;
import com.herbillon.guitar.model.Music;
import com.herbillon.guitar.network.GuitarAPI;
import com.herbillon.guitar.ui.chords.ChordAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            onRefresh();
        });

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

    private void filterMusics(String query) {
        List<Music> allMusics = processDatas();
        if (query.isEmpty()) {
            refreshUI(allMusics);
        } else {
            List<Music> filtered = new ArrayList<>();
            for (Music music : allMusics) {
                if (music.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(music);
                }
            }
            refreshUI(filtered);
        }
    }

    private List<Music> processDatas() {
        JSONArray datas = GuitarAPI.dataSongs;
        List<Music> musics = new ArrayList<>();
        try {
            for (int i = 0; i < datas.length(); i++) {
                JSONObject music = datas.getJSONObject(i);
                musics.add(new Music(
                        music.getInt("id"),
                        music.getString("title"),
                        music.getBoolean("favorite")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return musics;
    }

    private void refreshUI(List<Music> musics) {
        MusicAdapter adapter = new MusicAdapter(musics);
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
        int selectedId = binding.chipGroup.getCheckedChipId();
        guitarAPI.fetchMusics(selectedId == binding.chipFavorite.getId());
    }
}