package com.example.dam_5.utilities;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.FormBody;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class OkHttp {

        private String username;

        public OkHttp(String username){
            this.username = username;
        }

        public String getFinalURL() {
            return finalURL;
        }

        private String finalURL;

        // one instance, reuse
        private final OkHttpClient httpClient = new OkHttpClient();

        public String sendPost(String username, String requestUrl) throws Exception {

            // form parameters
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("format", "png")
                    .add("upload", "true")
                    .add("gender","male")
                    .build();



            String userToken = " N2VlOWUyZGEtZDQxNy00NDdhLWFmZGQtODM0MjJhYTk5NmYy";

            Request request = new Request.Builder()
                    .url(requestUrl)
                    .addHeader("Content-Type", "application/json;")
                    .addHeader("Authorization" , "Bearer" + userToken)
                    .post(formBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                // Get response body
                System.out.println(response.body().toString());
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(response.body().toString());
                JSONObject jObj = (JSONObject) obj;

                finalURL = jObj.get("url").toString();
                return finalURL;


            }

        }

        private static OkHttpClient createAuthenticatedClient(final String username,
                                                              final String password) {
            // build client with authentication information.
            OkHttpClient httpClient = new OkHttpClient.Builder().authenticator(new Authenticator() {
                public Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder().header("Authorization", credential).build();
                }
            }).build();
            return httpClient;
        }
    }

