package com.example.dam_5.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.example.dam_5.MainActivity;
import com.example.dam_5.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FireBaseNotification extends FirebaseMessagingService {

    private String currentUser=null;
    private String lastHunt;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        FirebaseManager fm = new FirebaseManager();

        Log.d("NOTIFICATION ", remoteMessage.toString());

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        String tag = remoteMessage.getNotification().getTag();

        Log.d("NOTIFICATION", title + " - " + body + " - " + tag);
        /*the tag can be split in "TYPE_MSG-HUNT_ID"  */
        /*String flag = remoteMessage.get*/
        String typeNotif = tag.split("-")[0];
        String huntID = tag.split("-")[1];
        String username = tag.split("-")[2];

        final String CHANNEL_ID = "DISCOVER_NOTIFICATION";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "TreasureHunt",
                NotificationManager.IMPORTANCE_HIGH);

        getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);

        fm.db.collection("users").document(fm.mAuth.getCurrentUser().getEmail())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            currentUser = task.getResult().getString("username");
                            lastHunt = task.getResult().getString("lastHunt");


                            if(typeNotif.equals("newHunt")){
                                /*dont send notification if i'm the one who made the hunt*/
                                if(username.equals(currentUser)){
                                    return;
                                }

                                Log.d("NOTIFICATION", remoteMessage.getNotification().getBody().toString());
                                Notification.Builder notif = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                                        .setContentTitle(title)
                                        .setContentText(body)
                                        .setSmallIcon(R.drawable.treasure)
                                        .setAutoCancel(true);
                                NotificationManagerCompat.from(getApplicationContext()).notify(1, notif.build());
                            }else if(typeNotif.equals("pinDiscovered") || typeNotif.equals("finishedHunt")) {

                                /*check if im following this specific hunt*/
                                fm.db.collection("hunts").document(huntID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                                        if(documentSnapshot.exists()){

                                            /*only notify if you are following that hunt and if it's still ongoing*/
                                            if (huntID.equals(lastHunt) && documentSnapshot.getBoolean("isOngoing") ) {
                                                Notification.Builder notif = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                                                        .setContentTitle(title)
                                                        .setContentText(body)
                                                        .setSmallIcon(R.drawable.treasure)
                                                        .setAutoCancel(true);
                                                NotificationManagerCompat.from(getApplicationContext()).notify(1, notif.build());


                                                /*close hunt if finished*/
                                                if(typeNotif.equals("finishedHunt")){
                                                    fm.db.collection("users").document(fm.mAuth.getCurrentUser().getEmail())
                                                            .update("isOnHunt", false);

                                                    fm.db.collection("hunts").document(huntID)
                                                            .update("isOnGoing", false);

                                                    /*winner is who put the last notification*/
                                                    fm.db.collection("hunts").document(huntID)
                                                            .update("winner", username);

                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                }
                                            }
                                        }
                                    }
                                });

                        }
                    }
                };

        });
    }
}
