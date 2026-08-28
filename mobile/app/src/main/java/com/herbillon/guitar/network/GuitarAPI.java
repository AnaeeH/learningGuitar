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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.appcompat.app.AlertDialog;

public class GuitarAPI {
    public static final String API_BASE_URL = "https://guitarapi-yl09.onrender.com/api";
    //static final String API_BASE_URL = "http://192.168.1.26:8000/api";

    public static JSONArray dataChords = null;
    public static JSONArray dataSongs = null;
    public static JSONObject dataSong = null;
    public static JSONArray dataSongChords = null;
    public static JSONObject dataTablature = null;

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

    /**
     * Ping Render every 13 minutes to prevent it from going into sleep mode
     */
    public void pingApi() {
        String url = API_BASE_URL + "/ping";
        Log.d("GuitarAPI", "pinging api to keep server awake");

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> Log.d("GuitarAPI", "ping ok"),
                error -> Log.e("GuitarAPI", "ping failed: " + error.toString())
        );
        requestQueue.add(request);
    }

    /**
     * Fetches all chords that match the selected filter
     *
     * @param filter, filter of chords
     */
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

    /**
     * Fetches all music that match the selected filter
     *
     * @param favorite, boolean favorite or not
     * @param riff, boolean riff or not
     * @param status, to learn, learnt, learned
     * @param difficulty, easy, medium, hard
     */
    public void fetchMusics(boolean favorite, boolean riff, String status, String difficulty) {
        String url = API_BASE_URL + "/get/musics/recent";

        List<String> params = new ArrayList<>();

        if (favorite) {
            params.add("favorite=true");
        }
        if (riff) {
            params.add("riff=true");
        }
        if (status != null) {
            params.add("status=" + status);
        }
        if (difficulty != null) {
            params.add("difficulty=" + difficulty);
        }
        if (!params.isEmpty()) {
            url += "?" + String.join("&", params);
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

    /**
     * Fetches the data for the selected music
     *
     * @param id, id of the music
     */
    public void fetchMusic(int id){
        String url = API_BASE_URL + "/get/music/" + id;
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

    /**
     * Fetches the chords data for the selected music
     *
     * @param id, id of the music
     */
    public void fetchMusicChords(int id){
        String url = API_BASE_URL + "/get/music/" + id + "/chords";
        Log.d("GuitarAPI", "fetching music");
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                url, null,
                response -> {
                    Log.d("GuitarAPI", "datas music : " + response);
                    dataSongChords = response;
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

    /**
     * Fetches the tablature data for the selected music
     *
     * @param id, id of the music
     */
    public void fetchTablature(int id){
        String url = API_BASE_URL + "/get/music/" + id + "/tablature";
        Log.d("GuitarAPI", "fetching tablature");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null,
                response -> {
                    Log.d("GuitarAPI", "tablature received : " + response);
                    dataTablature = response;
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

    /**
     * Function to add a music to these favorites
     *
     * @param id, id of the music
     * @param favorite, a boolean true or false
     * @param inFavorite, a boolean value of its previous value
     * @param inRiff,  boolean value for the riff
     */
    public void patchMusicFavorite(int id, boolean favorite, boolean inFavorite, boolean inRiff) {
        String url = API_BASE_URL + "/patch/music/" + id + "/favorite";

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
                    fetchMusics(inFavorite, inRiff,null, null);
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

    /**
     * Function to patch the music
     * For comment, difficulty and status of the music
     *
     * @param id, id of the music
     * @param field, field (comment, difficulty or progress)
     * @param value, new value
     */
    public void patchMusic(int id, String field, Object value) {
        String url = API_BASE_URL + "/patch/music/" + id + "/" + field;

        JSONObject body = new JSONObject();
        try {
            body.put(field, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("GuitarAPI", "patching music " + url);
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
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/merge-patch+json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        requestQueue.add(request);
    }
}