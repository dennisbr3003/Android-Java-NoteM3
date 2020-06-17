package com.notemasterv10.takenote.webservice;

import androidx.annotation.NonNull;

import com.notemasterv10.takenote.library.PassPointImage;
import com.notemasterv10.takenote.listing.Note;

import java.util.ArrayList;
import java.util.List;

// pojo going up -->

public class UserDataPayload {

    private String device_id;
    private List<ArrayItemObject> shared_preference = new ArrayList<ArrayItemObject>();
    private List<Note> noteList = new ArrayList<Note>();
    private List<PassPointImage> passPointImageList = new ArrayList<PassPointImage>();

    public UserDataPayload() {
    }

    public UserDataPayload(String device_id) {
        this.device_id = device_id;
    }

    public UserDataPayload(String device_id, List<ArrayItemObject> shared_preference) {
        this.device_id = device_id;
        this.shared_preference = shared_preference;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public List<ArrayItemObject> getShared_preference() {
        return shared_preference;
    }

    public void setShared_preference(List<ArrayItemObject> shared_preference) {
        this.shared_preference = shared_preference;
    }

    public void addElement(String item_id, String item_name, String item_value, String item_datatype){
        ArrayItemObject aio = new ArrayItemObject(item_name, item_value, item_datatype);
        shared_preference.add(aio);
    }

    public List<Note> getNoteList() {
        return noteList;
    }

    public void setNoteList(List<Note> noteList) {
        this.noteList = noteList;
    }

    public List<PassPointImage> getPassPointImageList() {
        return passPointImageList;
    }

    public void setPassPointImageList(List<PassPointImage> passPointImageList) {
        this.passPointImageList = passPointImageList;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Device identifier %s\n", getDevice_id()));
        sb.append(String.format("Object array size %s\n", shared_preference.size()));
        for(ArrayItemObject aio : shared_preference){
            sb.append(String.format("Name %s value %s\n", aio.itemName, aio.itemValue));
        }
        return sb.toString();
    }

    class ArrayItemObject{

        private String itemId;
        private String itemName;
        private String itemValue;
        private String itemDatatype;

        public ArrayItemObject(String itemName, String itemValue, String itemDatatype) {
            this.itemId = getDevice_id();
            this.itemName = itemName;
            this.itemValue = itemValue;
            this.itemDatatype = itemDatatype;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getItemValue() {
            return itemValue;
        }

        public void setItemValue(String itemValue) {
            this.itemValue = itemValue;
        }

        public String getItemDatatype() {
            return itemDatatype;
        }

        public void setItemDatatype(String itemDatatype) {
            this.itemDatatype = itemDatatype;
        }

    }

}
