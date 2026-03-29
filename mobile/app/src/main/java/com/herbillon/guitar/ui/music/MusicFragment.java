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
import android.widget.ImageView;
import android.widget.TextView;

import com.herbillon.guitar.GuitarApp;
import com.herbillon.guitar.Refreshable;
import com.herbillon.guitar.databinding.FragmentMusicBinding;
import com.herbillon.guitar.model.Music;
import com.herbillon.guitar.network.GuitarAPI;
import com.herbillon.guitar.ui.musics.MusicAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MusicFragment extends Fragment implements Observer, Refreshable {

    private FragmentMusicBinding binding;
    private final HashMap<String, View> composants = new HashMap<>();
    private GuitarAPI guitarAPI;
    private Music music;
    private int musicId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMusicBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.scrollContent.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        GuitarAPI.dataSong = null;

        composants.put("iconFavorite", binding.iconFavorite);
        composants.put("textFavorite", binding.textFavorite);
        composants.put("BPM", binding.textBpm);
        composants.put("comment", binding.textComment);

        GuitarApp app = (GuitarApp) requireActivity().getApplication();
        guitarAPI = app.guitarAPI;
        guitarAPI.addObserver(this);

        musicId = (int) getArguments().get("musicId");
        Log.d("MusicFragment", "re " + getArguments().get("musicId"));
        Log.d("MusicFragment", "fetching " + musicId);
        GuitarAPI.dataSong = null;
        guitarAPI.fetchMusic(musicId);

        binding.btnTablature.setOnClickListener(v -> {
            if (music == null) return;
            Bundle args = new Bundle();
            args.putInt("musicId", musicId);
            args.putString("musicTitle", music.getTitle());
            Navigation.findNavController(v)
                    .navigate(com.herbillon.guitar.R.id.actionMusicFragToTablatureFrag, args);
        });

        return view;
    }

    private void processDatas() {
        JSONObject datas = GuitarAPI.dataSong;
        if (datas == null) return;

        Log.d("MusicFragment", "fetching " + datas);
        try {
            music = new Music(
                    musicId,
                    datas.getString("title"),
                    datas.getBoolean("favorite"),
                    datas.getInt("tempo"),
                    datas.getString("time_signature"),
                    datas.optString("comment", null)
            );

        } catch (JSONException e) {
            e.printStackTrace();
        }
        GuitarAPI.dataSong = null;
    }

    private void refreshUI() {
        if (music == null){ return; }
        binding.progressBar.setVisibility(View.GONE);
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
                    guitarAPI.patchMusicComment(musicId, newComment);
                }
            }
        });

        if (music.getFavorite()){
            ((ImageView) composants.get("iconFavorite")).setVisibility(View.VISIBLE);
            ((TextView) composants.get("textFavorite")).setVisibility(View.VISIBLE);
        }

        if (!music.getFavorite()){
            ((ImageView) composants.get("iconFavorite")).setVisibility(View.INVISIBLE);
            ((TextView) composants.get("textFavorite")).setVisibility(View.INVISIBLE);
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
        binding.progressBar.setVisibility(View.VISIBLE);
        processDatas();
        refreshUI();
    }

    @Override
    public void onRefresh() {
        guitarAPI.fetchMusic(musicId);
    }
}