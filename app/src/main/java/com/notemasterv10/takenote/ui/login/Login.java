package com.notemasterv10.takenote.ui.login;

import com.notemasterv10.takenote.webservice.WebUser;

public class Login {

    private static Login instance = new Login();

    private WebUser webuser;

    private Login(){
    }

    public synchronized static Login getInstance(){
        if(instance == null){
            instance = new Login();
        }
        return instance;
    }

    public WebUser getWebuser() {
        return webuser;
    }

    public void setWebuser(WebUser webuser) {
        this.webuser = webuser;
    }
}
