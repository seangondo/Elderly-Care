package com.tugasakhir.elderlycare.service;

import static com.tugasakhir.elderlycare.ui.MainActivity.client;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.model.TrendReceive;
import com.tugasakhir.elderlycare.ui.MainActivity2;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class mqttServices extends Service {

    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;
    public static String msg;
    public static String getTopic;
    public static String living_temp, living_light, kitchen_light, kitchen_gas, bLampLiving, bLampKitchen, bFanLiving, bAutoMode;

    public static List<String> kitchen_date, kitchen_time, living_date, living_time;
    public static List<Integer> living_no, living_val, kitchen_no, kitchen_val;

    public static JSONArray trendRec = new JSONArray();
    public static JSONArray HrTrendRec = new JSONArray();
    public static JSONArray HrData = new JSONArray();
    public static JSONArray onBody = new JSONArray();

    public static ArrayList<TrendReceive> trend;

    // TODO buat mqtt menerima berdasarkan database sensor
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
                wearableData();
                checkNotification();
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

        ArrayList<TrendReceive> trend = new ArrayList<>();

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

            Collections.reverse(trend);
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

        // CARA 2
        if(getTopic.contains("/apps/trend")) {
            myRec = new JSONArray(mqttServices.msg);
            Log.e("Data", String.valueOf(trendRec));
            Log.e("Input trend", String.valueOf(myRec.length()));
            Log.e("trendRec", String.valueOf(trendRec.length()));

            for (int i = myRec.length() - 1; i >= 0; i--) {
                JSONObject data = new JSONObject();
                JSONObject arrObj = myRec.getJSONObject(i);

                for(int x = 0; x < trendRec.length(); x++) {
                    if ((Objects.equals(arrObj.getString("house_id"), trendRec.getJSONObject(x).getString("house_id"))) &&
                            (Objects.equals(arrObj.getString("type"), trendRec.getJSONObject(x).getString("type"))) &&
                            (Objects.equals(arrObj.getInt("dataNo"), trendRec.getJSONObject(x).getInt("dataNo")))) {
                        trendRec.remove(x);
                    }
                }

                data.put("house_id", arrObj.getString("house_id"));
                data.put("type", arrObj.getString("type"));
                data.put("dataNo", arrObj.getInt("dataNo"));
                data.put("value", arrObj.getInt("value"));
                data.put("date", arrObj.getString("date"));
                data.put("time", arrObj.getString("time"));
                trendRec.put(data);

//                if(trendRec.length() <= myRec.length()) {
//                    data.put("house_id", arrObj.getString("house_id"));
//                    data.put("type", arrObj.getString("type"));
//                    data.put("dataNo", arrObj.getInt("dataNo"));
//                    data.put("value", arrObj.getInt("value"));
//                    data.put("date", arrObj.getString("date"));
//                    data.put("time", arrObj.getString("time"));
//                    trendRec.put(data);
//                } else {
//                    for(int x = 0; x < trendRec.length(); x++) {
//                        if ((Objects.equals(arrObj.getString("house_id"), trendRec.getJSONObject(x).getString("house_id"))) &&
//                                (Objects.equals(arrObj.getString("type"), trendRec.getJSONObject(x).getString("type")))) {
//                            JSONObject curObj = trendRec.getJSONObject(x);
//                            Log.e("Error", "masok 1");
//                            curObj.put("house_id", arrObj.getString("house_id"));
//                            curObj.put("type", arrObj.getString("type"));
//                            curObj.put("dataNo", arrObj.getInt("dataNo"));
//                            curObj.put("value", arrObj.getInt("value"));
//                            curObj.put("date", arrObj.getString("date"));
//                            curObj.put("time", arrObj.getString("time"));
//
//                        } else {
//                            Log.e("Error", "masok 2");
//                            data.put("house_id", arrObj.getString("house_id"));
//                            data.put("type", arrObj.getString("type"));
//                            data.put("dataNo", arrObj.getInt("dataNo"));
//                            data.put("value", arrObj.getInt("value"));
//                            data.put("date", arrObj.getString("date"));
//                            data.put("time", arrObj.getString("time"));
//                            trendRec.put(data);
//                        }
//                    }
//                }
            }
        }
    }

    private void checkNotification() throws JSONException {
        JSONArray myRec = null;
        if(getTopic.contains("/apps/alarm/smart_home")) {
            myRec = new JSONArray(msg);
            for (int i = 0; i < myRec.length(); i++) {
                JSONObject arrObj = myRec.getJSONObject(i);
                String id = arrObj.getString("house_id");
                String name = arrObj.getString("elder_name");
                String address = arrObj.getString("address");
                String date = arrObj.getString("date");
                String time = arrObj.getString("time");
                String type = arrObj.getString("type");
                String message = arrObj.getString("message");
                createNotification(address, name, date, time, type, message);
            }
        }
        if(getTopic.contains("/apps/alarm/wearable")) {
            myRec = new JSONArray(msg);
            for (int i = 0; i < myRec.length(); i++) {
                JSONObject arrObj = myRec.getJSONObject(i);
                String id = arrObj.getString("watch_id");
                String name = arrObj.getString("elder_name");
                String address = arrObj.getString("address");
                String date = arrObj.getString("date");
                String time = arrObj.getString("time");
                String type = arrObj.getString("type");
                String message = arrObj.getString("message");
                createNotification(address, name, date, time, type, message);
            }
        }
    }

    public void createNotification (String address, String elder_name, String date, String time, String type, String message) {

        Intent intent = new Intent(this, MainActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("goAct", "Notification");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE ) ;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext() , default_notification_channel_id ) ;
        mBuilder.setContentTitle("Elderly Care")
                .setContentText(type + ": Something went wrong on " + elder_name + " house!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message + "\nAddress : " + address))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_smarthome);
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager. IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID , "NOTIFICATION_CHANNEL_NAME" , importance) ;
            mBuilder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }
