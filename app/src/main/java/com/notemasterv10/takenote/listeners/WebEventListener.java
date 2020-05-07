package com.notemasterv10.takenote.listeners;

public interface WebEventListener {

    public enum Action{
        SHOW_UPLOAD, HIDE_UPLOAD
    }

    public void showHideMenuItem(Action action);

}
