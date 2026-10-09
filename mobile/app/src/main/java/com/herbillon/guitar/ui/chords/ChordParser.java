package com.herbillon.guitar.ui.chords;

import com.herbillon.guitar.model.Chord;
import com.herbillon.guitar.model.ChordPosition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts the chords JSON returned by the API into {@link Chord} objects.
 *
 * <p>The raw data is stored in {@link GuitarAPI#dataChords} (a {@code JSONArray}) and is
 * fetched once when the app starts. This class holds the single parsing logic so that every
 * screen needing real {@code Chord} objects (name, label, barre, muted strings, finger
 * positions) can build them without duplicating the code.</p>
 *
 * <p>Used by:</p>
 * <ul>
 *   <li>{@code ChordsFragment}: builds the list of chord cards displayed on the Chords page.</li>
 *   <li>{@code MusicFragment}: finds the chord matching a chip name in order to open the
 *       zoom dialog ({@link ChordZoomDialog}).</li>
 * </ul>
 */
public class ChordParser {
    public static List<Chord> parseChords(JSONArray datas) {
        List<Chord> chords = new ArrayList<>();
        if (datas == null) return chords;
        try {
            for (int i = 0; i < datas.length(); i++) {
                JSONObject chordJson = datas.getJSONObject(i);
                JSONObject note = chordJson.getJSONObject("note");

                boolean isMajor = chordJson.getBoolean("isMajor");
                String label = note.getString("label") + (isMajor ? " majeur" : " mineur");
                label = label.substring(0, 1).toUpperCase() + label.substring(1);

                Chord chord = new Chord(chordJson.getInt("id"), chordJson.getString("name"), label, isMajor);

                if (!chordJson.isNull("barreFret")) {
                    chord.setBarreFret(chordJson.getInt("barreFret"));
                    chord.setBarreFromString(chordJson.getInt("barreFromString"));
                    chord.setBarreToString(chordJson.getInt("barreToString"));
                }

                if (!chordJson.isNull("mutedStrings")) {
                    JSONArray mutedArray = chordJson.getJSONArray("mutedStrings");
                    List<Integer> muted = new ArrayList<>();
                    for (int j = 0; j < mutedArray.length(); j++) {
                        muted.add(mutedArray.getInt(j));
                    }
                    chord.setMutedStrings(muted);
                }

                JSONArray positionsArray = chordJson.getJSONArray("positions");
                List<ChordPosition> positions = new ArrayList<>();
                for (int j = 0; j < positionsArray.length(); j++) {
                    JSONObject pos = positionsArray.getJSONObject(j);
                    positions.add(new ChordPosition(pos.getInt("string"), pos.getInt("fret")));
                }
                chord.setPositions(positions);

                chords.add(chord);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chords;
    }
}
