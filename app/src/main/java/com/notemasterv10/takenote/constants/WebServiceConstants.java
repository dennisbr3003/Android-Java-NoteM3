package com.notemasterv10.takenote.constants;

public interface WebServiceConstants {

    String BASE_URL = "http://192.168.178.21:8080/notemaster/";
    //String BASE_URL = "https://takenote10.herokuapp.com/notemaster/";
    String JSON_UTF8 = "application/json; charset=utf-8";
    String RESPONSE_STATUS = "status";
    String IS_SUCCESS = "1";
    String CONN_IS_ALIVE = "ping";
    String PROC_USER_DATA = "userdata";
    String AUTH_USER = "user/authenticate";
    String DEVICE_HAS_DATA = "device/%s/hasdata";
    String SIGNATURE_FIELD = "message";
    String SIGNATURE_KEY = "device_id";
    String USER_ROLE = "USER";
    String ADMIN_ROLE = "ADMIN";
    String GOD_ROLE = "GOD_MODE";
    enum SyncDirection{
        UP, DOWN
    }

}
