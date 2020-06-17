package com.notemasterv10.takenote.webservice;

import java.util.ArrayList;
import java.util.List;

// pojo coming in -->

public class UserDataResponse {
    private String device_id;
    private List<ArrayItemObject> shared_preference = new ArrayList<ArrayItemObject>();

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


    public int getArraySize(){
        return this.shared_preference.size();
    }

    public ArrayItemObject getArrayElement(int idx){
        return this.shared_preference.get(idx);
    }

}
