package com.example.dam_5;

import static android.view.View.GONE;

import androidx.annotation.NonNull;

import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.fragment.app.FragmentActivity;


import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/*google maps*/
import com.example.dam_5.utilities.GlobalVariables;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.dam_5.databinding.ActivityNewHuntMapBinding;
/*firebase*/
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import com.example.dam_5.utilities.Pin;

public class NewHuntMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityNewHuntMapBinding binding;
    private Button submitHuntBtn;
    private TextView huntTitleText;
    private TextView remainingCoordinates;
    private LatLng userLatLng;
    private EditText titlePinInput;

    /*layouts for background colors*/
    private ConstraintLayout containerDialogTitlePin;
    private ConstraintLayout containerSubmitButton;

    /*information received through intent*/
    private Integer radius;
    private Integer numCoord;
    private String huntTitle;
    /*dialog to insert new pin*/
    private Button dialogCancelButton;
    private Button dialogAddPinButton;
    private EditText inputTitlePin;
    private ProgressBar progressBar;

    /*firebase*/
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference hunts;




    /*selected coordinates*/
    private LinkedList<Pin> chosenCoordinates;
    int radM; /*radius in metres*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewHuntMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapV);
        mapFragment.getMapAsync(this);

        /*retrieval of intent extras*/

        Intent intent = getIntent();
        huntTitle = intent.getStringExtra("title");
        radius = intent.getIntExtra("radius", 2); //default value is 2, medium radius
        numCoord = intent.getIntExtra("numCoordinates", 3);

        /*set the values*/
        containerSubmitButton = findViewById(R.id.containerSubmitButton);
        submitHuntBtn = findViewById(R.id.submitHuntButton);
        huntTitleText = findViewById(R.id.huntTitle);
        remainingCoordinates = findViewById(R.id.remainingCoordinates);

        Log.d("INTENT_VALUES", "title:" + huntTitle + " radius : " + radius + " numCoord :" + numCoord);
        huntTitleText.setText(huntTitle);
        remainingCoordinates.setText(numCoord.toString());
        containerSubmitButton.setBackgroundResource(R.drawable.gradient_button_hunt_disabled);

        /*overlay init*/
        ConstraintLayout overlay = findViewById(R.id.overlayLayer);
        overlay.setVisibility(GONE);

        /*initially complete button is disabled for obvious reasons*/
        submitHuntBtn.setEnabled(false);
        //*click on finish button*/

        /*when user clicks on map, he's prompted to select a name for the pin*/
        /*titlePinInput = findViewById(R.id.titlePinInput);*/

        /*dialog per inserire nome*/
        containerDialogTitlePin = findViewById(R.id.containerDialogTitlePin);
        containerDialogTitlePin.setVisibility(GONE);

        /*initialize list*/
        chosenCoordinates = new LinkedList<>();

        userLatLng = new LatLng(intent.getDoubleExtra("latitude", 40), intent.getDoubleExtra("longitude", -3));

        dialogAddPinButton = findViewById(R.id.dialogAddPinButton);
        dialogCancelButton = findViewById(R.id.dialogCancelButton);
        inputTitlePin = findViewById(R.id.inputTitlePin);

        /*firebase instance*/
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        /*mMap.addMarker(new MarkerOptions().position(userLatLng).title("Marker in Sydney"));*/
        mMap.setMyLocationEnabled(true);

        /*to move camera on user position*/
        CameraUpdate center = CameraUpdateFactory.newLatLng(userLatLng);
        CameraUpdate zoom = CameraUpdateFactory.zoomBy(10);


