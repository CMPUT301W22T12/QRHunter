package com.example.qrhunter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.qrhunter.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    FirebaseFirestore db;
    private GoogleMap map;
    private ActivityMapsBinding binding;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Boolean locationPermissions = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private UiSettings mUiSettings;
    ArrayList<location> markers;
    private LatLng currentL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getLocationPermission();
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
        map = googleMap;
        markers = new ArrayList<>();

        mUiSettings = map.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);

        if (locationPermissions) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = db.collection("QRcode");
        // Clear the old list
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable
                    FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(MapsActivity.this, "error in firebase" + error, Toast.LENGTH_SHORT).show();
                }
                markers.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        GeoPoint geo = doc.getGeoPoint("Location");
                        Double latitude = geo.getLatitude();
                        Double longitude = geo.getLongitude();
                        String score = String.valueOf(doc.getData().get("Score"));
                        location markerLocation = new location(latitude, longitude, score);
                        markers.add(markerLocation);
                    } catch (Exception exception) {
                        Toast.makeText(MapsActivity.this, "Some QRcodes do not contain location" + error, Toast.LENGTH_SHORT).show();
                    }
                }

                for (int i = 0; i < markers.size(); i++) {
                    location marker = markers.get(i);
                    String score = marker.getScore();
                    LatLng QR = new LatLng(marker.getLongitude(), marker.getLatitude());
                    String distance = getDistance(QR, currentL);

                    String info = "Score: " + score + " Distance: " + distance;
                    map.addMarker(new MarkerOptions().position(QR).title(info));
                }
            }
        });
    }

    private String getDistance(LatLng a, LatLng b) {
        double distance = SphericalUtil.computeDistanceBetween(a, b);
        return String.format("%.2f", distance / 1000) + "km";
    }

    /**
     * Call this function to get the current device location
     * @param
     * @return
     * https://developers.google.com/maps/documentation/android-sdk/location#runtime-permission
     */
    private void getDeviceLocation() {

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            currentL = new LatLng(location.getLatitude(), location.getLongitude());
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                                    location.getLongitude()), 13));
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                                    .zoom(15)                   // Sets the zoom
                                    .build();                   // Creates a CameraPosition from the builder
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    }
                });
    }

    /**
     * Call this function to get the permission form the user
     * @param
     * @return
     */
    private void getLocationPermission(){

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationPermissions = true;

            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissions = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            locationPermissions = false;
                            return;
                        }
                    }
                    locationPermissions = true;
                }
            }
        }
    }
}