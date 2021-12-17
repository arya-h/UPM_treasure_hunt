package com.example.dam_5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NewHuntActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Integer radiusValue;
    private SeekBar seekBar;
    private EditText newHuntTitle;
    private EditText newHuntNumCoordinates;
    private MapView mapView;

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
                if(radiusValue==1){
                    Toast.makeText(getApplicationContext(), "small radius", Toast.LENGTH_SHORT).show();
                }else if(radiusValue==2){
                    Toast.makeText(getApplicationContext(), "medium radius", Toast.LENGTH_SHORT).show();
                }else if(radiusValue==3){
                    Toast.makeText(getApplicationContext(), "huge radius", Toast.LENGTH_SHORT).show();
                }
            }
        });

/*        *//*map component*//*
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);*/
        /*mapView = findViewById(R.id.map);
*//*        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)*//*
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);*/

        Button nextStepBtn = findViewById(R.id.buttonGoToMap);
        nextStepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewHuntMapActivity.class);
                startActivity(intent);
            }
        });

    }

    // Get a handle to the GoogleMap object and display marker.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }

}