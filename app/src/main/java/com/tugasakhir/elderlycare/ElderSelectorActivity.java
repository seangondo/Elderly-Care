package com.tugasakhir.elderlycare;

import static com.tugasakhir.elderlycare.MainActivity.dataElder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ElderSelectorActivity extends AppCompatActivity implements RecyclerViewInterface {

    // INITIALIZE MQTT CONNECTION AND SERVICES
    public static MqttAndroidClient client;
    public static String serverUri;
    String clientID, mqttUser, mqttPass;

    private ArrayList<Integer> myId = new ArrayList<>();
    private ArrayList<String> myName = new ArrayList<>();
    private ArrayList<String> myAddress = new ArrayList<>();
    private ArrayList<String> myHouse = new ArrayList<>();
    private ArrayList<String> myRobot = new ArrayList<>();
    private ArrayList<String> myWearable = new ArrayList<>();
    private ArrayList<String> myImage = new ArrayList<>();

    public static int elderSelected = 0;
    public static String elderName;

    final loadingDialog loadingDialog = new loadingDialog(ElderSelectorActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_selector);
        serverUri = getString(R.string.ipServer);
        clientID = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), serverUri, clientID);
        mqttUser = getString(R.string.mqttUser);
        mqttPass = getString(R.string.mqttPass);
        detectElder();
    }

    private void detectElder() {
        JSONArray jsArray = new JSONArray(dataElder);
        JSONObject arrObj = null;
        try {
            for(int i = 0; i < jsArray.length(); i++) {
                arrObj = jsArray.getJSONObject(i);
                Log.e("Hasil", String.valueOf(arrObj));
                myId.add(arrObj.getInt("elder_id"));
                myName.add(arrObj.getString("name"));
                myAddress.add(arrObj.getString("address"));
                myHouse.add(arrObj.getString("house_id"));
                myRobot.add(arrObj.getString("robot_id"));
                myWearable.add(arrObj.getString("watch_id"));
                myImage.add("https://private-server.uk.to/image/"+arrObj.getString("image"));
            }
            initRecyclerView();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.elderRecycler);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, myId, myName, myAddress, myImage, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int pos) {
        elderSelected = myId.get(pos);
        elderName = myName.get(pos);
        loadingDialog.startDialog();
        startMqtt(myId.get(pos));
    }

    private void startMqtt(int id) {
        try {
            Log.d("Token", String.valueOf(client.isConnected()));
            if(!client.isConnected()){
                MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                mqttConnectOptions.setConnectionTimeout(3000);
                mqttConnectOptions.setAutomaticReconnect(true);
                mqttConnectOptions.setCleanSession(true);
                mqttConnectOptions.setUserName(mqttUser);
                mqttConnectOptions.setPassword(mqttPass.toCharArray());
                IMqttToken token = client.connect(mqttConnectOptions);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        //setSubscription();
                        String subscriptionTopic = id+"/#";
                        Log.d("Topic", subscriptionTopic);
                        startService(new Intent(ElderSelectorActivity.this, mqttServices.class));
                        subscribeToTopic(subscriptionTopic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        loadingDialog.dismissDialog();
                        if(String.valueOf(exception).contains("failed to connect")) {
                            Toast.makeText(ElderSelectorActivity.this, "Server unavailable! Try again in few moment!", Toast.LENGTH_LONG).show();
                            Log.e("Login Failed!", "Server unavailable!");
                        } else if(String.valueOf(exception).contains("Not authorized to connect")) {
                            Toast.makeText(ElderSelectorActivity.this, "Contact admin! Something wrong with this apps!", Toast.LENGTH_LONG).show();
                            Log.e("Login Failed!", "Wrong user/password");
                        }
                    }
                });
            }
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private void subscribeToTopic(String topic) {
        try {
            client.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed : " + topic);
                    loadingDialog.dismissDialog();
                    goToHome();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                    loadingDialog.dismissDialog();
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    private void goToHome() {
        Intent i = new Intent(this, MainActivity2.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }
}