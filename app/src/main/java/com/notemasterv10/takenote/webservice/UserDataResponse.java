package com.notemasterv10.takenote.webservice;

import android.media.Image;

import com.notemasterv10.takenote.library.PassPointImage;
import com.notemasterv10.takenote.listing.Note;

import java.util.ArrayList;
import java.util.List;

// pojo coming in -->

public class UserDataResponse {
    private String device_id;
    private List<ArrayItemObject> shared_preference = new ArrayList<ArrayItemObject>();
    private List<Note> noteList = new ArrayList<Note>();
    private List<PassPointImage> passPointImageList = new ArrayList<PassPointImage>();

    public UserDataResponse() {
        super();
    }

    public UserDataResponse(String device_id) {
        super();
        this.device_id = device_id;
    }

    public UserDataResponse(String device_id, List<ArrayItemObject> shared_preference) {
        super();
        this.device_id = device_id;
        this.shared_preference = shared_preference;
    }

    public UserDataResponse(String device_id, List<ArrayItemObject> shared_preference, List<Note> noteList) {
        this.device_id = device_id;
        this.shared_preference = shared_preference;
        this.noteList = noteList;
    }

    public UserDataResponse(String device_id, List<ArrayItemObject> shared_preference, List<Note> noteList, List<PassPointImage> passPointImageList) {
        this.device_id = device_id;
        this.shared_preference = shared_preference;
        this.noteList = noteList;
        this.passPointImageList = passPointImageList;
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

    public int getSharedPreferenceArraySize(){
        return this.shared_preference.size();
    }

    public ArrayItemObject getSharedPrefernceArrayElement(int idx){
        return this.shared_preference.get(idx);
    }

    public int getNoteArraySize(){
        return this.noteList.size();
    }

    public Note getNoteArrayElement(int idx){
        return this.noteList.get(idx);
    }
    public int getPasspointImageArraySize(){
        return this.passPointImageList.size();
    }

    public PassPointImage getPasspointImageArrayElement(int idx){
        return this.passPointImageList.get(idx);
    }


}
