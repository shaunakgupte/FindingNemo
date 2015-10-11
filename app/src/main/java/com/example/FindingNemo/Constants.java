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

public class Constants {
    public static final String INTENT_EXTRA_LATITUDE = "LATITUDE";
    public static final String INTENT_EXTRA_LONGITUDE = "LONGITUDE";
    public static final String INTENT_EXTRA_ADDRESS = "ADDRESS";
    public static final String INTENT_EXTRA_IMAGE_PATH = "IMAGE_PATH";
    public static final String INTENT_EXTRA_UPLOAD_MODE = "UPLOAD_MODE";
    public static final int THUMBNAIL_WIDTH = 200;
    public static final int THUMBNAIL_HEIGHT = 100;

    public static final int[] icons = {
            0, R.drawable.camera, R.drawable.gallery, R.drawable.retry, R.drawable.about
    };
    public static enum tags {
            HEADER, CAMERA, GALLERY, RETRY, ABOUT
    };
}
