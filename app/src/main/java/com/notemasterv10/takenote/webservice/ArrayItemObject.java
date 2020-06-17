package com.notemasterv10.takenote.webservice;

public class ArrayItemObject {
    private String itemId;
    private String itemName;
    private String itemValue;
    private String itemDatatype;

    public ArrayItemObject(String itemId, String itemName, String itemValue, String itemDatatype) {
        super();
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemValue = itemValue;
        this.itemDatatype = itemDatatype;
    }

    public ArrayItemObject() {
        super();
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
