package com.example.dam_5.utilities;

import android.content.Context;
import android.util.Log;

import com.example.dam_5.SignInActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
/*json parser*/
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import io.grpc.internal.JsonParser;

public class AvatarGetter implements Runnable {

    private String username;
    private Context ctx;
    private URL[] urls;
    private String resultURL;

    public String getResultURL() {
        return resultURL;
    }




    public AvatarGetter(Context ctx, String username,  URL... urls){
        this.ctx = ctx;
        this.urls = urls;
        this.username = username;
    }

    public AvatarGetter(String username){
        this.username = username;
    }

    public String retrieveURL() {
        StringBuffer content = null;
        String authToken = "N2VlOWUyZGEtZDQxNy00NDdhLWFmZGQtODM0MjJhYTk5NmYy";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("format", "png");
        parameters.put("gender", "male");
        parameters.put("upload", true);
        parameters.put("username", username);



        try{
            //URL url = new URL("https://api.m3o.com/v1/avatar/Generate");
            HttpURLConnection http = (HttpURLConnection) new URL("https://api.m3o.com/v1/avatar/Generate").openConnection();
            http.setRequestMethod("POST");
            /*URLConnection con = url.openConnection();*/
            /*HttpURLConnection http = (HttpURLConnection)con;*/
            http.setDoOutput(true);
            http.setRequestMethod("POST"); // PUT is another valid option
            String jsonInputString = "{'username':'test', 'upload' : true, 'gender':'male', 'format':'png'}";

            try(OutputStream os = http.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            http.setRequestProperty("Content-Type", "application/json;");
            http.setRequestProperty("Authorization","Bearer "+"N2VlOWUyZGEtZDQxNy00NDdhLWFmZGQtODM0MjJhYTk5NmYy");
            BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String output;

            StringBuffer response = new StringBuffer();
            while ((output = in.readLine()) != null) {
                response.append(output);
            }

            in.close();
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(response.toString());
                JSONObject jObj = (JSONObject) obj;
                resultURL = jObj.get("url").toString();
            }catch (ParseException e){
                e.printStackTrace();
            }
            // printing result from response

            Log.d("dddd", "Response:-" + resultURL);
            http.disconnect();

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return resultURL;
    }

    @Override
    public void run() {
        // onPreExecuted
        ((SignInActivity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
               /* ((MainActivity)ctx).prepareUIStartDownload();*/
            }
        });

        Log.d("NETUTIL", urls[0].toString());

        // task to be performed
        String result = retrieveURL();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // onPostExecuted

        ((SignInActivity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                resultURL = result;
                ((SignInActivity) ctx).prepareFinishDownload(result);

            }
        });
    }
}


class ParameterStringBuilder {
    public static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }
}