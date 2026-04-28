package com.herbillon.guitar.ui.music;

import com.herbillon.guitar.model.Beat;
import com.herbillon.guitar.model.Measure;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MusicParser {
    public static List<Measure> parseMeasures(JSONObject data) throws JSONException {
        List<Measure> measures = new ArrayList<>();
        JSONArray measuresArray = data.getJSONArray("measures");

        for (int i = 0; i < measuresArray.length(); i++) {
            JSONObject mObj = measuresArray.getJSONObject(i);
            Measure measure = new Measure();
            measure.numero = mObj.getInt("numero");
            measure.repeatStart = mObj.getBoolean("repeatStart");
            measure.repeatEnd = mObj.getBoolean("repeatEnd");
            measure.beats = new ArrayList<>();

            JSONArray beatsArray = mObj.getJSONArray("beats");
            for (int j = 0; j < beatsArray.length(); j++) {
                JSONObject bObj = beatsArray.getJSONObject(j);
                Beat beat = new Beat();
                beat.isRest = bObj.getBoolean("isRest");
                beat.tied = bObj.optBoolean("tied");
                beat.position = bObj.getInt("position");
                beat.duration = bObj.getInt("duration");
                beat.type = bObj.getString("type");
                beat.dot = bObj.getBoolean("dot");

                beat.pitchStep = bObj.optString("pitchStep", "");
                beat.pitchOctave = bObj.optInt("pitchOctave", 0);
                beat.pitchAlter = bObj.optInt("pitchAlter", 0);

                beat.string = bObj.optInt("string", -1);
                beat.fret = bObj.optInt("fret", -1);

                beat.harmonyText = bObj.optString("harmonyText", null);
                beat.strumDirection = bObj.optString("strumDirection", null);

                measure.beats.add(beat);
            }
            measures.add(measure);
        }
        return measures;
    }
}
