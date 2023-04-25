package com.tugasakhir.elderlycare.service;

import static com.tugasakhir.elderlycare.ui.MainActivity.client;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.tugasakhir.elderlycare.ui.MainActivity2;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class notificationServices extends Service {


    private static String msg;
    private static String getTopic;

    public static final String NOTIFICATION_CHANNEL_ID = "90001" ;
    private final static String default_notification_channel_id = "default" ;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e("Connection MQTT", String.valueOf(cause));
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                getTopic = topic;
                msg = new String(message.getPayload());
                Log.d("MQTT Topic", getTopic);
                checkNotification();

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkNotification() throws JSONException {
        JSONArray myRec = null;
        Log.e("MASOK", "Notification");
        if(getTopic.contains("/apps/alarm")) {
            Log.e("MASOK", "Notification");
            myRec = new JSONArray(mqttServices.msg);
            for (int i = 0; i < myRec.length(); i++) {
                JSONObject arrObj = myRec.getJSONObject(i);
                String id = arrObj.getString("house_id");
                String date = arrObj.getString("date");
                String time = arrObj.getString("time");
                String type = arrObj.getString("type");
                String message = arrObj.getString("message");
                createNotification(id, date, time, type, message);
            }
        }
    }

    public void createNotification (String house_id, String date, String time, String type, String message) {
        Intent intent = new Intent(this, MainActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("goAct", "Notification");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE ) ;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext() , default_notification_channel_id ) ;
        mBuilder.setContentTitle("Elderly Care");
        mBuilder.setContentText(type + " Something went wrong on House ID : " + house_id + "!");
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(pendingIntent);
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager. IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID , "NOTIFICATION_CHANNEL_NAME" , importance) ;
            mBuilder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(( int ) System. currentTimeMillis () , mBuilder.build()) ;
    }
}
