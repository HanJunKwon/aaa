package com.example.rhinspeak.Model;

import java.util.ArrayList;

public class Syllable {
    private char cho;
    private char jung;
    private char jong;
    private boolean is_same;
    private String syllable;
    private ArrayList consonant_vowel = new ArrayList();


    // 생성자
    public Syllable(){
        this.cho = 4519;
        this.jung = 4519;
        this.jong = 4519;
        this.is_same = true;
        this.syllable ="";
        this.consonant_vowel = new ArrayList();
    }

    public boolean getIs_same() {
        return is_same;
    }

    public void setIs_same(boolean is_same) {
        this.is_same = is_same;
    }

    public char getCho() {
        return cho;
    }

    public void setCho(char cho) {
        this.cho = cho;
    }

    public char getJung() {
        return jung;
    }

    public void setJung(char jung) {
        this.jung = jung;
    }

    public char getJong() {
        return jong;
    }

    public void setJong(char jong) {
        this.jong = jong;
    }

    public String getSyllable() {
        return syllable;
    }

    public void setSyllable(String syllable) {
        this.syllable = syllable;
    }

    public ArrayList getConsonant_vowel() {
        return consonant_vowel;
    }

    public ConsonantVowel getConsonant_vowel(int index) { return (ConsonantVowel) consonant_vowel.get(index); }

    public void setConsonant_vowel(ArrayList consonant_vowel) {
        this.consonant_vowel = consonant_vowel;
    }

    /**
     * size를 리턴하게 되면 종성이 비어있는지 여부를 확인할 수 있고 for문을 돌릴 때 사용할 수 있음
     * @return 초성/중성/종성 존재 여부 확인
     */
    public int getSize(){
        if(this.consonant_vowel.size() == 0){
            return 0;
        }

        ConsonantVowel jongObj = (ConsonantVowel) this.consonant_vowel.get(0);
        if (jongObj.getCharacter() == 4519){
            return 2;
        }else{
            return 3;
        }
    }
}
