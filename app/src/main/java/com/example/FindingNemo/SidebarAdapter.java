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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by shaunak on 13/6/15.
 */
public class SidebarAdapter extends ArrayAdapter<String> {
    String[] menuItems = null;
    int[] icons = null;
    LayoutInflater inflater;

    public SidebarAdapter(Context context, String[] objects, int[] icons) {
        super(context, -1, objects);
        Activity activity = (Activity) context;
        menuItems = objects;
        inflater = activity.getLayoutInflater();
        this.icons = icons;
    }

    @Override
    public int getCount() {
        return menuItems == null ? 0 :menuItems.length;
    }

    @Override
    public String getItem(int position) {
        return menuItems[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (position == 0)
            view = inflater.inflate(R.layout.sidebar_header, null);
        else
            view  = inflater.inflate(R.layout.sidebar_items, null);
        TextView tv = (TextView) view.findViewById(R.id.tvSidebarMenuItem);
        tv.setText(getItem(position));
        ImageView  iv = (ImageView) view.findViewById(R.id.ivSidebarIcon);
        if (position > 0)
            iv.setImageResource(icons[position]);
        view.setTag(position);
        return view;
    }
}
