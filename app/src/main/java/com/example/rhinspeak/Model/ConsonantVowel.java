package com.example.rhinspeak.Model;

public class ConsonantVowel {
    private boolean consonant; // 자음 = true, 모음 = false
    private char character; // 자음 또는 모음 하나
    private boolean current; // 정답, 오답 여부

    public boolean isConsonant() {
        return consonant;
    }

    public void setConsonant(boolean consonant) {
        this.consonant = consonant;
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}
