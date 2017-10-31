package com.example.android.nearbyhospitals;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by ikelasid on 10/19/2017.
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CHECK_SETTINGS =1001 ;

    private ImageView mHospitalImageView;
    private ImageView mPharmacyImageView;
    private TextView mHospitalTextView;
    private TextView mPharmacyTextview;
    private ProgressBar mProgressBar;

    //vars
    private boolean mManualLocationBoolean=false;
    private boolean mLocationPermissionsGranted =false;
    private String FINE_LOCATION=Manifest.permission.ACCESS_FINE_LOCATION;
    private String COARSE_LOCATION=Manifest.permission.ACCESS_COARSE_LOCATION;
    private final int LOCATION_PERMISSION_CODE=10;

    private static final int ERROR_DIALOG_REQUEST=9001;

    private Location mManualLocation =new Location("");
    private Location mLastKnownLocation =new Location("");
    private String mCurrentLatitude;
    private String mCurrentLongitude;
    private FusedLocationProviderClient mFusedLocationProviderClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //checking google play services version//availability
        isServicesOK();

        findViesById();

        //making progress bar visible
        setUpProgressBarVisibility();

        // Get permissions && location
        getLocationPermissions();



    }


    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");
        int available= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available== ConnectionResult.SUCCESS){
            Log.d(TAG, "isServicesOK: google play services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "isServicesOK: google play servises isnt working but we can fix it");
            Dialog dialog=GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else{
            Log.d(TAG, "isServicesOK: Google play services isn't and won't work. You can't make map requests");
        }
        return false;

    }

    private void setListeners() {
        Log.d(TAG, "setListeners: entered");
        if (mManualLocationBoolean) {
            mCurrentLatitude = String.valueOf(mManualLocation.getLatitude());
            mCurrentLongitude = String.valueOf(mManualLocation.getLongitude());
        }
        else{
            mCurrentLatitude = String.valueOf(mLastKnownLocation.getLatitude());
            mCurrentLongitude = String.valueOf(mLastKnownLocation.getLongitude());
        }
        mHospitalImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Hospitals",Toast.LENGTH_LONG).show();
                Intent mapsIntent=new Intent(MainActivity.this,MapsActivity.class);
                mapsIntent.putExtra("CURRENT_LONGITUDE",mCurrentLongitude);
                mapsIntent.putExtra("CURRENT_LATITUDE",mCurrentLatitude);
                mapsIntent.putExtra("SEARCH_TYPE","hospital");
                startActivity(mapsIntent);
            }
        });
        mPharmacyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Pharmacies",Toast.LENGTH_LONG).show();
                Intent mapsIntent=new Intent(MainActivity.this,MapsActivity.class);
                mapsIntent.putExtra("CURRENT_LONGITUDE",mCurrentLongitude);
                mapsIntent.putExtra("CURRENT_LATITUDE",mCurrentLatitude);
                mapsIntent.putExtra("SEARCH_TYPE","pharmacy");
                startActivity(mapsIntent);
            }
        });
    }

    private void getLocationPermissions(){
        String[] permissions={FINE_LOCATION,COARSE_LOCATION};
        // IF you don't have permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //then request permissions
            Log.d(TAG, "getLocationPermissions: asking for permissions");
                    ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_CODE);
            }
            //Otherwise // if you have permissions
            else{
            Log.d(TAG, "getLocationPermissions: got permissions");
            mLocationPermissionsGranted=true;
            getDeviceLastKnownLocation();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: got results");
        switch (requestCode){
            case LOCATION_PERMISSION_CODE:
                if(grantResults.length>0){
                    for(int i =0 ; i<grantResults.length; i++){
                        if(grantResults[i]!=PackageManager.PERMISSION_GRANTED )
                            mLocationPermissionsGranted=false;
                        return;
                    }
                    mLocationPermissionsGranted=true;
                    getDeviceLastKnownLocation();
                }
        }

    }


    // Trying to get LastKnownLocation || Setting manual location if LastKnownLocation is null
    private void getDeviceLastKnownLocation() {
        Log.d(TAG, "getDeviceLastKnownLocation: getting the device's current  location");

        mFusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this);

        try {
            if(mLocationPermissionsGranted){
                Task location= mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: Found location");
                            mLastKnownLocation=(Location)task.getResult();
                            if(mLastKnownLocation ==null){
                                showToast("Unable to get Last Location. Setting up location manually (Church of Saint Sofia, Thessaloniki)");
                                mManualLocationBoolean=true;
                                mManualLocation.setLatitude(40.632828);
                                mManualLocation.setLongitude(22.946963);
                                setListeners();
                                setUpProgressBarVisibility();
                            }
                            else{
                                Log.d(TAG, "coords "+ mLastKnownLocation.toString());
                                setListeners();
                                setUpProgressBarVisibility();
                            }
                        }
                        else {
                            Log.d(TAG, "onComplete: Unable to find location//Location==null");
                            showToast("Couldn't find current location");
                        }
                    }
                });
            }
            else{
                showToast("You don't have location permissions.Please give permission.");
                getLocationPermissions();
            }
        }
        catch (SecurityException e){
            Log.d(TAG, "getDeviceLastKnownLocation: Security Exception " + e.getMessage());;
        }
    }

    private void showToast(String s){
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
    }



    private void setUpProgressBarVisibility() {
        if(mProgressBar.getVisibility()==View.VISIBLE){
            Log.d(TAG, "setUpProgressBarVisibility: VISIBLE");
            mProgressBar.setVisibility(View.INVISIBLE);
            mHospitalImageView.setClickable(true);
            mPharmacyImageView.setClickable(true);
        }
        else{
            Log.d(TAG, "setUpProgressBarVisibility: INVISIBLE");
            mProgressBar.setVisibility(View.VISIBLE);
            mHospitalImageView.setClickable(false);
            mPharmacyImageView.setClickable(false);
        }
    }

    private void findViesById() {
        mHospitalImageView = (ImageView) findViewById(R.id.image_hospital);
        mPharmacyImageView = (ImageView) findViewById(R.id.image_pharmacy);
        mHospitalTextView = (TextView) findViewById(R.id.tv_hospital);
        mPharmacyTextview = (TextView) findViewById(R.id.tv_pharmacy);
        mProgressBar=(ProgressBar)findViewById(R.id.progressBar);
    }
}
