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
    public static boolean lampLiving, lampKitchen;
    public static String living_temp, living_light, kitchen_light, kitchen_gas, bLampLiving, bLampKitchen, bFanLiving, bAutoMode;

    public static List<String> kitchen_date, kitchen_time, living_date, living_time;
    public static List<Integer> living_no, living_val, kitchen_no, kitchen_val;
    public mqttServices() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                getTopic = topic;
                msg = new String(message.getPayload());
//                Log.d("MQTT Topic", getTopic);
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
//        if(getTopic.equals(MainActivity.myUser + "/livingroom/light/control")) {
        if(getTopic.equals(MainActivity.myUser + "/apps/control_button/livingroom/fan")) {
//            lampLiving = arrObj.getBoolean("lights");
            bFanLiving = arrObj.getString("value");
        }
        if(getTopic.equals(MainActivity.myUser + "/apps/control_button/livingroom/light")) {
//            lampLiving = arrObj.getBoolean("lights");
            bLampLiving = arrObj.getString("value");
        }
//        if(getTopic.equals(MainActivity.myUser + "/kitchen/light/control")) {
        if(getTopic.equals(MainActivity.myUser + "/apps/control_button/kitchen/light")) {
//            lampKitchen = arrObj.getBoolean("lights");
            bLampKitchen = arrObj.getString("value");
        }
        if(getTopic.equals(MainActivity.myUser + "/apps/control_button/automatic_mode")) {
//            lampKitchen = arrObj.getBoolean("lights");
            bAutoMode = arrObj.getString("value");
        }
    }

    private void smartHomePieChart() throws JSONException {
        JSONArray myRec = null;
        if(getTopic.equals(MainActivity.myUser + "/apps/data")) {
            myRec = new JSONArray(mqttServices.msg);
            for (int i = 0; i < myRec.length(); i++) {
                JSONObject arrObj = myRec.getJSONObject(i);
                living_temp = arrObj.getString("living_temp");
                living_light = arrObj.getString("living_light");
                kitchen_light = arrObj.getString("kitchen_light");
                kitchen_gas = arrObj.getString("kitchen_gas");
            }
        }
//        if(getTopic.equals(MainActivity.myUser + "/apps/trend/trend_kitchen_gas")) {
//            kitchen_no.clear();
//            kitchen_val.clear();
//            kitchen_date.clear();
//            kitchen_time.clear();
//            myRec = new JSONArray(mqttServices.msg);
//            for (int i = 0; i < myRec.length(); i++) {
//                JSONObject arrObj = myRec.getJSONObject(i);
//                kitchen_no.add(arrObj.getInt("dataNo"));
//                kitchen_val.add(arrObj.getInt("value"));
//                kitchen_date.add(arrObj.getString("date"));
//                kitchen_time.add(arrObj.getString("time"));
//            }
//        }
//        if(getTopic.equals(MainActivity.myUser + "/apps/trend/trend_living_temp")) {
//            living_no.clear();
//            living_val.clear();
//            living_date.clear();
//            living_time.clear();
//            myRec = new JSONArray(mqttServices.msg);
//            for (int i = 0; i < myRec.length(); i++) {
//                JSONObject arrObj = myRec.getJSONObject(i);
//                living_no.add(arrObj.getInt("dataNo"));
//                living_val.add(arrObj.getInt("value"));
//                living_date.add(arrObj.getString("date"));
//                living_time.add(arrObj.getString("time"));
//            }
//        }
    }

    private void smartHomeLogging() throws JSONException {
        JSONArray myRec = null;
        List<Integer> no = new ArrayList<>();
        List<Integer> val = new ArrayList<>();
        List<String> date = new ArrayList<>();
        List<String> time = new ArrayList<>();


        if(getTopic.equals(MainActivity.myUser + "/apps/trend/trend_kitchen_gas")) {
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
        if(getTopic.equals(MainActivity.myUser + "/apps/trend/trend_living_temp")) {
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