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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends Activity implements PhotoHelper.PhotoListener {

    PhotoHelper photoHelper;
    LocationHelper locationHelper;
    public static final int REQUEST_UPLOAD_PHOTO = 0x201;
    public static final int REQUEST_UPLOAD_RETRY = 0x202;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] items;
    private SidebarAdapter sidebarAdapter;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle(R.string.app_name);
        photoHelper = new PhotoHelper(MainActivity.this);
        if(MainActivity.this.getIntent().getBooleanExtra("LaunchCamera", true))
            getPicFromCamera(null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeSidebar();


        photoHelper.setPhotoListener(this);
        locationHelper = new LocationHelper(this);
        locationHelper.checkGPSStatus();
        refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (photoHelper.checkActivityResult(requestCode, resultCode, data))
            return;
        if (locationHelper.checkActivityResult(requestCode))
            return;
        refresh();
    }


    @Override
    public void onHavePhoto(String imagePath) {
        Intent i = new Intent(MainActivity.this, ImageUploadActivity.class);
        i.putExtra(Constants.INTENT_EXTRA_IMAGE_PATH, imagePath);
        startActivityForResult(i, REQUEST_UPLOAD_PHOTO);
    }

    public void getPicFromCamera(View v) {
        Debug.startMethodTracing("calc");
        photoHelper.getPicFromCamera();
    }

    public void getPicFromGallery(View v) {
        photoHelper.getPicFromGallery();
    }

    public void goToWebsite(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website)));
        startActivity(browserIntent);
    }

    public void emailDeveloper(View v) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.developer_email));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " feedback");
        startActivity(Intent.createChooser(intent, "Send Email"));
    }

    public void retryFailed(View v) {
        Intent i = new Intent(this, FailedListActivity.class);
        startActivityForResult(i, REQUEST_UPLOAD_RETRY);
    }

    private void refresh() {
        int failedUploads = PhotoHelper.getFailedUploads(this);
        /*TextView tv = (TextView) findViewById(R.id.tvFailedUpload);
        if (failedUploads == 1)
            tv.setText(getString(R.string.failed_upload_one));
        else if (failedUploads == 0)
            tv.setText(getString(R.string.failed_upload_none));
        else
            tv.setText(String.format(getString(R.string.failed_uploads_many), failedUploads));*/
        items[3] = String.format(getResources().getStringArray(R.array.menuItems)[3], failedUploads);
        sidebarAdapter.notifyDataSetChanged();
    }

    private void initializeSidebar(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close ){
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }
        };
        items = getResources().getStringArray(R.array.menuItems);

        sidebarAdapter = new SidebarAdapter(this, items, Constants.icons);
        mDrawerList.setAdapter(sidebarAdapter);

        final SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", 0);
        String name = sharedPreferences.getString("name", null);
        if (name == null) {
            DialogHelper.showNameDialog(this, new DialogHelper.NameDialogListener() {
                @Override
                public void onNameSet(String name) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", name);
                    editor.commit();
                    items[0] = String.format(items[0], name);
                    sidebarAdapter.notifyDataSetChanged();
                }
            });

        } else {
            items[0] = String.format(items[0], name);
            sidebarAdapter.notifyDataSetChanged();
        }
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Constants.tags tag = Constants.tags.values()[position];
                switch (tag) {
                    case HEADER:
                        break;
                    case CAMERA:
                        getPicFromCamera(view);
                        break;
                    case GALLERY:
                        getPicFromGallery(view);
                        break;
                    case RETRY:
                        retryFailed(view);
                        break;
                    case ABOUT:
                        break;
                }
            }
        });
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
