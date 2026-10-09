package com.herbillon.guitar.ui.chords;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.herbillon.guitar.R;
import com.herbillon.guitar.model.Chord;

import java.util.List;
import java.util.function.Consumer;

public class ChordAdapter extends RecyclerView.Adapter<ChordAdapter.ChordViewHolder> {
    private List<Chord> chords;
    private final Consumer<Chord> onChordClick;

    public ChordAdapter(List<Chord> c, Consumer<Chord> onChordClick) {
        this.chords = c;
        this.onChordClick = onChordClick;
    }

    public static class ChordViewHolder extends RecyclerView.ViewHolder {
        ChordDiagramView diagram;
        TextView name;
        TextView label;

        public ChordViewHolder(@NonNull View itemView) {
            super(itemView);
            diagram = itemView.findViewById(R.id.chordDiagram);
            name = itemView.findViewById(R.id.chordName);
            label = itemView.findViewById(R.id.chordLabel);
        }
    }

    @NonNull
    @Override
    public ChordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chord, parent, false);
        return new ChordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChordViewHolder holder, int position) {
        Chord chord = chords.get(position);
        holder.name.setText(chord.getName());
        holder.label.setText(chord.getLabel());
        holder.diagram.setChord(chord);
        holder.itemView.setOnClickListener(v -> onChordClick.accept(chord));
    }

    @Override
    public int getItemCount() {
        return chords.size();
    }
}
