package com.herbillon.guitar.ui.scales;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;

import com.herbillon.guitar.GuitarApp;
import com.herbillon.guitar.MainActivity;
import com.herbillon.guitar.R;
import com.herbillon.guitar.Refreshable;
import com.herbillon.guitar.databinding.FragmentScalesBinding;
import com.herbillon.guitar.model.Scale;
import com.herbillon.guitar.model.ScaleNote;
import com.herbillon.guitar.model.ScalePosition;
import com.herbillon.guitar.network.GuitarAPI;
import com.herbillon.guitar.ui.scales.ScaleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScalesFragment extends Fragment implements Observer, Refreshable {
    private FragmentScalesBinding binding;
    private GuitarAPI guitarAPI;

    private static final int SPAN_COUNT = 3;
    private GridLayoutManager layoutManager;

    private List<Scale> allScales = new ArrayList<>();
    private final Set<Integer> collapsedScaleIds = new HashSet<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScalesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.recyclerView.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).showLoading();

        GuitarApp app = (GuitarApp) requireActivity().getApplication();
        guitarAPI = app.guitarAPI;

        layoutManager = new GridLayoutManager(getContext(), SPAN_COUNT);
        binding.recyclerView.setLayoutManager(layoutManager);

        EditText searchInput = view.findViewById(R.id.searchInput);
        searchInput.setHint(R.string.search_scale);
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
        guitarAPI.addObserver(this);

        if (GuitarAPI.dataScales == null) {
            guitarAPI.fetchScales();
        }
        return view;
    }

    private void processDatas() {
        JSONArray datas = GuitarAPI.dataScales;
        if (datas == null) return;

        allScales = new ArrayList<>();
        try {
            for (int i = 0; i < datas.length(); i++) {
                JSONObject scaleJson = datas.getJSONObject(i);

                Scale scale = new Scale(
                        scaleJson.getInt("id"),
                        scaleJson.getString("name"),
                        scaleJson.getString("type")
                );

                JSONArray positionsArray = scaleJson.getJSONArray("positions");
                List<ScalePosition> positions = new ArrayList<>();
                for (int j = 0; j < positionsArray.length(); j++) {
                    JSONObject posJson = positionsArray.getJSONObject(j);

                    ScalePosition position = new ScalePosition(
                            posJson.getInt("id"),
                            posJson.getInt("positionNumber"),
                            posJson.getInt("startFret")
                    );

                    JSONArray notesArray = posJson.getJSONArray("notes");
                    List<ScaleNote> notes = new ArrayList<>();
                    for (int k = 0; k < notesArray.length(); k++) {
                        JSONObject noteJson = notesArray.getJSONObject(k);

                        ScaleNote note = new ScaleNote(
                                noteJson.getInt("id"),
                                noteJson.getInt("string"),
                                noteJson.getInt("fret"),
                                noteJson.getBoolean("isRoot")
                        );

                        notes.add(note);
                    }
                    position.setNotes(notes);

                    positions.add(position);
                }
                scale.setPositions(positions);

                allScales.add(scale);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        if (allScales.isEmpty()) return;

        String query = binding.searchBar.searchInput.getText().toString().toLowerCase().trim();
        List<Object> items = new ArrayList<>();
        for (Scale scale : allScales) {
            boolean matches = query.isEmpty()
                    || scale.getName().toLowerCase().contains(query)
                    || scale.getType().toLowerCase().contains(query)
                    || (scale.getRootLabel() != null && scale.getRootLabel().toLowerCase().contains(query));

            if (matches) {
                items.add(scale);
                if (scale.getPositions() != null && !collapsedScaleIds.contains(scale.getId())) {
                    items.addAll(scale.getPositions());
                }
            }
        }
        refreshUI(items);
    }

    private void refreshUI(List<Object> scales) {
        ((MainActivity) requireActivity()).hideLoading();
        binding.recyclerView.setVisibility(View.VISIBLE);

        ScaleAdapter adapter = new ScaleAdapter(scales, collapsedScaleIds, this::onScaleHeaderClick);
        binding.recyclerView.setAdapter(adapter);
        layoutManager.setSpanSizeLookup(adapter.getSpanSizeLookup(SPAN_COUNT));
    }

    private void onScaleHeaderClick(Scale scale) {
        int id = scale.getId();
        if (collapsedScaleIds.contains(id)) {
            collapsedScaleIds.remove(id);
        } else {
            collapsedScaleIds.add(id);
        }
        applyFilters();
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
        guitarAPI.fetchScales();
    }
}







