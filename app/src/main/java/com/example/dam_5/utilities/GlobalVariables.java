package com.example.dam_5.utilities;

import android.location.Location;

public class GlobalVariables {


    public boolean isHuntInProgress() {
        return isHuntInProgress;
    }

    public void setHuntInProgress(boolean huntInProgress) {
        isHuntInProgress = huntInProgress;
    }

    /*bool to know if hunt is ongoing*/
    private boolean isHuntInProgress = false;

    public String getLastHuntCode() {
        return lastHuntCode;
    }

    public void setLastHuntCode(String lastHuntCode) {
        this.lastHuntCode = lastHuntCode;
    }

    private String lastHuntCode;


    private static final GlobalVariables instance = new GlobalVariables();

    public static GlobalVariables getInstance() {
        return instance;
    }

    private GlobalVariables() {

    }


}
