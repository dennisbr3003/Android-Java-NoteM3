package com.notemasterv10.takenote.interaction;

public class ExtendedStringAnswer extends Answer {

    private String answer="";

    private byte[] newNoteContent = null;
    private String newNoteName = "";
    private byte[] currentNoteContent = null;

    private String currentNoteName ="";
    private String rename_newname ="";
    private boolean isCurrentNote = false;

    private int position=0;

    public ExtendedStringAnswer() {
        super("");
    }

    public ExtendedStringAnswer(String answer) {
        super("");
        this.answer = answer;
    }

    public byte[] getNewNoteContent() {
        return newNoteContent;
    }

    public void setNewNoteContent(byte[] newNoteContent) {
        this.newNoteContent = newNoteContent;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCurrentNoteName() {
        return currentNoteName;
    }

    public void setCurrentNoteName(String currentNoteName) {
        this.currentNoteName = currentNoteName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean getCurrentNote() {
        return isCurrentNote;
    }

    public void setCurrentNote(boolean currentNote) {
        this.isCurrentNote = currentNote;
    }

    public byte[] getCurrentNoteContent() {
        return currentNoteContent;
    }

    public void setCurrentNoteContent(byte[] currentNoteContent) {
        this.currentNoteContent = currentNoteContent;
    }

    public String getNewNoteName() {
        return newNoteName;
    }

    public void setNewNoteName(String newNoteName) {
        this.newNoteName = newNoteName;
    }

}
