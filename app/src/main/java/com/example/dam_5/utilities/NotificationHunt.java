package com.example.dam_5.utilities;

public class NotificationHunt {

    private String type;
    private String user;
    private String pinName;

    /*constructor for new discovery*/
    public NotificationHunt(String type, String user, String pinName ){
        this.type = type;
        this.pinName = pinName;
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPinName() {
        return pinName;
    }

    public void setPinName(String pinName) {
        this.pinName = pinName;
    }



}
