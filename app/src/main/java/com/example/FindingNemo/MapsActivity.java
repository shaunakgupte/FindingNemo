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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

public class MapsActivity extends Activity {

    private Marker marker;
    private Intent returnIntent = new Intent();
    private GoogleMap map;
    private EditText etLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_maps);

        LatLng latLng = new LatLng(getIntent().getDoubleExtra(Constants.INTENT_EXTRA_LATITUDE, 0),
                getIntent().getDoubleExtra(Constants.INTENT_EXTRA_LONGITUDE, 0));

        etLocation = (EditText) findViewById(R.id.etLocation);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setLocation(latLng);
            }
        });

        View locationButton = ((View)findViewById(1).getParent()).findViewById(2);
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        rlp.setMargins(30, 30, 30, 30);

        setLocation(latLng);
    }

    private void setLocation(final LatLng latLng) {
        if (latLng == null)
            return;
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng), 1000, null);
        if (marker != null)
            marker.remove();
        marker = map.addMarker(new MarkerOptions().position(latLng));
        returnIntent.putExtra(Constants.INTENT_EXTRA_LATITUDE, latLng.latitude);
        returnIntent.putExtra(Constants.INTENT_EXTRA_LONGITUDE, latLng.longitude);

        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject result = LocationHelper.getLocationFormGoogle(latLng.latitude + "," +
                        latLng.longitude);
                final String address = LocationHelper.getCityAddress(result);
                returnIntent.putExtra(Constants.INTENT_EXTRA_ADDRESS , address);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        etLocation.setText(address);
                    }
                });
            }
        }).start();
    }

    public void Select(View v) {
        this.setResult(RESULT_OK, returnIntent);
        this.finish();
    }

    public void search(View v) {
        final String searchString = etLocation.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {

                final LatLng latLng = LocationHelper.getLocationFromAddress(MapsActivity.this,
                        searchString);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setLocation(latLng);
                    }
                });
            }
        }).start();

    }
}
