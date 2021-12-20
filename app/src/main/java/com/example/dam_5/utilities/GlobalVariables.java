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
    private boolean isHuntInProgress;
    private String lastHuntCode;
    private String username;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String email;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public String getLastHuntCode() {
        return lastHuntCode;
    }

    public void setLastHuntCode(String lastHuntCode) {
        this.lastHuntCode = lastHuntCode;
    }




    private static final GlobalVariables instance = new GlobalVariables();

    public static GlobalVariables getInstance() {
        return instance;
    }

    private GlobalVariables() {

    }


}
