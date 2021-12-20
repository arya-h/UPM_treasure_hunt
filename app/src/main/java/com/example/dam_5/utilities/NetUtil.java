package com.example.dam_5.utilities;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class NetUtil {
    public static String readTextUrl(URL wsUrl){
        Log.d("NETUTIL", wsUrl.toString());
        StringBuilder response = new StringBuilder();
        try {
            URLConnection conn = wsUrl.openConnection();

            BufferedReader br = new BufferedReader (new InputStreamReader(
                    conn.getInputStream()));
            String inputLine;
            while ((inputLine = br.readLine()) != null)
                response.append(inputLine);
            Log.i ("NetUtil", response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }
}
