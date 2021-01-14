package com.notemasterv10.takenote.listeners;

import com.notemasterv10.takenote.webservice.WebUser;

public interface LoginEventListener {

    enum Action{
        REGISTER, LOGIN
    }

    public void processLogin(WebUser webuser, Action action);

}
