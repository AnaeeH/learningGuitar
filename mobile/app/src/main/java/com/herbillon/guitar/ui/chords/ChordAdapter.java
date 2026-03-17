package com.herbillon.guitar.ui.chords;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.herbillon.guitar.R;
import com.herbillon.guitar.model.Chord;

import java.util.List;

public class ChordAdapter extends RecyclerView.Adapter<ChordAdapter.ChordViewHolder> {
    private List<Chord> chords;

    public ChordAdapter(List<Chord> c) {
        this.chords = c;
    }

    public static class ChordViewHolder extends RecyclerView.ViewHolder {
        ImageView diagram;
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

        String base64 = chord.getDiagram();
        String base64Data = base64.substring(base64.indexOf(",") + 1);
        byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        holder.diagram.setImageBitmap(bitmap);
        holder.diagram.setColorFilter(android.graphics.Color.WHITE, android.graphics.PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public int getItemCount() {
        return chords.size();
    }
}
