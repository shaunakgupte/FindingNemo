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
import android.os.Debug;

/**
 * Created by shaunak on 21/6/15.
 */
public class LaunchCameraActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("LaunchCamera", false);
        this.startActivity(i);
        super.onCreate(savedInstanceState);
        this.finish();
    }
}
