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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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

public class HuntFragment extends Fragment implements OnMapReadyCallback {

    /*firebase*/
    private FirebaseFirestore db;
    private String username;
    /*private FirebaseAuth mAuth;*/

    /* location */
    private FusedLocationProviderClient mFusedLocationClient;

    private static final String TAG = "LISTENER";

    /*views*/
    private HuntViewModel huntViewModel;
    private FragmentHuntBinding binding;

    private boolean isOngoing;
    private TextView titleNoHunt;
    private ArrayList<Pin> chosenCoordinates;
    private int numCoord;

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

        /*coordinates data*/
        chosenCoordinates = new ArrayList<>();

        titleNoHunt = getView().findViewById(R.id.huntFragm_idle);

        if (!GlobalVariables.getInstance().isHuntInProgress()) {
            titleNoHunt.setVisibility(View.VISIBLE);
            titleNoHunt.setText(R.string.home_huntNot);
            Intent intent= new Intent(getContext(), MainActivity.class);
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

            db.collection("hunts")
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
                                ArrayList<Object> notifs = (ArrayList<Object>) docSnap.get("notifications");
                                Object lastNotification = notifs.get(notifs.size() - 1);

                                JSONParser parser = new JSONParser();
                                try {
                                    Object now = parser.parse(replaceString(lastNotification.toString()));
                                    JSONObject jObj = (JSONObject) now;
                                    /*NotificationHunt not = new NotificationHunt(jObj.get("type").toString(), jObj.get("username").toString(), jObj.get("pinName").toString()); */
                                    String type = jObj.get("type").toString();
                                    String username = jObj.get("username").toString();
                                    String pinName = jObj.get("pinName").toString();

                                    Snackbar.make(getView(), "type :"+type + " - username : " + username + " - pinName = " + pinName, Snackbar.LENGTH_LONG).show();


                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
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

        /*to query last position*/
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setMyLocationEnabled(true);

        /*retrieve information about hunt*/
        randomTag = GlobalVariables.getInstance().getLastHuntCode();
        db.collection("hunts").document(randomTag).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
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
                                        numCoord = chosenCoordinates.size();

                                        for (Pin p : chosenCoordinates) {
                                            Log.d("HUNTMAP", p.coordinates.toString());

                                            mMap.addMarker(new MarkerOptions().position(p.getCoordinates()).title(p.getTitle()));
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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                /*determine whether user is inside the circle*/
                /*get location precise*/

                /*center of circle is*/

                double latC = marker.getPosition().latitude;
                double longC = marker.getPosition().longitude;

                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(@NonNull Location location) {
                        /*if the distance from the centre is less than the radius, inside*/
                        Log.d("LOCATION_NOW", location.getLatitude() + " - " + location.getLongitude());

                        float[] flDist = new float[4];
                        Location.distanceBetween(latC, longC, location.getLatitude(), location.getLongitude(), flDist);
                       /* Log.d("LOCATION", "Distance, whole array : " + flDist);
                        Log.d("LOCATION", "Distance : " + flDist[0]);

                        Log.d("LOCATION", "marker : " + latC + " - " + longC);*/


                        if (flDist[0] < radM) {
                            Snackbar.make(getView(), "You're inside!", Snackbar.LENGTH_SHORT).show();
                            /*save that this user has registered*/
                            /*change the marker image?*/
                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                            /*if it was the last pin*/
                            numCoord--;
                            if (numCoord == 0) {
                                /*send notification that hunt has finished*/
                                /*create map for notification*/
                                Map<String, String> notif = new HashMap<>();
                                notif.put("user", GlobalVariables.getInstance().getUsername());
                                notif.put("type", "finishedHunt");
                                notif.put("pinName", "");
                                /*update db, will then send notification to other users*/
                                db.collection("hunts").document(randomTag)
                                        .update("notifications", FieldValue.arrayUnion(notif));

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
                                        .update("notifications", FieldValue.arrayUnion(notif));

                            }
                        } else {
                            /*otherwise i'm outside*/
                            Snackbar.make(getView(), "You're way too far to register for this pin :" + flDist[0], Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });


                return false;
            }
        });


    }
}