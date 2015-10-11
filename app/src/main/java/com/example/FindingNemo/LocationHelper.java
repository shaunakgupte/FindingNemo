/*
 * Copyright 2015 Shaunak Gupte <gupte.shaunak@gmail.com>
 *
 * This file is part of Finding Nemo.
 *
 * Finding Nemo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Finding Nemo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Finding Nemo. If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.FindingNemo;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;

public class LocationHelper {

    private final int REQUEST_GPS = 0x103;
    private Activity activity;
    private LocationListener listener;
    private LocationManager locationManager;
    private static Location gpsLocation = null, networkLocation=null;

    public LocationHelper(Activity activity) {
        this.activity = activity;
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
    }

    public void setLocationListener(LocationListener listener) {
        this.listener = listener;
    }

    public void checkGPSStatus() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            DialogHelper.createDialog(activity, activity.getString(R.string.dialog_enable_gps), true,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_GPS);
                            }
                        }
                    }).show();
        }
    }

    public double[] getLastKnownLocation() {
        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        Location location;
        if(gpsLocation == null && networkLocation == null)
            return null;
        else if (gpsLocation == null)
            location = networkLocation;
        else if (networkLocation == null)
            location = gpsLocation;
        else {
            if (gpsLocation.getTime() > networkLocation.getTime() - 10000)
                location = networkLocation;
            else
                location = gpsLocation;
        }
        return new double[]{location.getLatitude(), location.getLongitude()};
    }
    public void enableLocation() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, networkListener);
    }

    public void disableLocation() {
        locationManager.removeUpdates(gpsListener);
        locationManager.removeUpdates(networkListener);
    }

    public boolean checkActivityResult(int requestCode) {
        if (requestCode == REQUEST_GPS) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                DialogHelper.createDialog(activity, activity.getString(R.string.dialog_enable_gps_again), true, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_GPS);
                        }
                    }
                }).show();
            }
            return true;
        }
        return false;
    }

    interface LocationListener {
        void onHaveLocation(double[] location);
    }
    private android.location.LocationListener gpsListener = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            gpsLocation = location;
            sendLocation();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    private android.location.LocationListener networkListener = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            networkLocation = location;
            sendLocation();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void sendLocation() {
        Location location;
        if(gpsLocation == null && networkLocation == null)
            return;
        else if (gpsLocation == null)
            location = networkLocation;
        else if (networkLocation == null)
            location = gpsLocation;
        else {
            if (gpsLocation.getTime() > networkLocation.getTime() - 10000)
                location = networkLocation;
            else
                location = gpsLocation;
        }
        listener.onHaveLocation(new double[]{location.getLatitude(), location.getLongitude()});
    }

    public static LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            return new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception ignored) {

        }
        return null;
    }

    public static String getCityAddress( JSONObject result){
        String addr= "";
        if( result.has("results") ){
            try {
                JSONArray array = result.getJSONArray("results");
                if( array.length() > 0 ){
                    JSONObject place = array.getJSONObject(0);
                    JSONArray components = place.getJSONArray("address_components");
                    for( int i = 0 ; i < components.length() ; i++ ){
                        JSONObject component = components.getJSONObject(i);
                        if(component.toString().contains("postal_code"))
                            continue;
                        if(component.toString().contains("country"))
                            continue;
                        if(component.toString().contains("administrative"))
                            continue;
                        addr = addr + component.getString("long_name") +", ";
                    }
                    addr=addr.substring(0, addr.length() - 2);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return addr;
    }
    public static JSONObject getLocationFormGoogle(String placesName) {

        String apiRequest = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + placesName; //+ "&ka&sensor=false"
        HttpGet httpGet = new HttpGet(apiRequest);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (Exception ignored){}

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {

            e.printStackTrace();
        }

        return jsonObject;
    }

}
