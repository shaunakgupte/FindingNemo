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
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PhotoHelper {

    private final int REQUEST_IMAGE_CAMERA = 0x101;
    private final int REQUEST_IMAGE_GALLERY = 0x102;

    private Activity activity;
    private PhotoListener listener;
    private String sCurrentPhotoPath;
    public PhotoHelper(Activity activity) {
        this.activity = activity;
    }

    public void setPhotoListener(PhotoListener listener) {
        this.listener = listener;
    }

    public static String getDateStringFromCalendar(Calendar cal) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM d, yyyy");
        return formatter.format(cal.getTime());
    }

    public static String getTimeStringFromCalendar(Calendar cal) {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        return formatter.format(cal.getTime());
    }

    public static String calendarToString(Calendar cal) {
        return getDateStringFromCalendar(cal)+ " " + getTimeStringFromCalendar(cal);
    }



    public static Bitmap getBitmapFromFile(String filename) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        options.inSampleSize = calculateInSampleSize(options, Constants.THUMBNAIL_WIDTH,
                Constants.THUMBNAIL_HEIGHT);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);

    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        while (height > reqHeight || width > reqWidth) {
            if (height / 2 > reqHeight && width / 2 > reqWidth)
                inSampleSize *= 2;
            height /= 2;
            width /= 2;
        }
        return inSampleSize;
    }

    public static int getFailedUploads(Context context) {
        int count = 0;
        File storageDir = context.getExternalFilesDir(null);
        if (storageDir == null)
            return 0;
        File[] files= storageDir.listFiles();
        for(File f : files)
            if(f.getAbsolutePath().endsWith(".txt"))
                count++;
        return count;
    }

    public boolean getPicFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //if (takePictureIntent.resolveActivity(activity.getPackageManager()) == null)
        //    return false;
        File photoFile;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
            showToast("Internal Error: Could not create temporary file. Try Again!");
            return false;
        }
        sCurrentPhotoPath = photoFile.getAbsolutePath();
        takePictureIntent.putExtra("fileName",photoFile.getAbsolutePath());
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photoFile));
        activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAMERA);
        Debug.stopMethodTracing();
        return true;
    }

    public boolean getPicFromGallery() {
        File photoFile;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
            showToast("Internal Error: Could not create temporary file. Try Again!");
            return false;
        }
        sCurrentPhotoPath = photoFile.getAbsolutePath();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
            Intent intent = new Intent();
            intent.setType("image/jpeg");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_GALLERY);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/jpeg");
            activity.startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
        }
        return true;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(null);

        File f = new File(storageDir, imageFileName+".jpg");
        return f;
        //return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void showToast(String text) {
        Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public boolean checkActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {
            listener.onHavePhoto(sCurrentPhotoPath);
            return true;
        }
        if (requestCode == REQUEST_IMAGE_GALLERY) {
            if (data == null) {
                showToast("Error: Could not get image from gallery. Try Again.");
            } else {
                Uri uri = data.getData();
                InputStream stream = null;
                OutputStream outputStream = null;
                int read;
                byte[] buffer = new byte[4096];
                try {
                    stream = activity.getContentResolver().openInputStream(uri);
                    outputStream = new FileOutputStream(sCurrentPhotoPath);
                    read = stream.read(buffer);
                    while(read != -1) {
                        outputStream.write(buffer, 0, read);
                        read = stream.read(buffer);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    showToast("Could not open file!");
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast("Internal Error occured!");
                } finally {
                    try {
                        if (stream != null)
                            stream.close();
                        if (outputStream != null)
                            outputStream.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        showToast("Internal Error occured!");
                    }

                }

                listener.onHavePhoto(sCurrentPhotoPath);
            }
            return true;
        }
        return false;
    }
    interface PhotoListener {
        void onHavePhoto(String filepath);
    }
}
