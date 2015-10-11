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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.io.File;

public class FailedListActivity extends Activity {
    FailedListAdapter adapter;
    ListView lvFailed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_failed);
        adapter = new FailedListAdapter(this);
        lvFailed = (ListView) findViewById(R.id.lvFailed);
        lvFailed.setAdapter(adapter);
        lvFailed.addFooterView(new View(this));
        lvFailed.addHeaderView(new View(this));
    }

    public void delete(final View v) {
        DialogHelper.createDialog(this, getString(R.string.dialog_delete) , true, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            (new File((String)v.getTag())).delete();
                            (new File(((String) v.getTag()).replace(".jpg",",txt"))).delete();
                            adapter.refreshFiles();
                            adapter.notifyDataSetChanged();
                    }
                }}).show();
    }

    public void retry(View v) {
        Intent i = new Intent(this, ImageUploadActivity.class);
        i.putExtra(Constants.INTENT_EXTRA_IMAGE_PATH, (String)v.getTag());
        i.putExtra(Constants.INTENT_EXTRA_UPLOAD_MODE, 2);
        this.startActivityForResult(i, 0x110);
    }

    public void edit(View v) {
        Intent i = new Intent(this, ImageUploadActivity.class);
        i.putExtra(Constants.INTENT_EXTRA_IMAGE_PATH, (String)v.getTag());
        i.putExtra(Constants.INTENT_EXTRA_UPLOAD_MODE, 1);
        this.startActivityForResult(i, 0x111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.refreshFiles();
        adapter.notifyDataSetChanged();
    }
}
