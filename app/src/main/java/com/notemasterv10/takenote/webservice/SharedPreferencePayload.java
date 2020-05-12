package com.notemasterv10.takenote.webservice;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

// pojo going up -->

public class SharedPreferencePayload {

    private String device_id;
    private List<ArrayItemObject> shared_preference = new ArrayList<ArrayItemObject>();

    public SharedPreferencePayload() {
    }

    public SharedPreferencePayload(String device_id) {
        this.device_id = device_id;
    }

    public SharedPreferencePayload(String device_id, List<ArrayItemObject> shared_preference) {
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

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Device identifier %s\n", getDevice_id()));
        sb.append(String.format("Object array size %s\n", shared_preference.size()));
        for(ArrayItemObject aio : shared_preference){
            sb.append(String.format("Name %s value %s\n", aio.item_name, aio.item_value));
        }
        return sb.toString();
    }

    class ArrayItemObject{

        private String item_id;
        private String item_name;
        private String item_value;
        private String item_datatype;

        public ArrayItemObject(String item_name, String item_value, String item_datatype) {
            this.item_id = getDevice_id();
            this.item_name = item_name;
            this.item_value = item_value;
            this.item_datatype = item_datatype;
        }

        public String getItem_id() {
            return item_id;
        }

        public void setItem_id(String item_id) {
            this.item_id = item_id;
        }

        public String getItem_name() {
            return item_name;
        }

        public void setItem_name(String item_name) {
            this.item_name = item_name;
        }

        public String getItem_value() {
            return item_value;
        }

        public void setItem_value(String item_value) {
            this.item_value = item_value;
        }

        public String getItem_datatype() {
            return item_datatype;
        }

        public void setItem_datatype(String item_datatype) {
            this.item_datatype = item_datatype;
        }
    }

}
