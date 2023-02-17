package com.tugasakhir.elderlycare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static MqttAndroidClient client;
    public String subscriptionTopic;
    String serverUri;
    String clientID;
    MqttHelper mqttHelper;

    static TextView connections;
    TextInputEditText user, passw;
    Button button;

    public static String myUser, myPass;

    final loadingDialog loadingDialog = new loadingDialog(MainActivity.this);

    private final View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.button:
                    loadingDialog.startDialog();

                    myUser = user.getText().toString();
                    myPass = passw.getText().toString();
                    if(!Objects.equals(myUser, "") & !Objects.equals(myPass, "")) {
                        try {
                            Log.d("Token", String.valueOf(client.isConnected()));
                            if(!client.isConnected()){
                                MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                                mqttConnectOptions.setConnectionTimeout(3000);
                                mqttConnectOptions.setAutomaticReconnect(true);
                                mqttConnectOptions.setCleanSession(true);
                                mqttConnectOptions.setUserName(myUser);
                                mqttConnectOptions.setPassword(myPass.toCharArray());
                                IMqttToken token = client.connect(mqttConnectOptions);
                                token.setActionCallback(new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken asyncActionToken) {
                                        //setSubscription();
                                        subscriptionTopic = myUser+"/#";
                                        Log.d("Topic", subscriptionTopic);
                                        startService(new Intent(MainActivity.this, mqttServices.class));
                                        loadingDialog.dismissDialog();
                                        subscribeToTopic();
                                        goToHome();
                                        Toast.makeText(MainActivity.this, "Login Success!", Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                        loadingDialog.dismissDialog();
                                        Toast.makeText(MainActivity.this, "connection failed!!!", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                try {
                                    IMqttToken token = client.disconnect();
                                    token.setActionCallback(new IMqttActionListener() {
                                        @Override
                                        public void onSuccess(IMqttToken asyncActionToken) {
                                            loadingDialog.dismissDialog();
                                            Toast.makeText(MainActivity.this,"Disconnected!!",Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                            loadingDialog.dismissDialog();
                                            Toast.makeText(MainActivity.this,"Could not diconnect!!",Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                                //Toast.makeText(MainActivity.this, "already connected!!!", Toast.LENGTH_LONG).show();
                            }
                        } catch (MqttException e) {
                            throw new RuntimeException(e);
                        }
                        //startMqtt(myUser, myPass);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serverUri = getString(R.string.ipServer);

        user = (TextInputEditText) findViewById(R.id.user_id);
        passw = (TextInputEditText) findViewById(R.id.user_passw);
        connections = (TextView) findViewById(R.id.tv3);
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(myClickListener);

        clientID = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), serverUri, clientID);

    }

    private void startMqtt(String u, String p) {
        mqttHelper = new MqttHelper(getApplicationContext(), u, p);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.d("Debug", "Connected!");
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.d("Debug", "Disconnect!");

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                Log.w("Debug", mqttMessage.toString());
                connections.setText(mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    private void subscribeToTopic() {
        try {
            client.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }


    public void goToHome(){
        Intent i = new Intent(this, MainActivity2.class);
        startActivity(i);
        overridePendingTransition(0, 0);
    }
//    public static void recMessage(String msg) {
//        Log.w("Message", msg);
//        connections.setText(msg.toString());
//    }
}