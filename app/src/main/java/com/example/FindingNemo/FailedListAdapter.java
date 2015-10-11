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
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class FailedListAdapter extends BaseAdapter {
    private Activity activity;
    private String[] filenames;
    private HashMap<String, Bitmap> images;
    private HashMap<String, ImageInfo> info;
    public FailedListAdapter(Activity activity) {
        super();
        this.activity = activity;
        images = new HashMap<>();
        info = new HashMap<>();
        refreshFiles();
    }

    public void refreshFiles() {
        File storageDir = activity.getExternalFilesDir(null);
        if (storageDir == null)
            return;
        File[] files= storageDir.listFiles();
        if (files == null) {
            filenames = null;
            return;
        }
        ArrayList<String> filenames = new ArrayList<>();
        for(File file: files) {
            if(file.getAbsolutePath().endsWith(".jpg")) {
                String filename = file.getAbsolutePath().replace(".jpg", ".txt");
                if((new File(filename)).exists())
                    continue;
                file.delete();
            }
            if(!file.getAbsolutePath().endsWith(".txt"))
                continue;
            String filename = file.getAbsolutePath().replace(".txt", ".jpg");
            if(!(new File(filename)).exists()) {
                file.delete();
                continue;
            }
            filenames.add(filename);
        }
        this.filenames = new String[filenames.size()];
        filenames.toArray(this.filenames);
    }

    @Override
    public int getCount() {
        return filenames==null? 0 : filenames.length;
    }

    @Override
    public Object getItem(int position) {
        return filenames == null ? null : filenames [position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String filename = filenames[position];
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.failedlistitem, null);
        }

        if (!info.containsKey(filename))
            info.put(filename, ImageInfo.fromFile(filename));

        final ImageInfo imageInfo = info.get(filename);

        if (imageInfo == null)
            return null;
        TextView tv = (TextView) convertView.findViewById(R.id.tvTime);
        tv.setText(PhotoHelper.calendarToString(imageInfo.getTime()));

        tv = (TextView) convertView.findViewById(R.id.tvLocation);
        if (imageInfo.getAddress() != null && !imageInfo.getAddress().isEmpty())
            tv.setText(imageInfo.getAddress());
        else {
            final double[] location = imageInfo.getLocation();
            if (location[0] == ImageInfo.UNKNOWN_LOCATION || location[1] == ImageInfo.UNKNOWN_LOCATION)
                tv.setText("Unknown");
            else {
                tv.setText(String.format("%.2f %s , %.2f %s", location[0], location[0] > 0 ? "N" : "S",
                        location[1], location[1] > 0 ? "E" : "W"));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject result = LocationHelper.getLocationFormGoogle(location[0] + "," + location[1]);
                        String address = LocationHelper.getCityAddress(result);
                        imageInfo.setAddress(address);
                        imageInfo.writePhotoCommentFile();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FailedListAdapter.this.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();

            }

        }
        if (imageInfo.getComments().isEmpty()) {
            convertView.findViewById(R.id.llComments).setVisibility(View.GONE);
        } else {
            tv = (TextView) convertView.findViewById(R.id.tvComments);
            tv.setText(imageInfo.getComments());
            convertView.findViewById(R.id.llComments).setVisibility(View.VISIBLE);
        }

        View v = convertView.findViewById(R.id.vDelete);
        v.setTag(filename);
        v = convertView.findViewById(R.id.vRetry);
        v.setTag(filename);
        v = convertView.findViewById(R.id.vEdit);
        v.setTag(filename);

        ImageView iv = (ImageView) convertView.findViewById(R.id.ivImage);
        if (!images.containsKey(filename))
            images.put(filename, PhotoHelper.getBitmapFromFile(filename));
        iv.setImageBitmap(images.get(filename));

        return convertView;
    }
}
