package com.herbillon.guitar.ui.chords;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.herbillon.guitar.R;
import com.herbillon.guitar.model.Chord;

public class ChordZoomDialog {
    public static void show(Context context, Chord chord) {
        Dialog dialog = new Dialog(context, R.style.ZoomDialog);
        View root = LayoutInflater.from(context).inflate(R.layout.dialog_chord, null);
        ((ChordDiagramView) root.findViewById(R.id.dialogChordDiagram)).setChord(chord);
        ((TextView) root.findViewById(R.id.dialogChordName)).setText(chord.getName());
        ((TextView) root.findViewById(R.id.dialogChordLabel)).setText(chord.getLabel());
        root.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(root);
        dialog.show();
    }
}
