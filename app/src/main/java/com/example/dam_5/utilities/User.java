package com.example.dam_5.utilities;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.LinkedList;

public class User {

    private static FirebaseFirestore db;
    FirebaseAuth auth;

    private static DocumentReference userRef;

    private String username;
    private String email;
    private LinkedList<String> friends;
    private Integer points;
    private boolean hasProfilePicture;

    public User(){

    }

    public static User getUserFromEmail(String email){

        db = FirebaseFirestore.getInstance();
        User x = new User();
        Log.d("USER.JAVA", "until here all good " + x.toString());

        userRef =

        userRef = db.collection("users").document(email);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("USER.JAVA", "DocumentSnapshot data: " + document.getData());

                        x.setUsername(document.getString("username"));
                        x.setEmail(email);
                        x.setProfilePictureURL(document.getString("profilePictureURL"));
                        x.setHasProfilePicture(document.getBoolean("hasProfilePicture"));
                        x.setPoints(((Long) document.get("points")).intValue() );
                        //future implementation will have friends also

                       /* x.setFriends(data.get("friends"));*/


                    } else {
                        Log.d("USER.JAVA", "No such document");
                        /*user doesnt exist*/
                    }
                } else {
                    Log.d("USER.JAVA", "get failed with ", task.getException());
                    /*show error screen*/
                }
            }
        });

        return x;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LinkedList<String> getFriends() {
        return friends;
    }

    public void setFriends(LinkedList<String> friends) {
        this.friends = friends;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public boolean isHasProfilePicture() {
        return hasProfilePicture;
    }

    public void setHasProfilePicture(boolean hasProfilePicture) {
        this.hasProfilePicture = hasProfilePicture;
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public void setProfilePictureURL(String profilePictureURL) {
        this.profilePictureURL = profilePictureURL;
    }

    private String profilePictureURL;

    public User(String username, String email, boolean hasProfilePicture, String profilePictureURL){
        this.username = username;
        this.email = email;
        this.hasProfilePicture = hasProfilePicture;
        this.profilePictureURL = profilePictureURL;
    }
}
