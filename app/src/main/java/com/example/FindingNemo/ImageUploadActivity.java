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
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

public class ImageUploadActivity extends Activity implements ImageUploader.ImageUploadListener, LocationHelper.LocationListener {
    private TextView tvTime, tvLocation, tvDate;
    private EditText etComments;
    private ImageInfo imageInfo = null;
    private GoogleMap map;
    private Marker marker;
    private LocationHelper locationHelper;
    private static final int REQUEST_LOCATION = 0x201;
    private static final int MAX_RETRIES = 3;
    private boolean requireLocation = false;
    int retries;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);


        setContentView(R.layout.activity_imageupload);
        locationHelper = new LocationHelper(this);
        locationHelper.setLocationListener(this);
        imageInfo = new ImageInfo();

        int mode = getIntent().getIntExtra(Constants.INTENT_EXTRA_UPLOAD_MODE, 0);
        this.setResult(RESULT_CANCELED);
        try {
            imageInfo.setFileName(getIntent().getStringExtra(Constants.INTENT_EXTRA_IMAGE_PATH));
        } catch (FileNotFoundException ignored) {

        }
        if(mode > 0)
            imageInfo = ImageInfo.fromFile(getIntent().getStringExtra(Constants.INTENT_EXTRA_IMAGE_PATH));

        ImageView ivImage = (ImageView) findViewById(R.id.ivImage);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvDate = (TextView) findViewById(R.id.tvDate);
        etComments = (EditText) findViewById(R.id.etComments);

        if (mode == 0) {
            imageInfo.setTitle(imageInfo.getFileName().substring(imageInfo.getFileName().lastIndexOf("/")).replace(".jpg", "").replace("JPEG_", ""));
            double[] location = null;
            try {
                location = imageInfo.getGeoLocation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (location == null) {
                location = locationHelper.getLastKnownLocation();
                locationHelper.enableLocation();
                requireLocation = true;
            }
            if (location != null) {
                imageInfo.setLocation(location);
            }

            Calendar cal = null;
            try {
                cal =imageInfo.getGPSTime();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (cal == null)
                cal = Calendar.getInstance();
            imageInfo.setTime(cal);

        }
        setTime();
        setLocation();
        ivImage.setImageBitmap(PhotoHelper.getBitmapFromFile(imageInfo.getFileName()));
        etComments.setText(imageInfo.getComments());
        if(mode == 2) {
            upload(null);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION && resultCode == RESULT_OK) {
            locationHelper.disableLocation();
            requireLocation = false;
            imageInfo.setLocation((float)data.getDoubleExtra(Constants.INTENT_EXTRA_LATITUDE, 0), (float)data.getDoubleExtra(Constants.INTENT_EXTRA_LONGITUDE, 0));
            tvLocation.setText(data.getStringExtra(Constants.INTENT_EXTRA_ADDRESS));
            imageInfo.setAddress(data.getStringExtra(Constants.INTENT_EXTRA_ADDRESS));
        }
    }
    @Override
    public void onHaveLocation(double[] location) {
        imageInfo.setLocation(location);
        setLocation();
    }

    @Override
    public void onUploadSuccess() {
        DialogHelper.createDialog(ImageUploadActivity.this, getString(R.string.upload_successful_message), false, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ImageUploadActivity.this.setResult(RESULT_OK);
                ImageUploadActivity.this.finish();
            }
        }).show();
    }

    @Override
    public void onUploadFail() {
        if(retries++ < MAX_RETRIES) {
            DialogHelper.createDialog(ImageUploadActivity.this, getString(R.string.upload_fail_message_try), true, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        ImageUploader iu = new ImageUploader(ImageUploadActivity.this, imageInfo, ImageUploadActivity.this);
                        iu.execute();
                    } else {
                        ImageUploadActivity.this.finish();
                    }
                }
            }).show();
        } else {
            DialogHelper.createDialog(ImageUploadActivity.this, getString(R.string.upload_fail_message_later), false, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ImageUploadActivity.this.finish();
                }
            }).show();
        }

    }

    public void upload(View v) {
        imageInfo.setComments(etComments.getText().toString());
        imageInfo.writePhotoCommentFile();
        retries = 0;
        ImageUploader iu = new ImageUploader(this, imageInfo, this);
        iu.execute();
    }
    public void changeTime(View v) {
        Calendar cal = imageInfo.getTime();
        (new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar cal = imageInfo.getTime();
                cal.set(year, monthOfYear, dayOfMonth);
                imageInfo.setTime(cal);
                (new TimePickerDialog(ImageUploadActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar cal = imageInfo.getTime();
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        cal.set(Calendar.MINUTE, minute);
                        imageInfo.setTime(cal);
                        setTime();
                    }
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)).show();
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))).show();
    }

    public void setTime() {
        tvDate.setText(PhotoHelper.getDateStringFromCalendar(imageInfo.getTime()));
        tvTime.setText(PhotoHelper.getTimeStringFromCalendar(imageInfo.getTime()));
    }

    public void changeLocation(View v) {
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra(Constants.INTENT_EXTRA_LATITUDE, imageInfo.getLocation()[0]);
        i.putExtra(Constants.INTENT_EXTRA_LONGITUDE, imageInfo.getLocation()[1]);
        this.startActivityForResult(i, REQUEST_LOCATION);
    }

    public void setLocation() {
        LatLng latLng = new LatLng(imageInfo.getLocation()[0], imageInfo.getLocation()[1]);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

        if (marker != null)
            marker.remove();
        marker = map.addMarker(new MarkerOptions().position(latLng));

        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject result = LocationHelper.getLocationFormGoogle(imageInfo.getLocation()[0] + "," + imageInfo.getLocation()[1]);
                final String address = LocationHelper.getCityAddress(result);
                imageInfo.setAddress(address);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvLocation.setText(address);
                        Toast.makeText(ImageUploadActivity.this, imageInfo.getAddress(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(ImageUploadActivity.this, imageInfo.getAddress(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationHelper.disableLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(requireLocation)
            locationHelper.enableLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationHelper.disableLocation();
    }
}
