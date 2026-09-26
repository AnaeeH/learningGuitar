package com.herbillon.guitar.ui.musics;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.herbillon.guitar.R;
import com.herbillon.guitar.model.Music;
import com.herbillon.guitar.utils.MusicConstants;

import java.util.List;
import java.util.function.Consumer;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private List<Music> musics;
    private Consumer<Music> onFavoriteClick;
    private Consumer<Music> onMusicClick;

    public MusicAdapter(List<Music> m, Consumer<Music> onFavoriteClick, Consumer<Music> onMusicClick) {
        this.musics = m;
        this.onFavoriteClick = onFavoriteClick;
        this.onMusicClick = onMusicClick;
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView artist;
        ImageView favorite;
        ImageView difficulty;
        View difficultyContainer;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.musicTitle);
            artist = itemView.findViewById(R.id.musicArtist);
            favorite = itemView.findViewById(R.id.musicFavorite);
            difficulty = itemView.findViewById(R.id.musicDifficulty);
            difficultyContainer = itemView.findViewById(R.id.musicDifficultyContainer);
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
        holder.artist.setText(music.getArtist() + "  -  " + music.getProgress() + "%");

        switch (music.getDifficulty()) {
            case MusicConstants.DIFFICULTY_MEDIUM:
                holder.difficulty.setImageResource(R.drawable.ic_music_note_medium);
                break;
            case MusicConstants.DIFFICULTY_HARD:
                holder.difficulty.setImageResource(R.drawable.ic_music_note_hard);
                break;
            default:
                holder.difficulty.setImageResource(R.drawable.ic_music_note_easy);
                break;
        }

        int backgroundColorRes = music.getRiff() ? R.color.app_accent : R.color.app_surface_variant;
        holder.difficultyContainer.setBackgroundTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(holder.itemView.getContext(), backgroundColorRes)
                )
        );

        if (music.getFavorite()) {
            holder.favorite.setImageResource(R.drawable.ic_star_filled);
        } else {
            holder.favorite.setImageResource(R.drawable.ic_star_outline);
        }

        holder.favorite.setOnClickListener(
                v -> onFavoriteClick.accept(music)
        );
        holder.itemView.setOnClickListener(
                v -> onMusicClick.accept(music)
        );
    }

    @Override
    public int getItemCount() {
        return musics.size();
    }
}
