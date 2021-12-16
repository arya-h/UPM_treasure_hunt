package com.example.dam_5.utilities;

public class GlobalVariables {


    public boolean isHuntInProgress() {
        return isHuntInProgress;
    }

    public void setHuntInProgress(boolean huntInProgress) {
        isHuntInProgress = huntInProgress;
    }

    /*bool to know if hunt is ongoing*/
    private boolean isHuntInProgress = false;


    private static final GlobalVariables instance = new GlobalVariables();
    public static GlobalVariables getInstance(){
        return instance;
    }

    private GlobalVariables(){

    }


}
