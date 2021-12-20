package com.example.dam_5.utilities;

import com.google.android.gms.maps.model.LatLng;

/*chosen coordinates*/
public class Pin {
    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LatLng coordinates;
    public String title;

    public Pin(LatLng coordinates, String title) {
        this.coordinates = coordinates;
        this.title = title;
    }

    public Pin() {
        this.coordinates = new LatLng(0, 0);
        this.title = "";
    }
}

