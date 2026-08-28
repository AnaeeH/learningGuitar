package com.herbillon.guitar.ui.musics;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Spinner;

import com.google.android.material.chip.ChipGroup;
import com.herbillon.guitar.GuitarApp;
import com.herbillon.guitar.MainActivity;
import com.herbillon.guitar.R;
import com.herbillon.guitar.Refreshable;
import com.herbillon.guitar.databinding.FragmentMusicsBinding;
import com.herbillon.guitar.model.Music;
import com.herbillon.guitar.network.GuitarAPI;
import com.herbillon.guitar.utils.MusicConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MusicsFragment extends Fragment implements Observer, Refreshable {
    private FragmentMusicsBinding binding;
    private GuitarAPI guitarAPI;
    private String currentSort = "recent";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMusicsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.recyclerView.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();

        GuitarApp app = (GuitarApp) requireActivity().getApplication();
        guitarAPI = app.guitarAPI;
        guitarAPI.addObserver(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        updateLastPlayedHero();

        setupSpinner(binding.spinnerStatus, MusicConstants.STATUS_VALUES);
        setupSpinner(binding.spinnerDifficulty, MusicConstants.DIFFICULTY_VALUES);
        setupSortButton();

        ChipGroup.OnCheckedStateChangeListener listener = (group, checkedIds) -> onRefresh();
        binding.chipGroupFavorite.setOnCheckedStateChangeListener(listener);
        binding.chipGroupRiff.setOnCheckedStateChangeListener(listener);

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMusics(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
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
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                onRefresh();
            }

            public void onNothingSelected(AdapterView<?> p) {
            }
        });
    }

    private String getSpinnerValue(Spinner spinner, String[][] values) {
        int pos = spinner.getSelectedItemPosition();
        return pos == 0 ? null : values[pos][0];
    }

    private void setupSortButton() {
        binding.btnSort.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            Menu menu = popup.getMenu();

            for (int i = 0; i < MusicConstants.SORT_VALUES.length; i++) {
                MenuItem item = menu.add(0, i, i, MusicConstants.SORT_VALUES[i][1]);
                item.setCheckable(true);
                if (MusicConstants.SORT_VALUES[i][0].equals(currentSort)) {
                    item.setChecked(true);
                }
            }
            menu.setGroupCheckable(0, true, true);

            popup.setOnMenuItemClickListener(item -> {
                currentSort = MusicConstants.SORT_VALUES[item.getItemId()][0];
                onRefresh();
                return true;
            });

            popup.show();
        });
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
        if (datas == null) {
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
                        music.getInt("progress"),
                        music.getBoolean("riff")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return musics;
    }

    private void updateLastPlayedHero() {
        JSONObject data = GuitarAPI.dataSongLastPlayed;
        if (data == null) {
            binding.lastPlayed.lastPlayedContainer.setVisibility(View.GONE);
            return;
        }

        try {
            int id = data.getInt("id");
            String title = data.getString("title");
            String artist = data.getString("artist");
            int progress = data.getInt("progress");

            binding.lastPlayed.lastPlayedTitle.setText(title);
            binding.lastPlayed.lastPlayedArtist.setText(artist);
            binding.lastPlayed.lastPlayedProgressBar.setProgress(progress);
            binding.lastPlayed.lastPlayedProgressText.setText(progress + "% complété");
            binding.lastPlayed.lastPlayedContainer.setVisibility(View.VISIBLE);

            binding.lastPlayed.lastPlayedContainer.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("musicId", id);
                bundle.putString("musicTitle", title);
                bundle.putString("musicArtist", artist);
                Navigation.findNavController(v)
                        .navigate(R.id.actionMusicsFragToTablatureFrag, bundle);
            });
        } catch (JSONException e) {
            e.printStackTrace();
            binding.lastPlayed.lastPlayedContainer.setVisibility(View.GONE);
        }
    }

    private void refreshUI(List<Music> musics) {
        if (musics == null) {
            return;
        }
        ((MainActivity) requireActivity()).hideLoading();
        binding.recyclerView.setVisibility(View.VISIBLE);

        int selectedIdFavorite = binding.chipGroupFavorite.getCheckedChipId();
        int selectedIdRiff = binding.chipGroupRiff.getCheckedChipId();
        boolean inFavorite = selectedIdFavorite == binding.chipFavorite.getId();
        boolean inRiff = selectedIdRiff == binding.chipRiff.getId();

        MusicAdapter adapter = new MusicAdapter(
                musics,
                (music) -> guitarAPI.patchMusicFavorite(currentSort, music.getId(), music.getFavorite(), inFavorite, inRiff),
                (music) -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("musicId", music.getId());
                    bundle.putString("musicTitle", music.getTitle());
                    bundle.putString("musicArtist", music.getArtist());
                    NavHostFragment.findNavController(this).navigate(com.herbillon.guitar.R.id.actionMusicsFragToMusicFrag, bundle);
                });

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
        updateLastPlayedHero();
        binding.recyclerView.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();

        String query = binding.searchInput.getText().toString();
        filterMusics(query);
    }

    @Override
    public void onRefresh() {
        binding.recyclerView.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();

        int selectedIdFavorite = binding.chipGroupFavorite.getCheckedChipId();
        int selectedIdRiff = binding.chipGroupRiff.getCheckedChipId();
        boolean favorite = selectedIdFavorite == binding.chipFavorite.getId();
        boolean riff = selectedIdRiff == binding.chipRiff.getId();
        String status = getSpinnerValue(binding.spinnerStatus, MusicConstants.STATUS_VALUES);
        String difficulty = getSpinnerValue(binding.spinnerDifficulty, MusicConstants.DIFFICULTY_VALUES);

        guitarAPI.fetchMusics(currentSort, favorite, riff, status, difficulty);
    }

    @Override
    public void onResume() {
        super.onResume();
        guitarAPI.fetchMusicLastPlayed();
    }
}