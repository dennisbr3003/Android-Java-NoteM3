package com.notemasterv10.takenote.webservice;

public class WebServiceChecker extends Thread {

    private volatile boolean isRunning = false;
    private WebService ws;

    public WebServiceChecker(){
    }

    public WebServiceChecker(boolean isRunning, WebService ws) {
        this.ws = ws;
        this.isRunning = isRunning;
    }

    public WebServiceChecker(WebService ws) {
        this.ws = ws;
    }

    public void setWebService(WebService ws) {
        this.ws = ws;
    }

    public void startChecker(){
        this.isRunning = true;
    }

    public void stopChecker(){
        this.isRunning = false;
    }

    @Override
    public void run() {
        super.run();
        while (isRunning) {
            try {
                Thread.sleep(5000);
                ws.checkForWebService(); // <-- this will update the status bar in the app
            } catch (InterruptedException e) {
                // do nothing
            }
        }

    }



}


