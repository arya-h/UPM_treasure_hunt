package com.example.dam_5.utilities;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {

    public  FirebaseFirestore db;
    public  FirebaseAuth mAuth;
    public String username;


    public FirebaseManager(){
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        db.collection("users").document(mAuth.getCurrentUser().getEmail())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            username = documentSnapshot.getString("username");
                        }
                    }
                });
    }
}
