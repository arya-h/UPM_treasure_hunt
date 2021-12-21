package com.example.dam_5.ui.search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dam_5.MainActivity;
import com.example.dam_5.R;
import com.example.dam_5.utilities.DownloadFactThread;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

public class
GenericUserActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ImageView proPic;
    private TextView randomFactText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_user);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String username;
        TextView usr = findViewById(R.id.usr_text);
        usr.setTextSize(24);

        proPic = findViewById(R.id.generic_user_profile_picture);
        URL wsUrl = null;
        randomFactText = findViewById(R.id.randomFactText);

        try {
            wsUrl = new URL("https://uselessfacts.jsph.pl/random.json?language=en");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        DownloadFactThread dTask = new DownloadFactThread(GenericUserActivity.this,
                new URL[]{wsUrl});
        Thread th = new Thread(dTask);
        th.start();







        db.collection("users").document(email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            if(documentSnapshot.getBoolean("hasProfilePicture")){
                                Picasso.get().load(documentSnapshot.getString("profilePictureURL")).into(proPic);
                            }
                            String tmp = "@"+documentSnapshot.getString("username");
                            usr.setText(tmp);
                        }
                    }
                });


    }

    public void prepareUIFinishDownload(String str){
        randomFactText.setText(str);
    }

}