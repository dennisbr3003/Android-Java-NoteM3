package com.notemasterv10.takenote.listeners;

import com.notemasterv10.takenote.webservice.UserDataResponse;

public interface WebEventListener {

    enum Action{
        SHOW_UPL_DL, HIDE_UPL_DL
    }

    void loadDownLoadedPreferences(UserDataResponse spr);

}
