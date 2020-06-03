package com.notemasterv10.takenote.interaction;

import com.notemasterv10.takenote.listing.Note;

public class ComplexBooleanAnswer extends Answer {

    private boolean answer = false;
    private Note note;

    public ComplexBooleanAnswer() {
        super("");
        this.answer = false;
    }

    public ComplexBooleanAnswer(boolean answer) {
        super("");
        this.answer = answer;
    }

    public ComplexBooleanAnswer(String extraInstructions, boolean answer) {
        super(extraInstructions);
        this.answer = answer;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public boolean isAnswer() {
        return answer;
    }

    public void setAnswer(boolean answer) {
        this.answer = answer;
    }
}
