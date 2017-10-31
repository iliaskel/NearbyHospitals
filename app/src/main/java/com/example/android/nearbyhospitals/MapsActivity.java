package com.example.android.nearbyhospitals;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.android.nearbyhospitals.Utils.GetNearbyPlacesData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private String mCurrentLatString;
    private String mCurrentLngString;
    private String mSearchType;
    private int RADIUS = 50000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        extractCurrentLocation(intent);
        if (intent.hasExtra("SEARCH_TYPE"))
            mSearchType = intent.getStringExtra("SEARCH_TYPE");


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private String buildSearchUrl(String searchType) {
        StringBuilder googleNearbyPlacesSearchUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleNearbyPlacesSearchUrl.append("location=" + mCurrentLatString + "," + mCurrentLngString);
        googleNearbyPlacesSearchUrl.append("&radius=" + RADIUS);
        googleNearbyPlacesSearchUrl.append("&type=" + searchType);
        googleNearbyPlacesSearchUrl.append("&key=AIzaSyDYnHFQJKhj4Mjg2ur6jEtmFTl3IOrVZKs");

        Log.d(TAG, "buildSearchUrl: " + googleNearbyPlacesSearchUrl.toString());
        return googleNearbyPlacesSearchUrl.toString();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Object[] dataToTransfer = new Object[2];
        dataToTransfer[0] = mMap;
        dataToTransfer[1] = buildSearchUrl(mSearchType);

        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        getNearbyPlacesData.execute(dataToTransfer);

    }


    private void extractCurrentLocation(Intent intent) {
        if(intent.hasExtra("CURRENT_LONGITUDE"))
            mCurrentLngString=intent.getStringExtra("CURRENT_LONGITUDE");
        if(intent.hasExtra("CURRENT_LATITUDE"))
            mCurrentLatString=intent.getStringExtra("CURRENT_LATITUDE");
    }

}
