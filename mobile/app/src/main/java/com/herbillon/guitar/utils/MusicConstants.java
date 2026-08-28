package com.herbillon.guitar.utils;

public class MusicConstants {

    public static final String STATUS_TO_LEARN = "to_learn";
    public static final String STATUS_LEARNING = "learning";
    public static final String STATUS_LEARNT = "learnt";

    public static final String DIFFICULTY_EASY = "easy";
    public static final String DIFFICULTY_MEDIUM = "medium";
    public static final String DIFFICULTY_HARD = "hard";

    public static final String[][] STATUS_VALUES = {
            {"", "Statut"},
            {STATUS_TO_LEARN, "À apprendre"},
            {STATUS_LEARNING, "En cours"},
            {STATUS_LEARNT, "Apprise"}
    };

    public static final String[][] DIFFICULTY_VALUES = {
            {"", "Difficulté"},
            {DIFFICULTY_EASY, "Facile"},
            {DIFFICULTY_MEDIUM, "Moyen"},
            {DIFFICULTY_HARD, "Difficile"}
    };

    public static final String[][] SORT_VALUES = {
            {"recent", "Récent"},
            {"title", "Titre"},
            {"artist", "Artiste"}
    };

    private MusicConstants() {
    }
}
