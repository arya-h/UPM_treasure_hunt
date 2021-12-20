package com.example.dam_5.ui.search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dam_5.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class
GenericUserActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ImageView proPic;


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

}