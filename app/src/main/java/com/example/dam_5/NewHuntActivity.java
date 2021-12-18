package com.example.dam_5;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dam_5.utilities.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class NewHuntActivity extends AppCompatActivity {

    private Integer radiusValue;
    private SeekBar seekBar;
    private EditText newHuntTitle;
    private EditText newHuntNumCoordinates;
    private LatLng lastKnownPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationManager locationManager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_hunt);

        /*title*/
        EditText newHuntTitle = findViewById(R.id.huntNameInput);
        /*number of coordinates*/
        EditText newHuntNumCoordinates = findViewById(R.id.huntNumCoordinatesInput);
        /*radius*/
        SeekBar seekBar = findViewById(R.id.seekBarRadius);

        seekBar.setMax(3);
        seekBar.setMin(1);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radiusValue = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (radiusValue == 1) {
                    Toast.makeText(getApplicationContext(), "small radius", Toast.LENGTH_SHORT).show();
                } else if (radiusValue == 2) {
                    Toast.makeText(getApplicationContext(), "medium radius", Toast.LENGTH_SHORT).show();
                } else if (radiusValue == 3) {
                    Toast.makeText(getApplicationContext(), "huge radius", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Button confirmChoiceBtn = findViewById(R.id.confirmChoicesButton);
        confirmChoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Service.INPUT_METHOD_SERVICE);
                if (newHuntNumCoordinates.getText().toString().isEmpty()) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    Snackbar.make(view, "Empty number of coordinates", Snackbar.LENGTH_LONG).show();
                    return;
                }
                int numCoord = Integer.parseInt(newHuntNumCoordinates.getText().toString());
                if (numCoord < 1 || numCoord > 5) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    Snackbar.make(view, "Invalid number of coordinates. Must be between 1 and 5", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (newHuntTitle.getText().toString().isEmpty()) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    Snackbar.make(view, "Title is empty!", Snackbar.LENGTH_LONG).show();
                    return;
                }else if(lastKnownPosition == null){
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    Snackbar.make(view, "Still querying for your last position, wait a second...", Snackbar.LENGTH_LONG).show();
                    return;
                }

                else {
                    /*send to map activity*/
                    Intent intent = new Intent(getApplicationContext(), NewHuntMapActivity.class);
                    intent.putExtra("radius", radiusValue);
                    intent.putExtra("numCoordinates", numCoord);
                    intent.putExtra("title", newHuntTitle.getText().toString());
                    /*last known location*/
                    intent.putExtra("latitude", lastKnownPosition.latitude);
                    intent.putExtra("longitude", lastKnownPosition.longitude);

                    startActivity(intent);
                }

            }
        });

        /*check if permission has been granted*/


            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            }else{

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        lastKnownPosition = new LatLng(location.getLatitude(), location.getLongitude());
                        Log.d("LOCATION_SERVICE",location.getLatitude() + " - " + location.getLongitude() );

                    }else{
                        Toast.makeText(getApplicationContext(), "Location services must be turned on", Toast.LENGTH_LONG).show();
                    }
                });


            }
            }

            }



