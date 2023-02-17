package com.tugasakhir.elderlycare;

import static com.tugasakhir.elderlycare.MainActivity.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tugasakhir.elderlycare.databinding.ActivityMain2Binding;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity2 extends AppCompatActivity {

    TextView tv1;
    Button b1;

    //Data
    public static String living_temp, living_light, kitchen_light, kitchen_gas;

    String myid;
    public static ActivityMain2Binding binding;

    private View.OnClickListener myClickList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.emergency:
                    Log.d("Call", "Start Call");
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:081330679849"));
                    startActivity(callIntent);
                    break;

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);

        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new Overview());

        tv1 = (TextView) findViewById(R.id.tv2);
        b1 = findViewById(R.id.emergency);

        b1.setOnClickListener(myClickList);

        binding.navigationBar.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.overview:
                    replaceFragment(new Overview());
                    tv1.setText("Overview");
                    break;
                case R.id.smartHome:
                    replaceFragment(new SmartHome());
                    tv1.setText("Smart Home");
                    break;
                case R.id.telepresence:
                    replaceFragment(new Telepresence());
                    tv1.setText("Telepresence");
                    break;
                case R.id.wearable:
                    replaceFragment(new Wearable());
                    tv1.setText("Wearable");
                    break;
            }
            return true;
        });

//        client.setCallback(new MqttCallback() {
//            @Override
//            public void connectionLost(Throwable cause) {
//
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                JSONArray myRec = null;
//                String msg = new String(message.getPayload());
//                Log.d("Message", msg);
//
//                myRec = new JSONArray(msg);
//                for (int i = 0; i < myRec.length(); i++) {
//                    JSONObject arrObj = myRec.getJSONObject(i);
//                    Log.d("Message", String.valueOf(arrObj));
//                    living_temp = arrObj.getString("living_temp");
//                    living_light = arrObj.getString("living_light");
//                    kitchen_light = arrObj.getString("kitchen_light");
//                    kitchen_gas = arrObj.getString("kitchen_gas");
//                    Log.d("Living Room Temp : ", living_temp);
//                    Log.d("Living Room Light : ", living_light);
//                    Log.d("Kitchen Light : ", kitchen_light);
//                    Log.d("Kitchen Gas : ", kitchen_gas);
//                }
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken token) {
//
//            }
//        });
    }
//    public void startUpdater() {
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Intent.ACTION_TIME_TICK);
//        updater = new BroadcastReceiver() {
//
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                counter++;
//                Log.d("Tes", String.valueOf(counter));
//            }
//        };
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        startUpdater();
//    }
//
//    @Override
//    protected void onPause(){
//        super.onPause();
//        unregisterReceiver(updater);
//    }

    void replaceFragment(Fragment f) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, f);
        fragmentTransaction.commit();
    }

}