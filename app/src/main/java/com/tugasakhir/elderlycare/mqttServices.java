package com.tugasakhir.elderlycare;

import static com.tugasakhir.elderlycare.MainActivity.client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

public class mqttServices extends Service {
    public static String msg;
    public static String getTopic;
    public static boolean lampLiving, lampKitchen;
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

                if(getTopic.equals(MainActivity.myUser + "/livingroom/light/control")) {
                    JSONObject arrObj = new JSONObject(msg);
                    lampLiving = arrObj.getBoolean("lights");
                }
                if(getTopic.equals(MainActivity.myUser + "/kitchen/light/control")) {
                    JSONObject arrObj = new JSONObject(msg);
                    lampKitchen = arrObj.getBoolean("lights");
                }
//                Log.d("MQTT Topic", getTopic);
//                Log.d("Mqtt Msg", msg);
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
}