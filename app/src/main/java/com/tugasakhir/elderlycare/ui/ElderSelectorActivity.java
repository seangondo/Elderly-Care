package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.ui.MainActivity.client;
import static com.tugasakhir.elderlycare.ui.MainActivity.dataElder;
import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.adapter.RecyclerViewAdapter;
import com.tugasakhir.elderlycare.api.RecyclerViewInterface;
import com.tugasakhir.elderlycare.handler.DBHandler;
import com.tugasakhir.elderlycare.service.loadingDialog;
import com.tugasakhir.elderlycare.service.mqttServices;
import com.tugasakhir.elderlycare.service.notificationServices;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ElderSelectorActivity extends AppCompatActivity implements RecyclerViewInterface {

    private ArrayList<Integer> myId = new ArrayList<>();
    private ArrayList<String> myName = new ArrayList<>();
    private ArrayList<String> myAddress = new ArrayList<>();
    private ArrayList<String> myHouse = new ArrayList<>();
    private ArrayList<String> myRobot = new ArrayList<>();
    private ArrayList<String> myWearable = new ArrayList<>();
    private ArrayList<String> myImage = new ArrayList<>();

    public static int elderSelected = 0;
    public static String elderName;

    DBHandler myDb = new DBHandler(this);

    Button addElder, addExisting, logout, editCaregiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_selector);
        addElder = (Button) findViewById(R.id.btnAddElder);
        addElder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAddElder();
            }
        });
        addExisting = (Button) findViewById(R.id.btnAddExistingElder);
        addExisting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAddExistingElder();
            }
        });
        logout = (Button) findViewById(R.id.btnLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        editCaregiver = (Button) findViewById(R.id.editCaregiver);
        editCaregiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(ElderSelectorActivity.this, EditCaregiver.class);
                i.putExtra("starting", "selector");
                startActivity(i);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        detectElder();
    }

    private void detectElder() {
//        JSONArray jsArray = new JSONArray(dataElder);
        JSONArray jsArray = null;
        try {
            jsArray = myDb.getElderDataAll();
            JSONObject arrObj = null;
            for(int i = 0; i < jsArray.length(); i++) {
                arrObj = jsArray.getJSONObject(i);
                Log.e("Hasil", String.valueOf(arrObj));
                myId.add(arrObj.getInt("elder_id"));
                myName.add(arrObj.getString("name"));
                myAddress.add(arrObj.getString("address"));
                myHouse.add(arrObj.getString("house_id"));
                myRobot.add(arrObj.getString("robot_id"));
                myWearable.add(arrObj.getString("watch_id"));
                myImage.add(myServer+"/image/"+arrObj.getString("image"));
            }
            initRecyclerView();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = this.findViewById(R.id.elderRecycler);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, myId, myName, myAddress, myImage, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int pos) {
        elderSelected = myId.get(pos);
        elderName = myName.get(pos);
//        loadingDialog.startDialog();
//        loadingDialog.dismissDialog();
        Log.e("Pressed", elderName);
        goToHome();
//        startMqtt(myId.get(pos));
    }



    private void goToHome() {
        Intent i = new Intent(this, MainActivity2.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }


    private void goToAddElder() {
        Intent i = new Intent(this, AddElder.class);
        i.putExtra("starting", "selector");
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    private void goToAddExistingElder() {
        Intent i = new Intent(this, AddExistingElder.class);
        i.putExtra("starting", "selector");
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
        stopService(new Intent(this, notificationServices.class));
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

        Toast.makeText(ElderSelectorActivity.this, "Logout Success!", Toast.LENGTH_LONG).show();

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }
}