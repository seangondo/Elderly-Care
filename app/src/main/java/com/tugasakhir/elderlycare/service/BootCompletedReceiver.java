package com.tugasakhir.elderlycare.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.tugasakhir.elderlycare.handler.DBHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DBHandler myDb = new DBHandler(context.getApplicationContext());

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            JSONArray arr = null;
            try {
                arr = myDb.LoginData();
                for(int i = 0; i < arr.length(); i ++) {
                    JSONObject obj = arr.getJSONObject(i);
                    if (obj.getInt("autoLog") == 1) {
                        // Start the foreground service after device boot completed
                        Intent serviceIntent = new Intent(context, mqttServices.class);
                        ContextCompat.startForegroundService(context, serviceIntent);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}