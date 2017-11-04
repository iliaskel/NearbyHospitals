package com.example.android.nearbyhospitals.Utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ikelasid on 10/20/2017.
 */

public class GetNearbyPlacesData extends AsyncTask<Object,String,String> {

    private static final String TAG = "GetNearbyPlacesData";
    String googlePlacesData;
    GoogleMap mMap;
    String url;


    @Override
    protected String doInBackground(Object... params) {

        mMap=(GoogleMap)params[0];
        url=(String)params[1];

        HttpResponse httpResponse =new HttpResponse();
        try {
           googlePlacesData= httpResponse.getResponseFromHttpRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "doInBackground: "+googlePlacesData);
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String,String>> nearbyPlacesList=null;
        DataParser parser=new DataParser();
        nearbyPlacesList=parser.parse(s);
        showNearbyPlaces(nearbyPlacesList);
    }


    private void showNearbyPlaces(List<HashMap<String,String>> nearbyPlaceList){


        for (int i=0;i<nearbyPlaceList.size();i++){

            MarkerOptions markerOptions=new MarkerOptions();
            HashMap<String,String> googlePlace=nearbyPlaceList.get(i);

            String placeName=googlePlace.get("place_name");
            String vicinity=googlePlace.get("vicinity");

            double lat=Double.parseDouble(googlePlace.get("lat"));
            double lng=Double.parseDouble(googlePlace.get("lng"));

            LatLng latLng=new LatLng(lat,lng);

            markerOptions.position(latLng);
            markerOptions.title(placeName+" : "+vicinity);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker());

            mMap.addMarker(markerOptions);
        }



    }

}
