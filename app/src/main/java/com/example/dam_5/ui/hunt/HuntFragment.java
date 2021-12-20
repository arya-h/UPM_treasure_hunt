package com.example.dam_5.ui.hunt;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dam_5.MainActivity;
import com.example.dam_5.R;
import com.example.dam_5.databinding.FragmentHuntBinding;
import com.example.dam_5.utilities.NotificationHunt;
import com.example.dam_5.utilities.Pin;
import com.example.dam_5.utilities.GlobalVariables;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HuntFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    /*firebase*/
    private Map<Marker, Integer> checkClickMap;
    private FirebaseFirestore db;
    private String username;
    private FirebaseAuth mAuth;

    /* location */
    private FusedLocationProviderClient mFusedLocationClient;

    private ArrayList<Marker> listMarkers;
    private static final String TAG = "LISTENER";

    /*views*/
    private HuntViewModel huntViewModel;
    private FragmentHuntBinding binding;

    private boolean isOngoing;
    private TextView titleNoHunt;
    private ArrayList<Pin> chosenCoordinates;
    private Long numCoord;
    private TextView numCoordText;

    /*hunt data*/
    String randomTag;
    long radius;
    int radM;
    String huntTitle;
    private GoogleMap mMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        huntViewModel =
                new ViewModelProvider(this).get(HuntViewModel.class);


        randomTag = GlobalVariables.getInstance().getLastHuntCode();
        isOngoing = GlobalVariables.getInstance().isHuntInProgress();
        binding = FragmentHuntBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;


    }

    private String replaceString(String str) {

        str = str.replaceAll("([\\[a-zA-Z]+\\w+)", "\"$1\"");
        str = str.replaceAll("=", ":");

        return str;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        /*firebase instance*/
        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        db.collection("users").document(mAuth.getCurrentUser().getEmail())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    GlobalVariables.getInstance().setUsername(documentSnapshot.getString("username"));
                }
            }
        });

        checkClickMap = new HashMap<>();

        /*coordinates data*/
        chosenCoordinates = new ArrayList<>();
        listMarkers = new ArrayList<>();

        titleNoHunt = getView().findViewById(R.id.huntFragm_idle);

        if (!GlobalVariables.getInstance().isHuntInProgress()) {
            titleNoHunt.setVisibility(View.VISIBLE);
            titleNoHunt.setText(R.string.home_huntNot);
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        } else {
            /*request permission*/
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                /*if user still denies after request, go back to home page*/
            } else if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                /*make user go back to home page*/
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapHunt);
            mapFragment.getMapAsync(this);

            /*add a listener for changes in the currently followed hunt*/
            /*if changes are seen, notification appears*/

