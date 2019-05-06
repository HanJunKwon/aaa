package com.example.rhinspeak.Model;


import java.util.ArrayList;
import java.util.List;

public class Word {
    public List<Syllable> getSyllables() {
        return syllables;
    }

    public void setSyllables(Syllable syllables) {
        this.syllables.add(syllables);
    }

    List<Syllable> syllables = new ArrayList<>();
}
