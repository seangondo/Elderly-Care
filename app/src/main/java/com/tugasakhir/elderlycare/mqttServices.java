package com.tugasakhir.elderlycare;

import static com.tugasakhir.elderlycare.MainActivity.client;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class mqttServices extends Service {
    public static String msg;
    public static String getTopic;
    public static String living_temp, living_light, kitchen_light, kitchen_gas, bLampLiving, bLampKitchen, bFanLiving, bAutoMode;

    public static List<String> kitchen_date, kitchen_time, living_date, living_time;
    public static List<Integer> living_no, living_val, kitchen_no, kitchen_val;
    public mqttServices() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Service", "MQTT Service called!");

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e("Connection MQTT", String.valueOf(cause));

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                getTopic = topic;
                msg = new String(message.getPayload());
//                Log.e("Hasil", String.valueOf(living_temp));
                Log.d("MQTT Topic", getTopic);
//                Log.d("Mqtt Msg", msg);
                smartHomePieChart();
                smartHomeLogging();
                smartHomeButton();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void smartHomeButton() throws JSONException {
        JSONObject arrObj = new JSONObject(msg);
        if(getTopic.contains("/apps/control_button/livingroom/fan")) {
            bFanLiving = arrObj.getString("value");
        }
        if(getTopic.contains("/apps/control_button/livingroom/light")) {
            bLampLiving = arrObj.getString("value");
        }
        if(getTopic.contains("/apps/control_button/kitchen/light")) {
            bLampKitchen = arrObj.getString("value");
        }
        if(getTopic.contains("/apps/control_button/automatic_mode")) {
            bAutoMode = arrObj.getString("value");
        }
    }

    private void smartHomePieChart() throws JSONException {
        JSONArray myRec = null;
        if(getTopic.contains("/apps/data")) {
            myRec = new JSONArray(mqttServices.msg);
            for (int i = 0; i < myRec.length(); i++) {
                JSONObject arrObj = myRec.getJSONObject(i);
                living_temp = arrObj.getString("livingroom_temp");
                living_light = arrObj.getString("livingroom_light");
                kitchen_light = arrObj.getString("kitchen_light");
                kitchen_gas = arrObj.getString("kitchen_gas");
            }
        }
    }

    private void smartHomeLogging() throws JSONException {
        JSONArray myRec = null;
        List<Integer> no = new ArrayList<>();
        List<Integer> val = new ArrayList<>();
        List<String> date = new ArrayList<>();
        List<String> time = new ArrayList<>();


        if(getTopic.contains("/apps/trend/kitchen_gas")) {
            myRec = new JSONArray(mqttServices.msg);
            for (int i = 0; i < myRec.length(); i++) {
                JSONObject arrObj = myRec.getJSONObject(i);
                no.add(arrObj.getInt("dataNo"));
                val.add(arrObj.getInt("value"));
                date.add(arrObj.getString("date"));
                time.add(arrObj.getString("time"));
            }
            Collections.reverse(no);
            Collections.reverse(val);
            Collections.reverse(date);
            Collections.reverse(time);
            kitchen_no = no;
            kitchen_val = val;
            kitchen_date = date;
            kitchen_time = time;
        }
        if(getTopic.contains("/apps/trend/livingroom_temp")) {
            myRec = new JSONArray(mqttServices.msg);
            for (int i = 0; i < myRec.length(); i++) {
                JSONObject arrObj = myRec.getJSONObject(i);
                no.add(arrObj.getInt("dataNo"));
                val.add(arrObj.getInt("value"));
                date.add(arrObj.getString("date"));
                time.add(arrObj.getString("time"));
            }
            Collections.reverse(no);
            Collections.reverse(val);
            Collections.reverse(date);
            Collections.reverse(time);
            living_no = no;
            living_val = val;
            living_date = date;
            living_time = time;
        }
    }
}