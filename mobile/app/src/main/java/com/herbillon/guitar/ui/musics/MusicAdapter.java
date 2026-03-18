package com.herbillon.guitar.ui.musics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.herbillon.guitar.R;
import com.herbillon.guitar.model.Music;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private List<Music> musics;

    public MusicAdapter(List<Music> m) {
        this.musics = m;
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView favorite;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.musicTitle);
            favorite = itemView.findViewById(R.id.musicFavorite);
        }
    }

    @NonNull
    @Override
    public MusicAdapter.MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_music, parent, false);
        return new MusicAdapter.MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.MusicViewHolder holder, int position) {
        Music music = musics.get(position);
        holder.title.setText(music.getTitle());
        if (music.getFavorite()){
            holder.favorite.setImageResource(R.drawable.ic_star_filled);
        } else {
            holder.favorite.setImageResource(R.drawable.ic_star_outline);
        }
    }

    @Override
    public int getItemCount() {
        return musics.size();
    }
}
