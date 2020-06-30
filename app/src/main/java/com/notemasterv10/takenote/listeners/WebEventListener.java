package com.notemasterv10.takenote.listeners;

import androidx.appcompat.app.AlertDialog;

import com.notemasterv10.takenote.webservice.UserDataResponse;

public interface WebEventListener {

    enum Action{
        SHOW_UPL_DL, HIDE_UPL_DL
    }

    void loadDownLoadedUserData(UserDataResponse spr, AlertDialog dlg);

}
