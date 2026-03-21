package com.herbillon.guitar.network;

import android.content.Context;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.appcompat.app.AlertDialog;

public class GuitarAPI {
    public static final String API_BASE_URL = "http://192.168.1.26:8000/api";

    public static JSONArray dataChords = null;
    public static JSONArray dataSongs = null;
    public static JSONObject dataSong = null;
    private final Handler handler = new Handler();
    private Runnable updateRunnable;

    private static RequestQueue requestQueue;
    private final MutableLiveData<GuitarAPI> liveData = new MutableLiveData<>();

    public void addObserver(Observer<? super GuitarAPI> observer) {
        liveData.observeForever(observer);
    }

    public void removeObserver(Observer<? super GuitarAPI> observer) {
        liveData.removeObserver(observer);
    }

    public void start(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }


    public void fetchChords(String filter) {
        String url = API_BASE_URL + "/chords";

        if (filter.equals("major")) {
            url += "?isMajor=true";
        }
        if (filter.equals("minor")) {
            url += "?isMajor=false";
        }

        Log.d("GuitarAPI", "fetching chords");
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                url, null,
                response -> {
                    Log.d("GuitarAPI", "datas received : " + response);
                    dataChords = response;
                    liveData.setValue(this);
                }, error -> Log.e("GuitarAPI", error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void fetchMusics(boolean favorite) {
        String url = API_BASE_URL + "/musics";

        if (favorite) {
            url += "?favorite=true";
        }

        Log.d("GuitarAPI", "fetching musics");
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                url, null,
                response -> {
                    Log.d("GuitarAPI", "datas received : " + response);
                    dataSongs = response;
                    liveData.setValue(this);
                }, error -> Log.e("GuitarAPI", error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void fetchMusic(int id){
        String url = API_BASE_URL + "/music/" + id;
        Log.d("GuitarAPI", "fetching music");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null,
                response -> {
                    Log.d("GuitarAPI", "datas music : " + response);
                    dataSong = response;
                    liveData.setValue(this);
                }, error -> Log.e("GuitarAPI", error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void patchMusicFavorite(int id, boolean favorite, boolean inFavorite) {
        String url = API_BASE_URL + "/music/" + id + "/favorite";

        JSONObject body = new JSONObject();
        try {
            body.put("favorite", !favorite);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("GuitarAPI", "patching music favorite " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH,
                url, body,
                response -> {
                    Log.d("GuitarAPI", "datas received : " + response);
                    fetchMusics(inFavorite);
                    liveData.setValue(this);
                }, error -> {
            if (error.networkResponse != null) {
                Log.e("GuitarAPI", "Status: " + error.networkResponse.statusCode);
                Log.e("GuitarAPI", "Body: " + new String(error.networkResponse.data));
            } else {
                Log.e("GuitarAPI", error.toString());
            }
        }
        ){
            @Override
            public Map<String, String> getHeaders () {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/merge-patch+json");
                headers.put("Accept", "application/json");
                return headers;
            }
        } ;
        requestQueue.add(request);
    }

    public void patchMusicComment(int id, String comment) {
        String url = API_BASE_URL + "/music/" + id + "/comment";

        JSONObject body = new JSONObject();
        try {
            body.put("comment", comment);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("GuitarAPI", "patching music comment " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH,
                url, body,
                response -> {
                    Log.d("GuitarAPI", "datas received : " + response);
                    fetchMusic(id);
                    liveData.setValue(this);
                }, error -> {
            if (error.networkResponse != null) {
                Log.e("GuitarAPI", "Status: " + error.networkResponse.statusCode);
                Log.e("GuitarAPI", "Body: " + new String(error.networkResponse.data));
            } else {
                Log.e("GuitarAPI", error.toString());
            }
        }
        ){
            @Override
            public Map<String, String> getHeaders () {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/merge-patch+json");
                headers.put("Accept", "application/json");
                return headers;
            }
        } ;
        requestQueue.add(request);
    }
}