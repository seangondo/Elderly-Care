package com.tugasakhir.elderlycare.ui;

import static android.view.View.VISIBLE;
import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;
import static com.tugasakhir.elderlycare.ui.MainActivity.client;
import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tugasakhir.elderlycare.handler.DBHandler;
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.databinding.ActivityMain2Binding;
import com.tugasakhir.elderlycare.service.mqttServices;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity2 extends AppCompatActivity {

    DBHandler myDb = new DBHandler(this);

    TextView tv1, tvAuto;
    Button b1;
    ImageView img;
    ImageButton imgBut;
    TextView eName, eAddress;

    LinearLayout layout;

    CardView dropMenu, btn1, btn2, btn3;

    public static SwitchCompat swAuto;

    //Data
    public static String living_temp, living_light, kitchen_light, kitchen_gas;

    String myid;
    public static ActivityMain2Binding binding;

    private View.OnClickListener menuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu1 :
                    // TODO elder change menu
                    Log.e("Menu", "Elder settings");
                    break;
                case R.id.menu2 :
                    goToHome();
                    break;
                case R.id.menu3 :
                    // TODO Menu logout
                    Log.e("Menu", "Logout!");
                    logout();
                    break;
            }
        }
    };

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
                case R.id.sw_auto:
                    if(swAuto.isChecked()) {
                        try {
                            client.publish(elderSelected +"/apps/control_button/automatic_mode", "{\"value\": \"true\", \"var\": 1}".getBytes(),0, true);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            client.publish(elderSelected+"/apps/control_button/automatic_mode", "{\"value\": \"false\", \"var\": 1}".getBytes(),0, true);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
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
        tvAuto = (TextView) findViewById(R.id.AutoSmarthome);
        eName = (TextView) findViewById(R.id.elderName);
        eAddress = (TextView) findViewById(R.id.elderAddr);

        img = (ImageView) findViewById(R.id.elderImg);
        imgBut = (ImageButton) findViewById(R.id.imgButton);
        layout = (LinearLayout) findViewById(R.id.linearLayout);

        btn1 = (CardView) findViewById(R.id.menu1);
        btn2 = (CardView) findViewById(R.id.menu2);
        btn3 = (CardView) findViewById(R.id.menu3);

        btn1.setOnClickListener(menuClickListener);
        btn2.setOnClickListener(menuClickListener);
        btn3.setOnClickListener(menuClickListener);

        swAuto = (SwitchCompat) findViewById(R.id.sw_auto);
        b1 = findViewById(R.id.emergency);

        dropMenu = (CardView) findViewById(R.id.dropSetting);
        dropMenu.setVisibility(View.GONE);

        swAuto.setOnClickListener(myClickList);
        b1.setOnClickListener(myClickList);

        setData();

        binding.navigationBar.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.overview:
                    replaceFragment(new Overview());
                    tv1.setText("Overview");
                    swAuto.setVisibility(View.INVISIBLE);
                    tvAuto.setVisibility(View.INVISIBLE);
                    break;
                case R.id.smartHome:
                    replaceFragment(new SmartHome());
                    tv1.setText("Smart Home");
                    swAuto.setVisibility(VISIBLE);
                    tvAuto.setVisibility(VISIBLE);
                    break;
                case R.id.telepresence:
                    replaceFragment(new Telepresence());
                    tv1.setText("Telepresence");
                    swAuto.setVisibility(View.INVISIBLE);
                    tvAuto.setVisibility(View.INVISIBLE);
                    break;
                case R.id.wearable:
                    replaceFragment(new Wearable());
                    tv1.setText("Wearable");
                    swAuto.setVisibility(View.INVISIBLE);
                    tvAuto.setVisibility(View.INVISIBLE);
                    break;
            }
            return true;
        });

        imgBut.setOnClickListener(view -> {
            if (dropMenu.getVisibility() == VISIBLE) {
                TransitionManager.beginDelayedTransition(layout, new AutoTransition());
                dropMenu.setVisibility(View.GONE);
                imgBut.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
            } else {
                TransitionManager.beginDelayedTransition(layout, new AutoTransition());
                dropMenu.setVisibility(VISIBLE);
                imgBut.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
            }
        });
    }

    private void setData(){
        JSONObject obj = new JSONObject();
        try {
            obj = myDb.getElderData(elderSelected);
            Log.e("Hasil", String.valueOf(obj.getString("image")));
            String imgUrl = myServer+"/image/"+obj.getString("image");
            eName.setText(obj.getString("name"));
            eAddress.setText(obj.getString("address"));
            Glide.with(MainActivity2.this).asBitmap().load(imgUrl)  .into(img);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void replaceFragment(Fragment f) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, f);
        fragmentTransaction.commit();
    }

    private void goToHome() {
        Intent i = new Intent(this, ElderSelectorActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    private void logout() {
        myDb.deleteElderAll();
        myDb.deleteLoginAll();
        myDb.deleteAllSensor();
        myDb.deleteAllButton();
//        myDb.deleteLogin("caregiver_info", myUser);
        stopService(new Intent(this, mqttServices.class));
        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e("Disconnect MQTT", "Success!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("Disconnect MQTT", "Failed! " + exception);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        Toast.makeText(MainActivity2.this, "Logout Success!", Toast.LENGTH_LONG).show();

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }
}