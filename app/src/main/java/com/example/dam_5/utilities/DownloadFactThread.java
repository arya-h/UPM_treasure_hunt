package com.example.dam_5.utilities;

import android.content.Context;
import com.example.dam_5.MainActivity;
import com.example.dam_5.ui.search.GenericUserActivity;

import android.content.Context;
import android.util.JsonReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.InputStreamReader;
import java.net.URL;

public class DownloadFactThread implements Runnable{

    private Context ctx;
    private URL[] urls;

    public DownloadFactThread(Context ctx, URL... urls){
        this.ctx = ctx;
        this.urls = urls;
    }

    @Override
    public void run() {
        // onPreExecuted
        /*((GenericUserActivity)ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                *//*((MainActivity)ctx).prepareUIStartDownload();*//*
            }
        });*/

        // task to be performed
        String result = NetUtils.readTextUrl(urls[0]);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // onPostExecuted
        ((GenericUserActivity)ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String finalResult = "";

                JSONParser parser = new JSONParser();
                try {
                    Object obj = parser.parse(result);
                    JSONObject jObj = (JSONObject) obj;
                    finalResult = (String)jObj.get("text");

                } catch (ParseException exception) {
                    exception.printStackTrace();
                }


                ((GenericUserActivity)ctx).prepareUIFinishDownload(finalResult);

            }
        });
    }
}

