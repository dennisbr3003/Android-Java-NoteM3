package com.notemasterv10.takenote.webservice;

public class WebUser {

    private String name;
    private String password;
    private String device_id;
    private String remark;

    public WebUser() {
    }

    public WebUser(String name, String password, String device_id, String remark) {
        this.name = name;
        this.password = password;
        this.device_id = device_id;
        this.remark = remark;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
