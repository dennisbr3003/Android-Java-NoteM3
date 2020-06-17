package com.notemasterv10.takenote.library;

public class PassPointImage {

    private String id;
    private String name;
    private String created;
    private String updated;
    private byte[] file;

    public PassPointImage() {
    }

    public PassPointImage(String id, String name, String created, String updated, byte[] file) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.updated = updated;
        this.file = file;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
