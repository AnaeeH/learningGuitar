package com.herbillon.guitar.ui.musics;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.herbillon.guitar.GuitarApp;
import com.herbillon.guitar.Refreshable;
import com.herbillon.guitar.databinding.FragmentMusicsBinding;
import com.herbillon.guitar.network.GuitarAPI;

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

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        guitarAPI.removeObserver(this);
        binding = null;
    }

    @Override
    public void onChanged(Object o) {
        //TO DO
    }

    @Override
    public void onRefresh() {
        guitarAPI.fetchMusics();
    }
}