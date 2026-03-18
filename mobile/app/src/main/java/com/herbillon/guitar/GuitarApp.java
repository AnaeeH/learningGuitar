package com.herbillon.guitar;

import android.app.Application;

import com.herbillon.guitar.network.GuitarAPI;

public class GuitarApp extends Application {
    public GuitarAPI guitarAPI;

    @Override
    public void onCreate() {
        super.onCreate();
        guitarAPI = new GuitarAPI();
        guitarAPI.start(this);
        guitarAPI.fetchChords("");
        guitarAPI.fetchMusics(false);
    }
}
