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
import com.tugasakhir.elderlycare.handler.DBHandler;
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

    public static ArrayList<String> splitTopic = new ArrayList<>();

    public static String msg;
    public static String getTopic;
//    public static String living_temp, living_light, kitchen_light, kitchen_gas, bLampLiving, bLampKitchen, bFanLiving, bAutoMode;
//
//    public static List<String> kitchen_date, kitchen_time, living_date, living_time;
//    public static List<Integer> living_no, living_val, kitchen_no, kitchen_val;

//    public static JSONArray livingTemp = new JSONArray();
//    public static JSONArray livingLight = new JSONArray();
//    public static JSONArray lampLiving = new JSONArray();
//    public static JSONArray fanLiving = new JSONArray();

    public static JSONArray buttonSmartHome = new JSONArray();

    public static JSONArray sensorSmartHome = new JSONArray();

//    public static JSONArray kitchenGas = new JSONArray();
//    public static JSONArray kitchenLight = new JSONArray();
//    public static JSONArray lampKitchen = new JSONArray();
//    public static JSONArray autoMode = new JSONArray();


//    public static JSONArray trendKitchenGas = new JSONArray();
//    public static JSONArray trendLivingTemp = new JSONArray();

    public static JSONArray poseDetection = new JSONArray();

    public static JSONArray trendRec = new JSONArray();
    public static JSONArray HrTrendRec = new JSONArray();
    public static JSONArray StepsTrend = new JSONArray();
    public static JSONArray CalsTrend = new JSONArray();
    public static JSONArray HrData = new JSONArray();
    public static JSONArray onBody = new JSONArray();
    public static JSONArray StepsData = new JSONArray();
    public static JSONArray CalsData = new JSONArray();

    public static JSONArray statusRobot = new JSONArray();

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
//                Log.d("Mqtt Msg", msg);
                splitTopic = splitTopic(getTopic + "/");