/*            db.collection("hunts")
                    .document(randomTag)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot docSnap,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("ERROR", "listen:error", e);
                                return;
                            }
                            if (docSnap.exists()) {
                                Object notifs = (Object) docSnap.get("notifications");
                                if(notifs()){
                                    return;
                                }
                                Object lastNotification = notifs.get(notifs.size() - 1);
                                Log.d("HUNT_NOTIF", lastNotification.toString());

                                JSONParser parser = new JSONParser();
                                try {
                                    Object now = parser.parse(replaceString(lastNotification.toString()));
                                    Log.d("HUNT_NOTIF", now.toString());
                                    JSONObject jObj = (JSONObject) now;
                                    *//*NotificationHunt not = new NotificationHunt(jObj.get("type").toString(), jObj.get("username").toString(), jObj.get("pinName").toString()); *//*
                                    String type = jObj.get("type").toString();
                                    String username = jObj.get("user").toString();
                                    String pinName = jObj.get("namePin").toString();

                                    Log.d("BUGFIX", "sender : "+username + " current user : " + GlobalVariables.getInstance().getUsername());

                                    if(username.equals(GlobalVariables.getInstance().getUsername())){
                                        *//*dont need to notify the user of itself*//*
                                        return;
                                    }else{
                                        Snackbar.make(getView(), "type :"+type + " - username : " + username + " - pinName = " + pinName, Snackbar.LENGTH_LONG).show();
                                    }



                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });*/
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);
        /*to query last position*/
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setMyLocationEnabled(true);

        numCoordText = getView().findViewById(R.id.numCoord);

        /*retrieve information about hunt*/
        randomTag = GlobalVariables.getInstance().getLastHuntCode();
        db.collection("hunts").document(randomTag).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                numCoord = (long) document.get("numCoordinates");
                                numCoordText.setText(numCoord.toString());
                                List<Object> arr = (List<Object>) document.get("coordinates");
                                Log.d("HUNTMAP", arr.toString());

                                assert arr != null;
                                for (Object obj : arr) {
                                    Log.d("HUNTMAP", obj.toString());
                                    Log.d("HUNTMAP", replaceString(obj.toString()));
                                    JSONParser parser = new JSONParser();
                                    try {
                                        Object tmp = parser.parse(replaceString(obj.toString()));
                                        JSONObject jObj = (JSONObject) tmp;
                                        /*coordinates first*/
                                        String fullCoord = (jObj.get("coordinates").toString());
                                        Log.d("HUNTMAP", "FULLCOORD " + fullCoord);
                                        Object fullCoordObj = parser.parse(fullCoord);
                                        JSONObject jCoordObj = (JSONObject) fullCoordObj;
                                        Log.d("HUNTMAP", "riga 120 " + jCoordObj.get("latitude"));
                                        LatLng pos = new LatLng(Double.parseDouble(jCoordObj.get("latitude").toString()),
                                                Double.parseDouble(jCoordObj.get("longitude").toString()));
                                        String posTitle = jObj.get("title").toString();
                                        Double lat = Double.parseDouble(jCoordObj.get("latitude").toString());
                                        Double longi = Double.parseDouble(jCoordObj.get("longitude").toString());
                                        Log.d("HUNTMAP", "DAIII " + lat + "-" + longi + " - " + posTitle);


                                        Pin pinTmp = new Pin(new LatLng(lat, longi), posTitle);

                                        chosenCoordinates.add(pinTmp);

                                        Log.d("HUNTMAP", chosenCoordinates.toArray().toString());

                                        /*retrieve title*/
                                        huntTitle = document.getString("title");
                                        radius = (long) document.get("radius");

                                        /*radius in metres*/
                                        if (radius == 1) {
                                            radM = 20;
                                        } else if (radius == 2) {
                                            radM = 50;
                                        } else if (radius == 3) {
                                            radM = 100;
                                        }

                                        /*put markers on map*/


                                        for (Pin p : chosenCoordinates) {
                                            Log.d("HUNTMAP", p.coordinates.toString());

                                            checkClickMap.put(mMap.addMarker(new MarkerOptions().position(p.getCoordinates()).title(p.getTitle())),2);
                                            mMap.addCircle(new CircleOptions()
                                                    .center(p.getCoordinates())
                                                    .radius(radM))
                                                    .setStrokeColor(Color.RED);

                                        }

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                }

                            }
                        }
                    }
                });




        /*handle clicks on circles*/

    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.d("LOCATION_NOW", marker.getPosition().toString());

                double latC = marker.getPosition().latitude;
                double longC = marker.getPosition().longitude;



                Log.d("LOCATION_NOW", marker.getPosition().toString());

                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(@NonNull Location location) {
                        /*if the distance from the centre is less than the radius, inside*/
                        Log.d("LOCATION_NOW", location.getLatitude() + " - " + location.getLongitude());


                        float[] flDist = new float[4];
                        Location.distanceBetween(latC, longC, location.getLatitude(), location.getLongitude(), flDist);

                        if (flDist[0] < radM && !listMarkers.contains(marker)) {
                            /*if (listMarkers.contains(marker)) {
                                Snackbar.make(getView(), "you already clicked here", Snackbar.LENGTH_LONG).show();
                                return;
                            }*/
                            if(checkClickMap.containsKey(marker)){
                                int val = checkClickMap.get(marker);
                                val--;
                                checkClickMap.replace(marker, val);
                                if(val==0){
                                    numCoord--;
                                    marker.remove();
                                    numCoordText.setText(numCoord.toString());
                                }
                            }
                            Snackbar.make(getView(), "You're inside!", Snackbar.LENGTH_SHORT).show();
                            /*save that this user has registered*/
                            /*change the marker image or remove it directly??*/
                                                      /*if it was the last pin*/


                            if (numCoord == 0) {
                                /*send notification that hunt has finished*/
                                /*create map for notification*/
                                Map<String, String> notif = new HashMap<>();
                                notif.put("user", GlobalVariables.getInstance().getUsername());
                                notif.put("type", "finishedHunt");
                                notif.put("namePin", "");
                                /*update db, will then send notification to other users*/
                                db.collection("hunts").document(randomTag)
                                        .update("notifications", notif);

                                db.collection("hunts").document(randomTag)
                                        .update("isOngoing", false);
                            } else {
                                /*create map for notification*/
                                Map<String, String> notif = new HashMap<>();
                                notif.put("user", GlobalVariables.getInstance().getUsername());
                                notif.put("type", "discoveredPin");
                                notif.put("namePin", marker.getTitle());
                                /*update db, will then send notification to other users*/
                                db.collection("hunts").document(randomTag)
                                        .update("notifications", notif);


                            }
                        } else {
                            /*otherwise i'm outside*/
                            Snackbar.make(getView(), "You're way too far to register for this pin :" + flDist[0], Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
        return false;

            }

        }




            /*center of circle is*/



/*        }*/

    /*}*/

