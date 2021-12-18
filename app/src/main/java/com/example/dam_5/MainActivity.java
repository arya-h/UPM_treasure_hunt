package com.example.dam_5;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dam_5.utilities.GlobalVariables;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

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
import com.google.firebase.firestore.FirebaseFirestore;


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
    private TextView newHuntText, joinHuntText;
    private ExtendedFloatingActionButton parentFab;
    private boolean isAllFabsVisible;


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

        isAllFabsVisible = false;

        /*update value of isHuntInProgress on creation*/
        DocumentReference user =
        /*then add listener*/

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

                        parentFab.shrink();
                        isAllFabsVisible = false;
                    }
                }
            });
        }

        parentFab.shrink();



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

        /*join hunt option*/
        joinHuntFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
}