//                Log.e("Split", String.valueOf(splitTopic));
                getStatus();
                getPose();
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
//        if(getTopic.contains("/apps/control_button/livingroom/fan")) {
//            bFanLiving = arrObj.getString("value");
//
////            JSONObject obj = new JSONObject();
////            obj.put("elder_id", splitTopic.get(0));
////            obj.put("value", arrObj.getString("value"));
////            fanLiving.put(obj);
//        }
//        if(getTopic.contains("/apps/control_button/livingroom/light")) {
//            bLampLiving = arrObj.getString("value");
//
////            JSONObject obj = new JSONObject();
////            obj.put("elder_id", splitTopic.get(0));
////            obj.put("value", arrObj.getString("value"));
////            lampLiving.put(obj);
//        }
//        if(getTopic.contains("/apps/control_button/kitchen/light")) {
//            bLampKitchen = arrObj.getString("value");
//
////            JSONObject obj = new JSONObject();
////            obj.put("elder_id", splitTopic.get(0));
////            obj.put("value", arrObj.getString("value"));
////            lampKitchen.put(obj);
//        }
//        if(getTopic.contains("/apps/control_button/automatic_mode")) {
//            bAutoMode = arrObj.getString("value");
//
////            JSONObject obj = new JSONObject();
////            obj.put("elder_id", splitTopic.get(0));
////            obj.put("value", arrObj.getString("value"));
////            autoMode.put(obj);
//        }

        if(splitTopic.get(2).equals("control_button")) {
            JSONObject obj = new JSONObject();

            DBHandler myDb = new DBHandler(this);
            JSONObject elderData = myDb.getElderData(Integer.parseInt(splitTopic.get(0)));
            myDb.close();
            obj.put("house_id", elderData.getString("house_id"));
            obj.put("room", splitTopic.get(3));
            if(!splitTopic.get(3).equals("automatic_mode")) {
                obj.put("type", splitTopic.get(4));
            }
            obj.put("value", arrObj.getString("value"));
//            int val = dataExist(buttonSmartHome, "house_id", elderData.getString("house_id"));
//            int val1 = dataExist(buttonSmartHome, "room", splitTopic.get(3));
//            int val2 = dataExist(buttonSmartHome, "type", splitTopic.get(4));
            int val = dataExistButton(buttonSmartHome,
                    "house_id", elderData.getString("house_id"),
                    "room", splitTopic.get(3)
                    , "type", splitTopic.get(4));
//            Log.e("VAL", String.valueOf(buttonSmartHome.length()));
            if(val != -1) {
                buttonSmartHome.put(val, obj);
            } else {
                buttonSmartHome.put(obj);
            }
        }
    }

    private void smartHomePieChart() throws JSONException {
        JSONArray myRec = null;
        if(getTopic.contains("/apps/data")) {
            myRec = new JSONArray(mqttServices.msg);
            for (int i = 0; i < myRec.length(); i++) {
                JSONObject arrObj = myRec.getJSONObject(i);
//                living_temp = arrObj.getString("livingroom_temp");
//                living_light = arrObj.getString("livingroom_light");
//                kitchen_light = arrObj.getString("kitchen_light");
//                kitchen_gas = arrObj.getString("kitchen_gas");

                JSONObject obj = new JSONObject();
                DBHandler myDb = new DBHandler(this);
                JSONObject elderData = myDb.getElderData(Integer.parseInt(splitTopic.get(0)));
                myDb.close();
                obj.put("house_id", elderData.getString("house_id"));
                obj.put("living_temp", arrObj.getString("livingroom_temp"));
                obj.put("living_light", arrObj.getString("livingroom_light"));
                obj.put("kitchen_light", arrObj.getString("kitchen_light"));
                obj.put("kitchen_gas", arrObj.getString("kitchen_gas"));

//                sensorSmartHome.put(obj);

                int val = dataExist(sensorSmartHome, "house_id", elderData.getString("house_id"));
//                Log.e("smart home Sensor", String.valueOf(sensorSmartHome));
                if(val != -1) {
                    sensorSmartHome.put(val, obj);
                } else {
                    sensorSmartHome.put(obj);
                }

            }
        }
    }

    private void smartHomeLogging() throws JSONException {
        JSONArray myRec = null;
//        List<Integer> no = new ArrayList<>();
//        List<Integer> val = new ArrayList<>();
//        List<String> date = new ArrayList<>();
//        List<String> time = new ArrayList<>();
//
//        ArrayList<TrendReceive> trend = new ArrayList<>();
//
//        if(getTopic.contains("/apps/trend/kitchen_gas")) {
//            myRec = new JSONArray(mqttServices.msg);
//            for (int i = 0; i < myRec.length(); i++) {
//                JSONObject arrObj = myRec.getJSONObject(i);
//                no.add(arrObj.getInt("dataNo"));
//                val.add(arrObj.getInt("value"));
//                date.add(arrObj.getString("date"));
//                time.add(arrObj.getString("time"));
//            }
//            Collections.reverse(no);
//            Collections.reverse(val);
//            Collections.reverse(date);
//            Collections.reverse(time);
//            kitchen_no = no;
//            kitchen_val = val;
//            kitchen_date = date;
//            kitchen_time = time;
//
//            Collections.reverse(trend);
//        }
//        if(getTopic.contains("/apps/trend/livingroom_temp")) {
//            myRec = new JSONArray(mqttServices.msg);
//            for (int i = 0; i < myRec.length(); i++) {
//                JSONObject arrObj = myRec.getJSONObject(i);
//                no.add(arrObj.getInt("dataNo"));
//                val.add(arrObj.getInt("value"));
//                date.add(arrObj.getString("date"));
//                time.add(arrObj.getString("time"));
//            }
//            Collections.reverse(no);
//            Collections.reverse(val);
//            Collections.reverse(date);
//            Collections.reverse(time);
//            living_no = no;
//            living_val = val;
//            living_date = date;
//            living_time = time;
//        }

        // CARA 2
        if(getTopic.contains("/apps/trend")) {
            myRec = new JSONArray(mqttServices.msg);
//            Log.e("Data", String.valueOf(trendRec));
//            Log.e("Input trend", String.valueOf(myRec.length()));
//            Log.e("trendRec", String.valueOf(trendRec.length()));

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
        if(getTopic.contains("/apps/wearable/heart_rate")) {
            JSONObject heartRate = new JSONObject();
            JSONObject myRec = null;
            try {
                myRec = new JSONObject(msg);
                heartRate.put("watch_id", myRec.getString("watch_id"));
                heartRate.put("hr", myRec.getInt("hr"));
                int val = dataExist(HrData, "watch_id", myRec.getString("watch_id"));
                if(val != -1) {
                    HrData.put(val, heartRate);
                } else {
                    HrData.put(heartRate);
                }
//                HrData.put(heartRate);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(getTopic.contains("/apps/wearable/steps")) {
            JSONObject steps = new JSONObject();
            JSONObject myRec = null;
            try {
                myRec = new JSONObject(msg);
                steps.put("watch_id", myRec.getString("watch_id"));
                steps.put("steps", myRec.getInt("steps"));
                int val = dataExist(StepsData, "watch_id", myRec.getString("watch_id"));
                if(val != -1) {
                    StepsData.put(val, steps);
                } else {
                    StepsData.put(steps);
                }
//                StepsData.put(steps);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(getTopic.contains("/apps/wearable/calories")) {
            JSONObject calories = new JSONObject();
            JSONObject myRec = null;
            try {
                myRec = new JSONObject(msg);
                calories.put("watch_id", myRec.getString("watch_id"));
                calories.put("calories", myRec.getInt("calories"));
                int val = dataExist(CalsData, "watch_id", myRec.getString("watch_id"));
                if(val != -1) {
                    CalsData.put(val, calories);
                } else {
                    CalsData.put(calories);
                }
//                CalsData.put(calories);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(getTopic.contains("/apps/wearable/onbody")) {
            JSONObject onbody = new JSONObject();
            JSONObject myRec = null;
            try {
                myRec = new JSONObject(msg);
                onbody.put("watch_id", myRec.getString("watch_id"));
                onbody.put("onbody", myRec.getString("onbody"));
                int val = dataExist(onBody, "watch_id", myRec.getString("watch_id"));
                if(val != -1) {
                    onBody.put(val, onbody);
                } else {
                    onBody.put(onbody);
                }
//                onBody.put(onbody);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(getTopic.contains("/apps/wearable/trend")) {
            JSONArray myRec = null;
//            Log.e("Topic Wearable Trend", getTopic);
            try {
                myRec = new JSONArray(mqttServices.msg);
                for (int i = myRec.length() - 1; i >= 0; i--) {
                    JSONObject data = new JSONObject();
                    JSONObject arrObj = myRec.getJSONObject(i);
//                    Log.e("Topic Wearable Trend", arrObj.getString("type"));

                    if(arrObj.getString("type").equals("heart rate")) {

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
                    if(arrObj.getString("type").equals("steps")) {

                        for (int x = 0; x < StepsTrend.length(); x++) {
                            if ((Objects.equals(arrObj.getString("watch_id"), StepsTrend.getJSONObject(x).getString("watch_id"))) &&
                                    (Objects.equals(arrObj.getString("type"), StepsTrend.getJSONObject(x).getString("type"))) &&
                                    (Objects.equals(arrObj.getInt("dataNo"), StepsTrend.getJSONObject(x).getInt("dataNo")))) {
                                StepsTrend.remove(x);
                            }
                        }

                        data.put("watch_id", arrObj.getString("watch_id"));
                        data.put("type", arrObj.getString("type"));
                        data.put("dataNo", arrObj.getInt("dataNo"));
                        data.put("value", arrObj.getInt("value"));
                        data.put("date", arrObj.getString("date"));
                        data.put("time", arrObj.getString("time"));
                        StepsTrend.put(data);
                    }
                    if(arrObj.getString("type").equals("calories")) {

                        for (int x = 0; x < CalsTrend.length(); x++) {
                            if ((Objects.equals(arrObj.getString("watch_id"), CalsTrend.getJSONObject(x).getString("watch_id"))) &&
                                    (Objects.equals(arrObj.getString("type"), CalsTrend.getJSONObject(x).getString("type"))) &&
                                    (Objects.equals(arrObj.getInt("dataNo"), CalsTrend.getJSONObject(x).getInt("dataNo")))) {
                                CalsTrend.remove(x);
                            }
                        }

                        data.put("watch_id", arrObj.getString("watch_id"));
                        data.put("type", arrObj.getString("type"));
                        data.put("dataNo", arrObj.getInt("dataNo"));
                        data.put("value", arrObj.getInt("value"));
                        data.put("date", arrObj.getString("date"));
                        data.put("time", arrObj.getString("time"));
                        CalsTrend.put(data);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getPose() {
        if(getTopic.contains("/apps/pose")) {
            JSONObject pose = new JSONObject();
            JSONObject myRec = null;
            try {
                myRec = new JSONObject(msg);
                pose.put("house_id", myRec.getString("house_id"));
                pose.put("camera", myRec.getString("camera"));
                pose.put("pose", myRec.getString("pose"));
                pose.put("alarm", myRec.getBoolean("alarm"));
                int val = dataExist(poseDetection, "camera", myRec.getString("camera"));
//                int val1 = dataExist(poseDetection, "house_id", myRec.getString("house_id"));
                if(val != -1) {
                    poseDetection.put(val, pose);
                } else {
                    poseDetection.put(pose);
                }
//                poseDetection.put(pose);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getStatus() {

        if(getTopic.contains("/apps/robot/status")) {
            JSONObject status = new JSONObject();
            JSONObject myRec = null;
            try {
                myRec = new JSONObject(msg);
                status.put("robot_id", myRec.getString("robot_id"));
                status.put("status", myRec.getString("status"));
                int val = dataExist(statusRobot, "robot_id", myRec.getString("robot_id"));
//                int val1 = dataExist(statusRobot, "status", myRec.getString("status"));
                if(val != -1) {
                    statusRobot.put(val, status);
                } else {
                    statusRobot.put(status);
                }
//                Log.e("Status", String.valueOf(statusRobot));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public int dataExist(JSONArray arr, String objName, String value) {
        for(int i = 0; i < arr.length(); i++) {
            try {
                JSONObject obj = arr.getJSONObject(i);
                if(obj.getString(objName).equals(value)) {
                    return i;
                } else {
                    return -1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public int dataExistButton(JSONArray arr,
                               String objName1,
                               String value1,
                               String objName2,
                               String value2,
                               String objName3,
                               String value3) {
//        Log.e("Count", String.valueOf(arr.length()));
        for(int i = 0; i < arr.length(); i++) {
            try {
                JSONObject data = arr.getJSONObject(i);
//                Log.e("Data I", String.valueOf(data));
//                Log.e("Data I", data.getString(objName1));
//                Log.e("Data I", data.getString(objName2));
//                Log.e("Data I", data.getString(objName3));

                if(data.getString(objName1).equals(value1)
                        && data.getString(objName2).equals(value2)
                        && data.getString(objName3).equals(value3)) {
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public ArrayList<String> splitTopic(String topic) {
        ArrayList<String> topicSplit = new ArrayList<>();
        int i = 0;
        int index = 0;

        while (index >= 0) {
            int start = index;
            index = topic.indexOf("/", index);
            if(index != -1) {
                topicSplit.add(topic.substring(start, index));
                index++;
                i++;
            } else {
                break;
            }
        }
        return topicSplit;
    }
}