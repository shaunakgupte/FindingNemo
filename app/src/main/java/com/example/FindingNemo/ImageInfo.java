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

import android.media.ExifInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.TimeZone;

public class ImageInfo {
    private String fileName;
    private String comments;
    private String title;
    private String address;
    private double latitude, longitude;
    private long time;

    public static final double UNKNOWN_LOCATION = 1000;

    public ImageInfo() {
        latitude = UNKNOWN_LOCATION;
        longitude = UNKNOWN_LOCATION;
    }

    public String getFileName() {
        return fileName;
    }
    public String getCommentsFileName() {
        return fileName.replace(".jpg", ".txt");
    }

    public void setFileName(String filename) throws FileNotFoundException {
        File file = new File(filename);
        if(!file.exists())
            throw new FileNotFoundException();
        this.fileName = filename;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double[] getLocation() {
        return new double[]{latitude, longitude};
    }

    public void setLocation(double latitude, double longitude)  throws  IllegalArgumentException {
        if (latitude > 90 || latitude < -90)
            return;
        if (longitude > 180 || longitude < -180)
            return;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLocation(double[] location) throws  IllegalArgumentException {
        if (location.length != 2)
            throw new IllegalArgumentException("array length is not two");
        setLocation(location[0], location[1]);
    }

    public Calendar getTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal;
    }

    public String getPhotoComment() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return ((title != null) ? "Title: " + title + "\n" : "" ) +
                ((time != 0) ? "Taken on: " + PhotoHelper.calendarToString(cal) + "\n" : "" ) +
                ((time != 0) ? "Timestamp: " + Long.toString(time) + "\n" : "" ) +
                ((getLocation() != null) ? String.format("Location: %f %f\n", longitude, latitude) : "" ) +
                ((address != null) ? "Address:" + address + "\n": "" ) +
                ((comments != null) ? "Comment:" + comments : "" );
    }

    public void setTime(Calendar calendar) {
        this.time = calendar.getTimeInMillis();
    }

    public void writePhotoCommentFile(){
        PrintWriter out;
        try {
            out = new PrintWriter(getCommentsFileName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        out.print(getPhotoComment());
        out.close();
    }
    public static String getCommentsFromFile(String imagePath) throws IOException {
        return readFile(imagePath.replace(".jpg", ".txt"));
    }

    private static String readFile( String file ) throws IOException {
        BufferedReader reader = new BufferedReader( new FileReader(file));
        String         line;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = "\n";

        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }

        return stringBuilder.toString();
    }

    public static ImageInfo fromFile(String fileName) {
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.fileName = fileName;
        String comment;
        try {
            comment = getCommentsFromFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (comment.contains("Title:")) {
            String str = comment.substring(comment.indexOf("Title:") + 7);
            str = str.substring(0, str.indexOf("\n"));
            imageInfo.setTitle(str);
            comment = comment.substring(comment.indexOf("\n") + 1);
        }

        if (comment.contains("Timestamp:")) {
            String str = comment.substring(comment.indexOf("Timestamp:") + 11);
            str = str.substring(0, str.indexOf("\n"));
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(str));
            imageInfo.setTime(cal);
            comment = comment.substring(comment.indexOf("\n") + 1);
        }
        if (comment.contains("Location:")) {
            String str = comment.substring(comment.indexOf("Location:") + 10);
            str = str.substring(0, str.indexOf("\n"));
            String temp[] = str.split(" ");
            imageInfo.setLocation(Float.parseFloat(temp[1]), Float.parseFloat(temp[0]));
            comment = comment.substring(comment.indexOf("\n") + 1);
        }
        if (comment.contains("Address:")) {
            String str = comment.substring(comment.indexOf("Address:") + 8);
            if (comment.contains("Comment:"))
                str = str.substring(0, str.indexOf("\nComment"));
            imageInfo.setAddress(str);
        }

        if (comment.contains("Comment:")) {
            String str = comment.substring(comment.indexOf("Comment:") + 8);
            str = str.trim();
            imageInfo.setComments(str);
        }
        return  imageInfo;
    }

    public double[] getGeoLocation () throws IOException {
        ExifInterface exif = new ExifInterface(getFileName());
        float[] latLong  = new float[2];
        if(!exif.getLatLong(latLong))
            return null;
        return new double[]{latLong[0], latLong[1]};
    }

    public Calendar getGPSTime () throws IOException {
        ExifInterface exif = new ExifInterface(getFileName());
        if (exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP) == null)
            return null;
        if (exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP) == null)
            return null;
        String[] date = exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP).split(":");
        String[] time = exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP).split(":");
        Calendar cal  = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[2]),
                Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));
        return cal;
    }
}
