package com.tugasakhir.elderlycare.ui;

import static android.view.View.VISIBLE;
import static com.tugasakhir.elderlycare.service.mqttServices.client;
import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;
import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.api.RetrofitAPI;
import com.tugasakhir.elderlycare.databinding.ActivityMain2Binding;
import com.tugasakhir.elderlycare.handler.DBHandler;
import com.tugasakhir.elderlycare.service.mqttServices;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity2 extends AppCompatActivity {

    public static String title;

    private static Context context;

    private int countNow, countOld;

    public static Context getAppContext() {
        return MainActivity2.context;
    }

    DBHandler myDb = new DBHandler(this);

    TextView tv1, tvAuto;
    Button b1;
    ImageView img;
    ImageButton imgBut;
    TextView eName, eAddress;

    LinearLayout layout;
    LinearLayout dropMenu;

    CardView btn1, btn2, btn3, btn4, btn5;

    TextView notifCount;
    CardView notif;

    public static SwitchCompat swAuto;

    public static String globalText;

    //Updater
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 1000;

    //Data
    public static String living_temp, living_light, kitchen_light, kitchen_gas;

    String myid;
    public static ActivityMain2Binding binding;

    private View.OnClickListener menuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                // MENU CHANGE CAREGIVER INFO
                case R.id.menu1 :
                    goToCaregiverEdit();
                    break;

                // MENU EDIT ELDER INFO
                case R.id.menu2 :
                    goElderEdit();
                    break;

                // MENU CHANGE ELDER
                case R.id.menu3 :
                    goToHome();
                    break;

                // MENU ADD ELDER
                case R.id.menu4 :
                    goToAddElder();
                    break;

                // MENU LOGOUT
                case R.id.menu5 :
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
                    callIntent.setData(Uri.parse("tel:112"));
                    startActivity(callIntent);
                    break;
                case R.id.sw_auto:
                    if(swAuto.isChecked()) {
                        client.publish(elderSelected +"/apps/control_button/automatic_mode", "{\"value\": \"true\", \"var\": 1}".getBytes(),0, true);
                    } else {
                        client.publish(elderSelected+"/apps/control_button/automatic_mode", "{\"value\": \"false\", \"var\": 1}".getBytes(),0, true);
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity2.context = getAppContext();

        setContentView(R.layout.activity_main2);

        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        notifCount = (TextView) findViewById(R.id.numberNotification);
        notif = (CardView) findViewById(R.id.notificationNumber);

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
        btn4 = (CardView) findViewById(R.id.menu4);
        btn5 = (CardView) findViewById(R.id.menu5);

        btn1.setOnClickListener(menuClickListener);
        btn2.setOnClickListener(menuClickListener);
        btn3.setOnClickListener(menuClickListener);
        btn4.setOnClickListener(menuClickListener);
        btn5.setOnClickListener(menuClickListener);

        swAuto = (SwitchCompat) findViewById(R.id.sw_auto);
        b1 = findViewById(R.id.emergency);

        dropMenu = (LinearLayout) findViewById(R.id.dropSetting);
        dropMenu.setVisibility(View.GONE);

        swAuto.setOnClickListener(myClickList);
        b1.setOnClickListener(myClickList);

        setData();

        BroadcastReceiver setTitle = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("update_title")) {
                    title = intent.getStringExtra("title");
                    tv1.setText(title);
                    if(title.equals("Smart\nHome")) {
                        swAuto.setVisibility(VISIBLE);
                        tvAuto.setVisibility(VISIBLE);
                    } else {
                        swAuto.setVisibility(View.INVISIBLE);
                        tvAuto.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter("update_title");
        this.registerReceiver(setTitle, filter);


        if (savedInstanceState == null) {
            if(binding.navigationBar != null) {
                binding.navigationBar.setSelectedItemId(R.id.overview);
            } else if (binding.bottomnavigationBar != null) {
                binding.bottomnavigationBar.setSelectedItemId(R.id.overview);
            }
            replaceFragment(new Overview());
        }

        if(binding.navigationBar != null) {
            binding.navigationBar.setOnItemSelectedListener(item -> {
                switch (item.getItemId()) {
                    case R.id.overview:
                        replaceFragment(new Overview());
//                    tv1.setText("Overview");
//                    swAuto.setVisibility(View.INVISIBLE);
//                    tvAuto.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.smartHome:
                        replaceFragment(new SmartHome());
//                    tv1.setText("Smart \nHome");
//                    swAuto.setVisibility(VISIBLE);
//                    tvAuto.setVisibility(VISIBLE);
                        break;
                    case R.id.telepresence:
                        replaceFragment(new Telepresence());
//                    tv1.setText("Telepresence");
//                    swAuto.setVisibility(View.INVISIBLE);
//                    tvAuto.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.wearable:
                        replaceFragment(new Wearable());
//                    tv1.setText("Wearable");
//                    swAuto.setVisibility(View.INVISIBLE);
//                    tvAuto.setVisibility(View.INVISIBLE);
                        break;

//                case R.id.testing:
//                    replaceFragment(new TestTabLayout());
//                    tv1.setText("Testing");
//                    swAuto.setVisibility(View.INVISIBLE);
//                    tvAuto.setVisibility(View.INVISIBLE);
//                    break;
                }
                return true;
            });
        } else if (binding.bottomnavigationBar != null) {
            binding.bottomnavigationBar.setOnItemSelectedListener(item -> {
                switch (item.getItemId()) {
                    case R.id.overview:
                        replaceFragment(new Overview());
//                    tv1.setText("Overview");
//                    swAuto.setVisibility(View.INVISIBLE);
//                    tvAuto.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.smartHome:
                        replaceFragment(new SmartHome());
//                    tv1.setText("Smart \nHome");
//                    swAuto.setVisibility(VISIBLE);
//                    tvAuto.setVisibility(VISIBLE);
                        break;
                    case R.id.telepresence:
                        replaceFragment(new Telepresence());
//                    tv1.setText("Telepresence");
//                    swAuto.setVisibility(View.INVISIBLE);
//                    tvAuto.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.wearable:
                        replaceFragment(new Wearable());
//                    tv1.setText("Wearable");
//                    swAuto.setVisibility(View.INVISIBLE);
//                    tvAuto.setVisibility(View.INVISIBLE);
                        break;

//                case R.id.testing:
//                    replaceFragment(new TestTabLayout());
//                    tv1.setText("Testing");
//                    swAuto.setVisibility(View.INVISIBLE);
//                    tvAuto.setVisibility(View.INVISIBLE);
//                    break;
                }
                return true;
            });
        }

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
        
        getCountNotif();
    }

    public void getCountNotif() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(myServer+":8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<Object> call = retrofitAPI.getCountLog(String.valueOf(elderSelected));
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                String res = new Gson().toJson(response.body());
                try {
                    JSONObject obj = new JSONObject(res);
                    int count = obj.getInt("n");
                    countNow = count;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {

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
            Glide.with(MainActivity2.this).asBitmap().load(imgUrl).apply(new RequestOptions().override(300, 300)).centerCrop().into(img);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void replaceFragment(Fragment f) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, f);
        fragmentTransaction.commit();
        if (dropMenu.getVisibility() == VISIBLE) {
//            TransitionManager.beginDelayedTransition(layout, new AutoTransition());
            dropMenu.setVisibility(View.GONE);
            imgBut.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
        }
    }

    private void goToHome() {
        Intent i = new Intent(this, ElderSelectorActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    private void goElderEdit() {
        Intent i = new Intent(this, EditElderActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    private void goToAddElder() {
        Intent i = new Intent(this, AddElder.class);
        i.putExtra("starting", "overview");
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    private void logout() {
        myDb.deleteElderAll();
        myDb.deleteLoginAll();
        myDb.deleteAllSensor();
        myDb.deleteAllButton();
        myDb.deletePointAll();
//        myDb.deleteLogin("caregiver_info", myUser);
        stopService(new Intent(this, mqttServices.class));
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

        Toast.makeText(MainActivity2.this, "Logout Success!", Toast.LENGTH_LONG).show();

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    private void goToCaregiverEdit() {
        Intent i = new Intent(this, EditCaregiver.class);
        i.putExtra("starting", "overview");
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    private void goBack() {
        Intent i = new Intent(this, ElderSelectorActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    public void goToAlarmLog(View view) {
        Intent i = new Intent(this, alarm_log.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    public void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                getCountNotif();
                if(countNow != countOld) {
                    countOld = countNow;
                    Log.e("Count", String.valueOf(countNow));
                    if (countNow > 0) {
                        notifCount.setText(String.valueOf(countNow));
                        notif.setVisibility(View.VISIBLE);
                    } else {
                        notif.setVisibility(View.GONE);
                    }
                }
                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }
}