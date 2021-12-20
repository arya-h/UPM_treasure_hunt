package com.example.dam_5;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dam_5.ui.hunt.HuntFragment;
import com.example.dam_5.utilities.GlobalVariables;
import com.example.dam_5.utilities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dam_5.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.squareup.picasso.Picasso;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity {

    //in main activity will retrieve main information

    //will check if user has previously signed in
    //automatically log in with firebase api

    //firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private FloatingActionButton newHuntFab, joinHuntFab;
    private TextView joinHuntCodeInput;
    private TextView newHuntText, joinHuntText;
    private ExtendedFloatingActionButton parentFab;
    private boolean isAllFabsVisible;

    private Button shareCodeButton;
    private Button leaveHuntButton;
    private TextView mainTitle;


    /*sidebar*/
    private TextView usernameSidebar;
    private TextView emailSidebar;
    private ImageView profilePictureSidebar;
    private ImageButton logoutButton;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //authentication instance for firebase
        mAuth = FirebaseAuth.getInstance();
        //check if already authenticated
        FirebaseUser currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        /*NOTIFICATION MANAGER THROUGH FIREBASE TRIGGERS*/
        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        /*notify on creation of new hunt*/
        firebaseMessaging.subscribeToTopic("newHunt");

        /*notify about current hunt, if ongoing*/
        db.collection("users").document(mAuth.getCurrentUser().getEmail())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    if(documentSnapshot.getBoolean("isOnHunt")){
                        firebaseMessaging.subscribeToTopic(documentSnapshot.getString("lastHunt"));
                    }
                }
            }
        });




        /*declare floating action buttons*/
        parentFab = findViewById(R.id.fab);
        newHuntFab = findViewById(R.id.new_hunt);
        joinHuntFab = findViewById(R.id.join_hunt);
        newHuntText = findViewById(R.id.new_hunt_action_text);
        joinHuntText = findViewById(R.id.join_hunt_action_text);

        /*set them to invisibile at the beginning*/
        newHuntFab.setVisibility(View.GONE);
        newHuntText.setVisibility(View.GONE);
        joinHuntFab.setVisibility(View.GONE);
        joinHuntText.setVisibility(View.GONE);



        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_search, R.id.nav_profile, R.id.nav_hunt)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        /*sidebar text & img*/
        logoutButton = navigationView.getHeaderView(0).findViewById(R.id.logoutButton);
        usernameSidebar = navigationView.getHeaderView(0).findViewById(R.id.usernameSidebar);
        emailSidebar = navigationView.getHeaderView(0).findViewById(R.id.emailSidebar);
        profilePictureSidebar = navigationView.getHeaderView(0).findViewById(R.id.profilePictureSidebar);
        /*sidebar button logout*/

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mAuth.signOut();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog

                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();

            }
        });

        /*set sidebar values*/
        emailSidebar.setText(currentUser.getEmail());
        db.collection("users").document(currentUser.getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    /*also update global variables*/
                    GlobalVariables.getInstance().setUsername(username);
                    GlobalVariables.getInstance().setEmail(currentUser.getEmail());
                    username = task.getResult().getString("username");
                    boolean onHunt = task.getResult().getBoolean("isOnHunt");
                    Log.d("BUGFIX", "user "+ currentUser.getEmail() + " is on hunt? " + onHunt);
                    GlobalVariables.getInstance().setHuntInProgress(onHunt);
                    GlobalVariables.getInstance().setLastHuntCode(task.getResult().getString("lastHunt"));
                    usernameSidebar.setText("@"+username);
                    String url =  task.getResult().getString("profilePictureURL");
                    if(url != null){
                        Picasso.get().load(url).into(profilePictureSidebar);
                    }


                    /*handle buttons*/
                    if (GlobalVariables.getInstance().isHuntInProgress()) {

                        newHuntFab.setEnabled(false);
                        joinHuntFab.setEnabled(false);
                        parentFab.setBackgroundColor(Color.RED);
                        parentFab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Snackbar.make(view, "There is already a treasure hunt underway. Either abandon the current one or wait for" +
                                        "it to finish!", BaseTransientBottomBar.LENGTH_LONG).show();

                            }
                        });
                    } else {
                        parentFab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!isAllFabsVisible) {
                                    newHuntFab.show();
                                    newHuntText.setVisibility(View.VISIBLE);
                                    joinHuntFab.show();
                                    joinHuntText.setVisibility(View.VISIBLE);

                                    parentFab.extend();

                                    isAllFabsVisible = true;
                                } else {
                                    newHuntFab.hide();
                                    newHuntText.setVisibility(View.GONE);
                                    joinHuntFab.hide();
                                    joinHuntText.setVisibility(View.GONE);


                                    isAllFabsVisible = false;
                                }
                            }
                        });
                    }

                    parentFab.shrink();

                }
            }
        });

        isAllFabsVisible = false;

        /*update value of isHuntInProgress on creation*/
        /*DocumentReference user =*/
        /*then add listener*/





        /*set action button to start a new hunt*/
        newHuntFab.setBackgroundResource(R.drawable.treasure);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.new_hunt_dialog_title)
                .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        /*intent to new hunt screen*/
                        Intent intent = new Intent(getApplicationContext(), NewHuntActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog

                    }
                });

        /*new hunt internal fab*/
        newHuntFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.show();
            }
        });

        ConstraintLayout joinHuntDialog = findViewById(R.id.joinHuntDialogContainer);
        ProgressBar progressBar = findViewById(R.id.progressBarSearchHunt);
        progressBar.setVisibility(View.GONE);

        /*join hunt option*/
        joinHuntFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*prompt to insert code*/
                joinHuntDialog.setVisibility(View.VISIBLE);
                /*parentFab.shrink();*/
                newHuntFab.hide();
                newHuntText.setVisibility(View.GONE);
                joinHuntFab.hide();
                joinHuntText.setVisibility(View.GONE);
                isAllFabsVisible = false;

            }
        });


        Button searchHuntButton = findViewById(R.id.searchHuntButton);
        Button joinHuntButton = findViewById(R.id.joinHuntButton);
        TextView joinHuntCodeInput = findViewById(R.id.joinHuntCodeInput);
        /*join hunt button is initially disabled*/
        joinHuntButton.setEnabled(false);

        ImageButton closeDialogBtn = findViewById(R.id.closeJoinHuntDialogButton);
        closeDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*clear data*/
                joinHuntCodeInput.setText("");
                joinHuntButton.setEnabled(false);
                joinHuntButton.setBackgroundColor(Color.RED);
                joinHuntDialog.setVisibility(View.GONE);
                joinHuntCodeInput.setEnabled(true);
            }
        });



        searchHuntButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(joinHuntCodeInput.getText().toString().isEmpty()){
                    joinHuntCodeInput.setError("Empty code!");
                    joinHuntButton.setEnabled(false);
                    joinHuntButton.setBackgroundColor(Color.RED);
                    return;
                }else if(joinHuntCodeInput.getText().toString().length()!=10){
                    joinHuntCodeInput.setError("The code must be 10 characters long");
                    joinHuntButton.setEnabled(false);
                    joinHuntButton.setBackgroundColor(Color.RED);
                    return;
                }

                /*lock the input*/
                joinHuntCodeInput.setEnabled(false);
                joinHuntButton.setText("join");

                /*look it up on db*/
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setEnabled(true);
                db.collection("hunts").document(joinHuntCodeInput.getText().toString())
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            boolean isOngoing = documentSnapshot.getBoolean("isOngoing");
                            if(isOngoing){
                                /*disable spinner, enable join button*/
                                progressBar.setEnabled(false);
                                progressBar.setVisibility(View.INVISIBLE);
                                joinHuntButton.setBackgroundColor(Color.GREEN);
                                joinHuntButton.setEnabled(true);

                            }else{
                                Snackbar.make(view, R.string.hunt_over_message, Snackbar.LENGTH_LONG).show();
                                /*clear data*/
                                joinHuntCodeInput.setText("");
                                joinHuntButton.setEnabled(false);
                                joinHuntButton.setBackgroundColor(Color.RED);
                                joinHuntCodeInput.setEnabled(true);
                                return;
                            }

                        }else{
                            Snackbar.make(view, R.string.hunt_no_exist, Snackbar.LENGTH_LONG);
                        }
                    }
                });



            }
        });


        /*join hunt effectively
        *  send to fragment hunt if i can find a way*/
        joinHuntButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GlobalVariables.getInstance().setHuntInProgress(true);
                GlobalVariables.getInstance().setLastHuntCode(joinHuntCodeInput.getText().toString());
                joinHuntDialog.setVisibility(View.GONE);
                /*clear data*/
                joinHuntButton.setEnabled(false);
                joinHuntButton.setBackgroundColor(Color.RED);
                joinHuntCodeInput.setEnabled(true);
                /*send to hunt*/
                /*join on database*/
                db.collection("hunts").document(joinHuntCodeInput.getText().toString())
                        .update("participants", FieldValue.arrayUnion(currentUser.getEmail()));

                /*update user*/
                db.collection("users").document(mAuth.getCurrentUser().getEmail())
                        .update("isOnHunt", true);

                db.collection("users").document(mAuth.getCurrentUser().getEmail())
                        .update("lastHunt", joinHuntCodeInput.getText().toString());

                /*manage ui, make them visible*/
                mainTitle = findViewById(R.id.homeBigTitle);
                shareCodeButton = findViewById(R.id.shareCodeButton);
                leaveHuntButton = findViewById(R.id.leaveCurrentHuntButton);

                mainTitle.setText(R.string.home_huntInProgress);
                shareCodeButton.setVisibility(View.VISIBLE);
                leaveHuntButton.setVisibility(View.VISIBLE);


                joinHuntCodeInput.setText("");
                newHuntFab.setEnabled(false);
                joinHuntFab.setEnabled(false);
                newHuntFab.hide();
                newHuntText.setVisibility(View.GONE);
                joinHuntFab.hide();
                joinHuntText.setVisibility(View.GONE);
                parentFab.setBackgroundColor(Color.RED);
                isAllFabsVisible = false;

                updateUI();



            }
        });

        /*firebase listener. when user creates a hunt he's automatically */


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /*onBackPressed is overridden so that when the user presses the back button from the main activity it will exit the application*/
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finishAffinity();
    }


    private void updateUI(){
        if (GlobalVariables.getInstance().isHuntInProgress()) {
            /*parentFab.setEnabled(false);*/
            newHuntFab.setEnabled(false);
            joinHuntFab.setEnabled(false);
            parentFab.setBackgroundColor(Color.RED);
            parentFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "There is already a treasure hunt underway. Either abandon the current one or wait for" +
                            "it to finish!", BaseTransientBottomBar.LENGTH_LONG).show();

                }
            });
        } else {
            parentFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isAllFabsVisible) {
                        newHuntFab.show();
                        newHuntText.setVisibility(View.VISIBLE);
                        joinHuntFab.show();
                        joinHuntText.setVisibility(View.VISIBLE);
                        parentFab.extend();
                        isAllFabsVisible = true;
                    } else {
                        newHuntFab.hide();
                        newHuntText.setVisibility(View.GONE);
                        joinHuntFab.hide();
                        joinHuntText.setVisibility(View.GONE);
                        isAllFabsVisible = false;
                    }
                }
            });
        }
    }
}