package com.herbillon.guitar.ui.musics;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

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
    private ListPopupWindow sortPopup;
    private boolean sortPopupVisible = false;

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


        binding.searchBar.searchInput.setHint(R.string.search_music);
        binding.searchBar.searchInput.addTextChangedListener(new TextWatcher() {
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
                R.id.spinnerText,
                labels
        ) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;
                    boolean isActive = spinner.getSelectedItemPosition() == position;
                    styleDropdownItem(tv, isActive);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.spinner_chip_item);
        spinner.setAdapter(adapter);
        int offsetPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, -16, getResources().getDisplayMetrics()
        );
        spinner.setDropDownHorizontalOffset(offsetPx);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                int colorRes = (pos == 0) ? R.color.app_surface : R.color.app_accent;
                ColorStateList tint = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), colorRes)
                );
                spinner.setBackgroundTintList(tint);

                if (v != null) {
                    int textColorRes = (pos == 0) ? R.color.app_text_secondary : R.color.app_text_primary;
                    int textColor = ContextCompat.getColor(requireContext(), textColorRes);
                    TextView tv = v.findViewById(R.id.spinnerText);
                    ImageView arrow = v.findViewById(R.id.spinnerArrow);

                    if (tv != null) tv.setTextColor(textColor);
                    if (arrow != null) arrow.setImageTintList(ColorStateList.valueOf(textColor));
                }
                onRefresh();
            }

            public void onNothingSelected(AdapterView<?> p) {
            }
        });
    }

    private String getSpinnerValue(Spinner spinner, String[][] values) {
        int pos = spinner.getSelectedItemPosition();
        return pos <= 0 ? null : values[pos][0];
    }

    private void setupSortButton() {
        binding.btnSort.setOnClickListener(v -> {
            if (sortPopupVisible) {
                sortPopupVisible = false;
                sortPopup.dismiss();
                return;
            }

            sortPopup = new ListPopupWindow(requireContext());

            sortPopup.setAnchorView(v);
            sortPopup.setBackgroundDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_card)
            );
            int verticalOffset = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()
            );
            sortPopup.setVerticalOffset(verticalOffset);
            int contentWidth = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 160, getResources().getDisplayMetrics()
            );
            sortPopup.setContentWidth(contentWidth);

            List<String> labels = new ArrayList<>();
            for (String[] sortValue : MusicConstants.SORT_VALUES) {
                labels.add(sortValue[1]);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    requireContext(),
                    R.layout.spinner_chip_item,
                    R.id.spinnerText,
                    labels
            ) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView tv = view.findViewById(R.id.spinnerText);
                    boolean isActive = MusicConstants.SORT_VALUES[position][0].equals(currentSort);
                    styleDropdownItem(tv, isActive);
                    return view;
                }
            };
            sortPopup.setAdapter(adapter);

            sortPopup.setOnItemClickListener((parent, view, position, id) -> {
                currentSort = MusicConstants.SORT_VALUES[position][0];
                sortPopup.dismiss();
                onRefresh();
            });

            sortPopupVisible = true;
            sortPopup.show();
        });
    }

    private void styleDropdownItem(TextView tv, boolean isActive) {
        int colorRes = isActive ? R.color.app_accent : R.color.app_text_primary;
        tv.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        tv.setTypeface(null, isActive ? Typeface.BOLD : Typeface.NORMAL);
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

        binding.textMusicCount.setText(
                getResources().getQuantityString(R.plurals.music_count, musics.size(), musics.size()));

        int selectedIdFavorite = binding.chipGroupFavorite.getCheckedChipId();
        int selectedIdRiff = binding.chipGroupRiff.getCheckedChipId();
        boolean inFavorite = selectedIdFavorite == binding.chipFavorite.getId();
        boolean inRiff = selectedIdRiff == binding.chipRiff.getId();
        String status = getSpinnerValue(binding.spinnerStatus, MusicConstants.STATUS_VALUES);
        String difficulty = getSpinnerValue(binding.spinnerDifficulty, MusicConstants.DIFFICULTY_VALUES);

        MusicAdapter adapter = new MusicAdapter(
                musics,
                (music) -> guitarAPI.patchMusicFavorite(currentSort, music.getId(), music.getFavorite(), inFavorite, inRiff, status, difficulty),
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

        String query = binding.searchBar.searchInput.getText().toString();
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