package com.notemasterv10.takenote.listing;

public class Note {

    private String name;
    private String created;
    private String updated;
    private byte[] file;
    private boolean isCurrentNote;
    private int listPosition;

    public Note(String name, String created, String updated, byte[] file) {
        this.name = name;
        this.created = created;
        this.updated = updated;
        this.file = file;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public boolean isCurrentNote() {
        return isCurrentNote;
    }

    public void setCurrentNote(boolean currentNote) {
        isCurrentNote = currentNote;
    }

    public void setCurrentNote(String currentNote) {
        if (getName().equals(currentNote)){
            this.isCurrentNote = true;
        } else {
            this.isCurrentNote = false;
        }
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }
}
