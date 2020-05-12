package com.notemasterv10.takenote.webservice;

public class ArrayItemObject {
    private String item_id;
    private String item_name;
    private String item_value;
    private String item_datatype;

    public ArrayItemObject(String item_id, String item_name, String item_value, String item_datatype) {
        super();
        this.item_id = item_id;
        this.item_name = item_name;
        this.item_value = item_value;
        this.item_datatype = item_datatype;
    }

    public ArrayItemObject() {
        super();
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
