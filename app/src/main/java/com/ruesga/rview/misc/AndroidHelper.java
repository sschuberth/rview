/*
 * Copyright (C) 2016 Jorge Ruesga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ruesga.rview.misc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager.TaskDescription;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.ruesga.rview.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class AndroidHelper {

    public static boolean isLollipopOrGreater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isNougatOrGreater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isApi26OrGreater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    @TargetApi(Build.VERSION_CODES.N)
    @SuppressWarnings("deprecation")
    public static Locale getCurrentLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        }
        return context.getResources().getConfiguration().locale;
    }

    public static void showErrorSnackbar(Context context, @NonNull View parent,
            @StringRes int message) {
        Snackbar snackbar = Snackbar.make(parent, message, Snackbar.LENGTH_LONG);
        View v = snackbar.getView();
        v.setBackgroundColor(ContextCompat.getColor(context, R.color.accent));
        snackbar.show();
    }

    public static void showWarningSnackbar(Context context, @NonNull View parent,
            @StringRes int message) {
        Snackbar snackbar = Snackbar.make(parent, message, Snackbar.LENGTH_LONG);
        View v = snackbar.getView();
        v.setBackgroundColor(ContextCompat.getColor(context, R.color.alert));
        snackbar.show();
    }

    public  static void hideSoftKeyboard(Context context, Window window) {
        if (window == null) {
            return;
        }

        View view = window.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String loadRawResourceAsStream(Context ctx) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    ctx.getResources().openRawResource(R.raw.repositories)));
            char[] chars = new char[4096];
            int read;
            while ((read = reader.read(chars, 0, 4096)) != -1) {
                sb.append(chars, 0, read);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
        }
        return sb.toString();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void configureTaskDescription(Activity activity) {
        if (isLollipopOrGreater()) {
            TaskDescription taskDesc = new TaskDescription(
                    null, null, ContextCompat.getColor(activity, R.color.primaryDark));
            activity.setTaskDescription(taskDesc);
        }
    }
}
