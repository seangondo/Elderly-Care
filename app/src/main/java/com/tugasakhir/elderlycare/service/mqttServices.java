package com.tugasakhir.elderlycare.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.handler.DBHandler;
import com.tugasakhir.elderlycare.model.TrendReceive;
import com.tugasakhir.elderlycare.ui.MainActivity2;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;

public class mqttServices extends Service {

    public static final String NOTIFICATION_CHANNEL_ID = "ElderlyCare" ;
    public static final String NOTIFICATION_CHANNEL_FORE = "10001" ;
    private final static String default_notification_channel_id = "default" ;

    public static ArrayList<String> splitTopic = new ArrayList<>();

    public static String msg;
    public static String getTopic;

    public static JSONArray buttonSmartHome = new JSONArray();

    public static JSONArray sensorSmartHome = new JSONArray();

    public static JSONArray poseDetectionLiv1 = new JSONArray();
    public static JSONArray poseDetectionLiv2 = new JSONArray();
    public static JSONArray poseDetectionDining = new JSONArray();

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



    // INITIALIZE MQTT CONNECTION AND SERVICES
    public static MqttAndroidClient client;
    public String serverUri;
    String clientID, mqttUser, mqttPass;

    //DB Local
    DBHandler myDb = new DBHandler(this);

    public mqttServices() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Service", "MQTT Service called!");

        createNotificationChannel();
        Notification notification = createNotification();
        startForeground(1, notification);
//        stopForeground(true);

        // MQTT INIT
        serverUri = getString(R.string.ipServer);
        clientID = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), serverUri, clientID, Ack.AUTO_ACK);
        mqttUser = getString(R.string.mqttUser);
        mqttPass = getString(R.string.mqttPass);

        ArrayList<Integer> data = new ArrayList<>();

        try {
            JSONArray elderData = myDb.getElderDataAll();
            for(int i = 0; i < elderData.length(); i++) {
                JSONObject obj = elderData.getJSONObject(i);
                data.add(obj.getInt("elder_id"));
            }
            startMqtt(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e("Connection MQTT", String.valueOf(cause));
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                getTopic = topic;
                msg = new String(message.getPayload());
                splitTopic = splitTopic(getTopic + "/");
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

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mSensorManager.unregisterListener(this);;
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restart_service");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_FORE,
                    "MQTT Service Channel",
                    NotificationManager.IMPORTANCE_MIN
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_FORE)
                .setContentTitle("MQTT Service")
                .setContentText("MQTT service is running")
                .setSmallIcon(R.drawable.ic_smarthome);

        return builder.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void smartHomeButton() throws JSONException {
        JSONObject arrObj = new JSONObject(msg);

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
            int val = dataExistButton(buttonSmartHome,
                    "house_id", elderData.getString("house_id"),
                    "room", splitTopic.get(3)
                    , "type", splitTopic.get(4));
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

                JSONObject obj = new JSONObject();
                DBHandler myDb = new DBHandler(this);
                JSONObject elderData = myDb.getElderData(Integer.parseInt(splitTopic.get(0)));
                myDb.close();
                obj.put("house_id", elderData.getString("house_id"));
                obj.put("living_temp", arrObj.getString("livingroom_temp"));
                obj.put("living_light", arrObj.getString("livingroom_light"));
                obj.put("kitchen_light", arrObj.getString("kitchen_light"));
                obj.put("kitchen_gas", arrObj.getString("kitchen_gas"));

                int val = dataExist(sensorSmartHome, "house_id", elderData.getString("house_id"));
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
        if(getTopic.contains("/apps/trend")) {
            myRec = new JSONArray(mqttServices.msg);

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
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_smarthome);
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager. IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID , "Elderly Care Apps" , importance) ;
            mBuilder.setChannelId( NOTIFICATION_CHANNEL_ID );
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
        if(getTopic.contains("/apps/pose/living1")) {
            JSONObject pose = new JSONObject();
            JSONObject myRec = null;
            try {
                myRec = new JSONObject(msg);
                pose.put("house_id", myRec.getString("house_id"));
                pose.put("camera", myRec.getString("camera"));
                pose.put("pose", myRec.getString("pose"));
                pose.put("alarm", myRec.getBoolean("alarm"));
                int val = dataExist(poseDetectionLiv1, "camera", myRec.getString("camera"));
        //                int val1 = dataExist(poseDetection, "house_id", myRec.getString("house_id"));
                if(val != -1) {
                    poseDetectionLiv1.put(val, pose);
                } else {
                    poseDetectionLiv1.put(pose);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(getTopic.contains("/apps/pose/living2")) {
            JSONObject pose = new JSONObject();
            JSONObject myRec = null;
            try {
                myRec = new JSONObject(msg);
                pose.put("house_id", myRec.getString("house_id"));
                pose.put("camera", myRec.getString("camera"));
                pose.put("pose", myRec.getString("pose"));
                pose.put("alarm", myRec.getBoolean("alarm"));
                int val = dataExist(poseDetectionLiv2, "camera", myRec.getString("camera"));
//                int val1 = dataExist(poseDetection, "house_id", myRec.getString("house_id"));
                if(val != -1) {
                    poseDetectionLiv2.put(val, pose);
                } else {
                    poseDetectionLiv2.put(pose);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(getTopic.contains("/apps/pose/dining")) {
            JSONObject pose = new JSONObject();
            JSONObject myRec = null;
            try {
                myRec = new JSONObject(msg);
                pose.put("house_id", myRec.getString("house_id"));
                pose.put("camera", myRec.getString("camera"));
                pose.put("pose", myRec.getString("pose"));
                pose.put("alarm", myRec.getBoolean("alarm"));
                int val = dataExist(poseDetectionDining, "camera", myRec.getString("camera"));
//                int val1 = dataExist(poseDetection, "house_id", myRec.getString("house_id"));
                if(val != -1) {
                    poseDetectionDining.put(val, pose);
                } else {
                    poseDetectionDining.put(pose);
                }
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
        for(int i = 0; i < arr.length(); i++) {
            try {
                JSONObject data = arr.getJSONObject(i);

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

    private void startMqtt(ArrayList<Integer> getData) {
        Log.d("Token", String.valueOf(client.isConnected()));
        if(!client.isConnected()){
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setConnectionTimeout(3000);
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setUserName(mqttUser);
            mqttConnectOptions.setPassword(mqttPass.toCharArray());
            IMqttToken token = client.connect(mqttConnectOptions);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //setSubscription();
//                        ArrayList<Integer> getData = subsElder(data);
                    for (int i = 0; i < getData.size(); i++ ) {
                        String subscriptionTopic = String.valueOf(getData.get(i))+"/#";
                        Log.d("Topic", subscriptionTopic);
                        subscribeToTopic(subscriptionTopic);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        }
    }

    private void subscribeToTopic(String topic) {
        client.subscribe(topic, 0, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.w("Mqtt","Subscribed : " + topic);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.w("Mqtt", "Subscribed fail!");
            }
        });
    }
}