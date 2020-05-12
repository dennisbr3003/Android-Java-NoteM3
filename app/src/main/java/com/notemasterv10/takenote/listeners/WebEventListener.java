package com.notemasterv10.takenote.listeners;

import com.notemasterv10.takenote.webservice.SharedPreferenceResponse;

public interface WebEventListener {

    public enum Action{
        SHOW_UPL_DL, HIDE_UPL_DL
    }

    public void showHideMenuItem(Action action);
    public void loadDownLoadedPreferences(SharedPreferenceResponse spr);

}
