package com.herbillon.guitar.ui.music;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.herbillon.guitar.GuitarApp;
import com.herbillon.guitar.MainActivity;
import com.herbillon.guitar.R;
import com.herbillon.guitar.Refreshable;
import com.herbillon.guitar.databinding.FragmentMusicBinding;
import com.herbillon.guitar.model.Music;
import com.herbillon.guitar.network.GuitarAPI;
import com.herbillon.guitar.ui.musics.MusicAdapter;
import com.herbillon.guitar.utils.MusicConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MusicFragment extends Fragment implements Observer, Refreshable {

    private FragmentMusicBinding binding;
    private final HashMap<String, View> composants = new HashMap<>();
    private GuitarAPI guitarAPI;
    private Music music;
    private int musicId;
    private String musicChords;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMusicBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.scrollContent.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();
        GuitarAPI.dataSong = null;

        composants.put("iconFavorite", binding.iconFavorite);
        composants.put("textFavorite", binding.textFavorite);
        composants.put("iconRiff", binding.iconRiff);
        composants.put("textRiff", binding.textRiff);
        composants.put("BPM", binding.textBpm);
        composants.put("comment", binding.textComment);
        composants.put("titleChords", binding.titleChords);
        composants.put("textChords", binding.textChords);

        GuitarApp app = (GuitarApp) requireActivity().getApplication();
        guitarAPI = app.guitarAPI;
        guitarAPI.addObserver(this);

        musicId = (int) getArguments().get("musicId");
        Log.d("MusicFragment", "re " + getArguments().get("musicId"));
        Log.d("MusicFragment", "fetching " + musicId);
        GuitarAPI.dataSong = null;
        guitarAPI.fetchMusic(musicId);
        guitarAPI.fetchMusicChords(musicId);

        binding.btnTablature.setOnClickListener(v -> {
            if (music == null) return;
            Bundle args = new Bundle();
            args.putInt("musicId", musicId);
            args.putString("musicTitle", music.getTitle());
            args.putString("musicArtist", music.getArtist());
            Navigation.findNavController(v)
                    .navigate(com.herbillon.guitar.R.id.actionMusicFragToTablatureFrag, args);
        });
        return view;
    }

    private void setupChips(ChipGroup chipGroup, String[][] values, String currentValue) {

        Map<Integer, String> mapping = new HashMap<>();
        for (String[] entry : values) {
            if (!entry[0].isEmpty()){
                Chip chip = new Chip(requireContext());
                chip.setText(entry[1]);
                chip.setTag(entry[0]);
                chip.setCheckable(true);
                chip.setId(View.generateViewId());
                chipGroup.addView(chip);
                mapping.put(chip.getId(), entry[0]);

                if (entry[0].equals(currentValue)) {
                    chip.setChecked(true);
                }
            }
        }

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            Chip selectedChip = chipGroup.findViewById(checkedIds.get(0));
            if (selectedChip == null) return;

            String selectedValue = (String) selectedChip.getTag();
            if (chipGroup == binding.chipGroupStatus) {
                guitarAPI.patchMusic(musicId, "status", selectedValue);
            } else {
                guitarAPI.patchMusic(musicId, "difficulty", selectedValue);
            }
        });
    }

    private void processDatas() {
        JSONObject datas = GuitarAPI.dataSong;
        JSONArray datasChords = GuitarAPI.dataSongChords;
        if (datas == null) return;

        Log.d("MusicFragment", "fetching " + datas);
        try {
            music = new Music(
                    musicId,
                    datas.getString("title"),
                    datas.getString("artist"),
                    datas.getBoolean("favorite"),
                    datas.getString("difficulty"),
                    datas.getInt("progress"),
                    datas.getInt("tempo"),
                    datas.getString("time_signature"),
                    datas.optString("comment", null),
                    datas.getBoolean("riff")
            );

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (datasChords == null) return;
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < datasChords.length(); i++) {
                sb.append(datasChords.getString(i) + " ");
            }
            musicChords = sb.toString();
            Log.d("MusicFragment", "fetching " + musicChords);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GuitarAPI.dataSong = null;
        GuitarAPI.dataSongChords = null;
    }

    private void refreshUI() {
        if (music == null){ return; }
        ((MainActivity) requireActivity()).hideLoading();
        binding.scrollContent.setVisibility(View.VISIBLE);

        ((TextView) composants.get("BPM")).setText(music.getTempo() + " BPM · " + music.getTimeSignature());
        if (music.getComment() != null){
            ((TextView) composants.get("comment")).setText("" + music.getComment());
        } else {
            ((TextView) composants.get("comment")).setText("");
        }

        binding.textComment.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String newComment = binding.textComment.getText().toString().trim();
                if (!newComment.equals(music.getComment())) {
                    guitarAPI.patchMusic(musicId, "comment", newComment);
                }
            }
        });

        if (music.getFavorite()){
            ((ImageView) composants.get("iconFavorite")).setVisibility(View.VISIBLE);
            ((TextView) composants.get("textFavorite")).setVisibility(View.VISIBLE);
        } else {
            ((ImageView) composants.get("iconFavorite")).setVisibility(View.INVISIBLE);
            ((TextView) composants.get("textFavorite")).setVisibility(View.INVISIBLE);
        }

        if (music.getRiff()){
            ((ImageView) composants.get("iconRiff")).setVisibility(View.VISIBLE);
            ((TextView) composants.get("textRiff")).setVisibility(View.VISIBLE);
        } else {
            ((ImageView) composants.get("iconRiff")).setVisibility(View.INVISIBLE);
            ((TextView) composants.get("textRiff")).setVisibility(View.INVISIBLE);
        }

        if (musicChords == null || musicChords.isEmpty()){
            ((TextView) composants.get("titleChords")).setVisibility(View.GONE);
            ((TextView) composants.get("textChords")).setVisibility(View.GONE);
        } else {
            ((TextView) composants.get("titleChords")).setVisibility(View.VISIBLE);
            ((TextView) composants.get("textChords")).setVisibility(View.VISIBLE);
            ((TextView) composants.get("textChords")).setText(musicChords);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        guitarAPI.removeObserver(this);
        binding = null;
    }

    @Override
    public void onChanged(Object o) {
        Log.d("MusicFragment", "onChanged called, dataSong=" + GuitarAPI.dataSong);
        Log.d("MusicFragment", "onChanged called, dataTablature=" + GuitarAPI.dataTablature);

        if (GuitarAPI.dataSong == null) return;
        binding.scrollContent.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();
        processDatas();
        refreshUI();

        binding.chipGroupStatus.setOnCheckedStateChangeListener(null);
        binding.chipGroupDifficulty.setOnCheckedStateChangeListener(null);
        binding.chipGroupStatus.removeAllViews();
        binding.chipGroupDifficulty.removeAllViews();
//        setupChips(binding.chipGroupStatus, MusicConstants.STATUS_VALUES, music.getStatus());
        setupChips(binding.chipGroupDifficulty, MusicConstants.DIFFICULTY_VALUES, music.getDifficulty());
    }

    @Override
    public void onRefresh() {
        binding.scrollContent.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();
        guitarAPI.fetchMusic(musicId);
        guitarAPI.fetchMusicChords(musicId);
    }
}