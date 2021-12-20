package com.example.dam_5.utilities;


import com.example.dam_5.SignInActivity;

import java.net.URL;
import android.content.Context;
import android.util.Log;

import java.net.URL;



public class DownloadThread implements Runnable {

    private Context ctx;
    private URL[] urls;
    private String username;
    private String resultURL;

    public String getResult() {
        return resultURL;
    }

    public void setResult(String result) {
        this.resultURL = result;
    }



    public DownloadThread(Context ctx, String username, URL... urls){
        this.ctx = ctx;
        this.urls = urls;
        this.username = username;
    }

    @Override
    public void run() {
        // onPreExecuted
        ((SignInActivity)ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
               /* ((MainActivity)ctx).prepareUIStartDownload();*/
            }
        });

        // task to be performed
        OkHttp ok = new OkHttp(username);
        String result = null;
        try {
            resultURL = ok.sendPost(username, urls[0].toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // onPostExecuted

        ((SignInActivity)ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((SignInActivity)ctx).prepareFinishDownload(result);

            }
        });
    }
}

