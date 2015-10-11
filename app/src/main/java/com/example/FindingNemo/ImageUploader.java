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

/**
 * This file is part of Picasa Photo Uploader.
 *
 * Picasa Photo Uploader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Picasa Photo Uploader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Picasa Photo Uploader. If not, see <http://www.gnu.org/licenses/>.
 */
package com.example.FindingNemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ImageUploader extends AsyncTask<Void, Integer, Boolean> {

    private Context context;
    private ImageInfo imageInfo;
    private ProgressDialog dialog;
    private final String imageAuth;
    private static final String url = "http://picasaweb.google.com/data/feed/api/user/picasatest112/albumid/default";
  private ImageUploadListener listener;
    public interface ImageUploadListener {
        void onUploadSuccess();
        void onUploadFail();
    }

    public ImageUploader(Context context, ImageInfo imageInfo, ImageUploadListener listener) {
        this.context = context;
        this.imageInfo = imageInfo;
        this.listener = listener;
        imageAuth = context.getString(R.string.auth_string);
    }

    @Override
    protected Boolean doInBackground(Void... param) {
        File file = new File(imageInfo.getFileName());
        String tempfile = imageInfo.getCommentsFileName()+".1";
        File file1 = new File(imageInfo.getCommentsFileName());
        file1.renameTo(new File(tempfile));

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);

        try {

            Multipart multipart = new Multipart("Media multipart posting", "END_OF_PART");

            multipart.addPart("<entry xmlns='http://www.w3.org/2005/Atom'>" +
                    "<title>"+imageInfo.getTitle()+"</title>" +
                    "<summary>" +
                    imageInfo.getPhotoComment() +
                    "</summary>" +
                    "<category scheme=\"http://schemas.google.com/g/2005#kind\" term=\"http://schemas.google.com/photos/2007#photo\"/></entry>", "application/atom+xml");
            multipart.addPart(file, "image/jpeg");

            MultipartNotificationEntity entity = new MultipartNotificationEntity(multipart, new MultipartNotificationEntity.onProgressListener() {
                @Override
                public void onProgressUpdate(int percent) {
                    publishProgress(percent);
                }
            });

            HttpParams params = client.getParams();

            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 15000);
            params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);

            post.setEntity(entity);

            post.addHeader("Authorization", "GoogleLogin auth=" + imageAuth);
            post.addHeader("GData-Version", "2");
            post.addHeader("MIME-version", "1.0");

            HttpResponse response = client.execute(post);
            StatusLine line = response.getStatusLine();

            if (line.getStatusCode() > 201) {
                throw new Exception("Failed upload");
            }

            client.getConnectionManager().shutdown();

            (new File(imageInfo.getFileName())).delete();
            (new File(tempfile)).delete();
            return  true;
        } catch (Exception e) {
            e.printStackTrace();
            post.abort();
            client.getConnectionManager().shutdown();
            file1 = new File(tempfile);
            file1.renameTo(new File(imageInfo.getCommentsFileName()));
            return false;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setTitle(context.getString(R.string.dialog_image_upload_title));
        dialog.setMessage(context.getString(R.string.dialog_image_upload_message));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.setMax(100);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        dialog.dismiss();
        if(aBoolean)
            listener.onUploadSuccess();
        else
            listener.onUploadFail();

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        dialog.setProgress(values[0]);
    }
}
