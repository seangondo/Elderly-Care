package com.tugasakhir.elderlycare;

import static com.tugasakhir.elderlycare.MainActivity.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;

import com.tugasakhir.elderlycare.databinding.ActivityMain2Binding;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity2 extends AppCompatActivity {

    TextView text;
    public static ActivityMain2Binding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        text = (TextView) findViewById(R.id.tv2);

        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.navigationBar.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.overview:
                    Log.d("Halaman", "Overview");
                    replaceFragment(new Overview());
                    break;
                case R.id.smartHome:
                    Log.d("Halaman", "SmartHome");
                    replaceFragment(new SmartHome());
                    break;
                case R.id.telepresence:
                    Log.d("Halaman", "Telepresence");
                    replaceFragment(new Telepresence());
                    break;
                case R.id.wearable:
                    Log.d("Halaman", "Wearable");
                    replaceFragment(new Wearable());
                    break;
            }
            return true;
        });

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                JSONArray myRec = null;
                String msg = new String(message.getPayload());
                Log.d("Message", msg);
                JSONObject arrObj = new JSONObject(msg);
                String a = arrObj.getString("a");
                String b = arrObj.getString("b");
                String c = arrObj.getString("c");
                Log.d(topic, String.valueOf(arrObj));
                text.setText(a);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    void replaceFragment(Fragment f) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, f);
        fragmentTransaction.commit();
    }

}