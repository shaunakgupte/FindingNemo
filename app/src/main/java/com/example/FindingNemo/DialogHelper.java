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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

public class DialogHelper {
    public static AlertDialog createDialog(Context context, String message, boolean isTwoButton, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (message != null)
            builder = builder.setMessage(message);
        if (onClickListener != null)
            builder = builder.setPositiveButton("OK", onClickListener);
        else
            builder = builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        if (isTwoButton)
            if (onClickListener != null)
                builder = builder.setNegativeButton("Cancel", onClickListener);
            else
                builder = builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.setCancelable(false);
        return builder.create();
    }

    public static void showNameDialog(Context context, final NameDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Please enter your name");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                if (text == null || text.isEmpty())
                    return;
                listener.onNameSet(text);

            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public interface NameDialogListener {
        public void onNameSet (String name);
    }
}