/*        mMap.setMinZoomPreference(11.0f);
        mMap.setMaxZoomPreference(1.0f);*/
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));
        mMap.moveCamera(center);

        /*circle radius is defined in metres.*/
        /*let's say that
         * radius = 3 : 100m
         *        = 2 : 50m
         *        = 1 : 20m
         * */

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                /*dont let user put more pins if already done*/
                if (numCoord == 0) {
                    return;
                }

                containerDialogTitlePin.setVisibility(View.VISIBLE);

                /*dialog disappears and text is cleared*/
                dialogCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        containerDialogTitlePin.setVisibility(View.GONE);
                        inputTitlePin.setText("");
                    }
                });

                /*when user clicks on map, he's presented with the possibility to
                 * add a pin with a title. there shouldnt be two pins with the same name, but i'll use coordinates as key
                 * if i will implement deletion of pins subsequently*/

                dialogAddPinButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (inputTitlePin.getText().toString().isEmpty()) {
                            inputTitlePin.setError("No name chosen");
                            return;
                        } else {

                            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Service.INPUT_METHOD_SERVICE);

                            if (radius == 1) {
                                radM = 20;
                            } else if (radius == 2) {
                                radM = 50;
                            } else if (radius == 3) {
                                radM = 100;
                            }

                            /*add marker to map*/
                            mMap.addMarker(new MarkerOptions().position(latLng).title(inputTitlePin.getText().toString()));
                            mMap.addCircle(new CircleOptions()
                                    .center(latLng)
                                    .clickable(true)
                                    .radius(radM))
                                    .setStrokeColor(Color.RED);
                            /*add to local structure*/
                            chosenCoordinates.add(new Pin(latLng, inputTitlePin.getText().toString()));
                            containerDialogTitlePin.setVisibility(GONE);
                            numCoord--;
                            remainingCoordinates.setText(numCoord.toString());
                            inputTitlePin.setText("");
                            /*hide keyboard after pinned*/
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


                            /*if all the pins have been positioned*/
                            if (numCoord == 0) {
                                /*enable submit button*/
                                containerSubmitButton.setBackgroundResource(R.drawable.gradient_button_hunt);
                                submitHuntBtn.setEnabled(true);
                            }

                        }
                    }
                });
            }
        });

        /*manage click on submit hunt button when all the pins have been set*/
        /*send to firebase*/
        submitHuntBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*add overlay spinner*/
                ConstraintLayout overlay = findViewById(R.id.overlayLayer);
                overlay.setVisibility(View.VISIBLE);
                overlay.bringToFront();
                overlay.setEnabled(false);
                progressBar = findViewById(R.id.loadingSpinner);

                /*generate random string to share with friends code*/
                String randomTag = randomString();
                Map<String, Object> value = new HashMap<>();
                /*arr of participants, filled with creator first*/
                ArrayList<String> part = new ArrayList<>();
                part.add(currentUser.getEmail());
                value.put("participants",part);
                value.put("title", huntTitle);
                value.put("coordinates", chosenCoordinates);
                value.put("creator", currentUser.getEmail());
                value.put("numCoordinates", getIntent().getIntExtra("numCoordinates", 3));
                value.put("radius", radius);
                value.put("isOngoing", true);
                /*add to db*/
                hunts = db.collection("hunts");
                hunts.document(randomTag).set(value)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                /*Timer timer = new Timer();
                                *//*simulate loading*//*
                                Log.d("YO", "DocumentSnapshot successfully written!");


                                getCallingActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                                                   timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        overlay.setVisibility(GONE);
                                    }
                                }, 2000);
                                    }
                                });*/
                                Thread thread = new Thread(){
                                    @Override
                                    public void run() {
                                        try {
                                            synchronized (this) {
                                                wait(2000);

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        overlay.setVisibility(GONE);
                                                    }
                                                });

                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                         /*               Intent mainActivity = new Intent(getApplicationContext(),MainActivity.class);
                                        startActivity(mainActivity);*/
                                    };
                                };
                                thread.start();

                                /*put user in hunt directly*/
                                DocumentReference user = db.collection("users").document(currentUser.getEmail());
                                user.update("isOnHunt", true);
                                user.update("lastHunt", randomTag);
                                /*prompt him to share code with friends*/
                                GlobalVariables.getInstance().setHuntInProgress(true);
                                GlobalVariables.getInstance().setLastHuntCode(randomTag);

                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, randomTag);
                                sendIntent.setType("text/plain");

                                Intent shareIntent = Intent.createChooser(sendIntent, "Hunt Code");
                                startActivity(shareIntent);

                                /*after this he'll be on the hunt page, ready to start*/
                                /*the flag makes sure that back presses dont behave weirdly*/
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("code", randomTag);
                                intent.putExtra("fragmentNumber",1);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("YO", "Error writing document", e);
                            }
                        });


            }
        });


    }

    private String randomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }


}