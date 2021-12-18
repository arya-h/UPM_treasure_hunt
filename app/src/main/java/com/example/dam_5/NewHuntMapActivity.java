package com.example.dam_5;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.dam_5.databinding.ActivityNewHuntMapBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.LinkedList;

public class NewHuntMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityNewHuntMapBinding binding;
    private Button addCoordBtn;
    private TextView huntTitleText;
    private TextView remainingCoordinates;
    private LatLng userLatLng;
    private AlertDialog.Builder builder;
    private EditText titlePinInput;

    private ConstraintLayout containerDialogTitlePin;

    private Integer radius;
    private Integer numCoord;

    /*dialog to insert new pin*/
    Button dialogCancelButton;
    Button dialogAddPinButton;
    EditText inputTitlePin;

    /*chosen coordinates*/
    private class Pin{
        LatLng coordinates;
        String title;

        protected Pin(LatLng coordinates, String title){
            this.coordinates = coordinates;
            this.title = title;
        }
    }
    private LinkedList<Pin> chosenCoordinates;


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
        String huntTitle = intent.getStringExtra("title");
        radius = intent.getIntExtra("radius", 2); //default value is 2, medium radius
        numCoord = intent.getIntExtra("numCoordinates", 1);

        /*set the values*/

        addCoordBtn = findViewById(R.id.addCoordinatesButton);
        huntTitleText = findViewById(R.id.huntTitle);
        remainingCoordinates = findViewById(R.id.remainingCoordinates);

        Log.d("INTENT_VALUES", "title:" + huntTitle + " radius : " + radius + " numCoord :" + numCoord);
        huntTitleText.setText(huntTitle);
        remainingCoordinates.setText(numCoord.toString());

        /*initially complete button is disabled for obvious reasons*/
        addCoordBtn.setEnabled(false);
        //*click on finish button*/
        addCoordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        /*when user clicks on map, he's prompted to select a name for the pin*/
        /*titlePinInput = findViewById(R.id.titlePinInput);*/

        /*dialog per inserire nome*/
        containerDialogTitlePin = findViewById(R.id.containerDialogTitlePin);
        containerDialogTitlePin.setVisibility(GONE);

        /*initialize list*/
        chosenCoordinates = new LinkedList<>();

        userLatLng = new LatLng( intent.getDoubleExtra("latitude", 40), intent.getDoubleExtra("longitude", -3));

        dialogAddPinButton = findViewById(R.id.dialogAddPinButton);
        dialogCancelButton = findViewById(R.id.dialogCancelButton);
        inputTitlePin = findViewById(R.id.inputTitlePin);

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
        mMap.addMarker(new MarkerOptions().position(userLatLng).title("Marker in Sydney"));
        mMap.setMyLocationEnabled(true);

        /*to move camera on user position*/
        CameraUpdate center = CameraUpdateFactory.newLatLng(userLatLng);
        CameraUpdate zoom = CameraUpdateFactory.zoomBy(10);


/*        mMap.setMinZoomPreference(11.0f);
        mMap.setMaxZoomPreference(1.0f);*/
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10 ));
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


                containerDialogTitlePin.setVisibility(View.VISIBLE);

                /*dialog disappears and text is cleared*/
                dialogCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        containerDialogTitlePin.setVisibility(View.GONE);
                        inputTitlePin.setText("");
                    }
                });

                dialogAddPinButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (inputTitlePin.getText().toString().isEmpty()) {
                            inputTitlePin.setError("No name chosen");
                            return;
                        } else {

                            int radM = 100;
                            switch (radius) {
                                case 1:
                                    radM = 20;
                                    break;
                                case 2:
                                    radM = 50;
                                    break;
                                case 3:
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

                        }
                    }
                });
                }
        });
    }

    private class PurchaseConfirmationDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(requireContext())
                    .setMessage("okkk")
                    .setPositiveButton("va beneeee", (dialog, which) -> {} )
                    .create();
        }
    }


}