//        assert mNotificationManager != null;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        mNotificationManager.notify((int)System.currentTimeMillis() , mBuilder.build()) ;
        notificationManager.notify((int)System.currentTimeMillis() , mBuilder.build()) ;
    }

    private void wearableData() {
        Log.e("Wearable", "masok awal");
        if(getTopic.contains("/apps/wearable/heart_rate")) {
            Log.e("Wearable", "masok 1");
            JSONObject heartRate = new JSONObject();
            JSONObject myRec = null;
            try {
                myRec = new JSONObject(msg);
                heartRate.put("watch_id", myRec.getString("watch_id"));
                heartRate.put("hr", myRec.getInt("hr"));
                HrData.put(heartRate);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(getTopic.contains("/apps/wearable/onbody")) {
            Log.e("Wearable", "masok 2");
            JSONObject onbody = new JSONObject();
            JSONObject myRec = null;
            try {
                myRec = new JSONObject(msg);
                onbody.put("watch_id", myRec.getString("watch_id"));
                onbody.put("onbody", myRec.getString("onbody"));
                onBody.put(onbody);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(getTopic.contains("/apps/wearable/trend")) {
            Log.e("Wearable", "masok 3");
            JSONArray myRec = null;
            try {
                myRec = new JSONArray(mqttServices.msg);
                for (int i = myRec.length() - 1; i >= 0; i--) {
                    JSONObject data = new JSONObject();
                    JSONObject arrObj = myRec.getJSONObject(i);

                    for (int x = 0; x < HrTrendRec.length(); x++) {
                        if ((Objects.equals(arrObj.getString("watch_id"), HrTrendRec.getJSONObject(x).getString("watch_id"))) &&
                                (Objects.equals(arrObj.getString("type"), HrTrendRec.getJSONObject(x).getString("type"))) &&
                                (Objects.equals(arrObj.getInt("dataNo"), HrTrendRec.getJSONObject(x).getInt("dataNo")))) {
                            HrTrendRec.remove(x);
                        }
                    }

                    data.put("watch_id", arrObj.getString("watch_id"));
                    data.put("type", arrObj.getString("type"));
                    data.put("dataNo", arrObj.getInt("dataNo"));
                    data.put("value", arrObj.getInt("value"));
                    data.put("date", arrObj.getString("date"));
                    data.put("time", arrObj.getString("time"));
                    HrTrendRec.put(data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}