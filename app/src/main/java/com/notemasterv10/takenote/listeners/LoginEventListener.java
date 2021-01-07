package com.notemasterv10.takenote.listeners;

import com.notemasterv10.takenote.data.model.LoggedInUser;
import com.notemasterv10.takenote.webservice.WebUser;

public interface LoginEventListener {

    public void processLogin(WebUser loggedInUserResult);

}
