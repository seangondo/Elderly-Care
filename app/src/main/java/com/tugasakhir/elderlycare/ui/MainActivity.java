package com.tugasakhir.elderlycare.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.tugasakhir.elderlycare.handler.DBHandler;
import com.tugasakhir.elderlycare.model.ButtonResponse;
import com.tugasakhir.elderlycare.model.LoginResponse;
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.api.RetrofitAPI;
import com.tugasakhir.elderlycare.service.loadingDialog;
import com.tugasakhir.elderlycare.service.mqttServices;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    // INITIALIZE MQTT CONNECTION AND SERVICES
    public static MqttAndroidClient client;
    public String serverUri;

    String clientID, mqttUser, mqttPass;

    //DB Local
    DBHandler myDb = new DBHandler(this);
    Cursor cursorLog;

    public static ArrayList<Object> dataElder = new ArrayList<>();
    private ArrayList<Integer> elderId = new ArrayList<>();

    public static String myServer;

//    static TextView connections;
    TextInputEditText user, passw;
    TextView login;
    Button button;
    CheckBox cbLogin;

    public static String myUser, myPass;

    final com.tugasakhir.elderlycare.service.loadingDialog loadingDialog = new loadingDialog(MainActivity.this);

    private final View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.button:
                    myUser = Objects.requireNonNull(user.getText()).toString();
                    myPass = Objects.requireNonNull(passw.getText()).toString();
                    if(!Objects.equals(myUser, "") & !Objects.equals(myPass, "")) {
                        loadingDialog.startDialog();
                        login.setText("");
                        postData(myUser, myPass);

                    } else {
                        if(Objects.equals(myUser, "")) {
                            user.setError("Username can't be empty!");
                        }
                        if(Objects.equals(myPass, "")) {
                            passw.setError("Password can't be empty!");
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myServer = getString(R.string.ipWeb);

        // MQTT INIT
        serverUri = getString(R.string.ipServer);
        clientID = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), serverUri, clientID);
        mqttUser = getString(R.string.mqttUser);
        mqttPass = getString(R.string.mqttPass);

        user = (TextInputEditText) findViewById(R.id.user_id);
        passw = (TextInputEditText) findViewById(R.id.user_passw);
        button = (Button) findViewById(R.id.button);
        login = (TextView) findViewById(R.id.tvLogin);
        cbLogin = (CheckBox) findViewById(R.id.cbAutoLogin);

        button.setOnClickListener(myClickListener);

        cursorLog = myDb.getLoginInfo();
        Log.e("Jumlah Cursor", String.valueOf(cursorLog.getCount()));

        if(cursorLog.getCount() != 0) {
            try {
                checkLogin();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    //CHECK AUTO LOGIN
    private void checkLogin() throws JSONException {
//        String uName = cursorLog.getString(2);
//        String uPass = cursorLog.getString(3);
//        String autoLog  = cursorLog.getString(7);
//        ArrayList<Object> getData = myDb.LoginData();
        JSONArray array = myDb.LoginData();
        JSONObject obj;

        try {
            for(int i = 0; i < array.length(); i ++) {
                obj = array.getJSONObject(i);
                if(obj.getInt("autoLog") == 1) {
                    user.setText(obj.getString("username"));
                    passw.setText(obj.getString("password"));
                    cbLogin.setChecked(true);
                    loadingDialog.startDialog();
                    login.setText("");
                    postData(obj.getString("username"), obj.getString("password"));
                } else {
                    myDb.deleteLogin("caregiver_info", obj.getString("username"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //RETROFIT FUNCTION
    private void postData(String username, String password) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(myServer+":8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        JSONObject loginObject = new JSONObject();
        try {
            loginObject.put("username", username);
            loginObject.put("password", password);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),
                    (loginObject.toString()));
            Call<LoginResponse> call = retrofitAPI.createPost(body);
            call.enqueue(new Callback<LoginResponse>() {

                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    LoginResponse responseFromAPI = response.body();
                    checkLogin(responseFromAPI);
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    loadingDialog.dismissDialog();
                    login.setText("Server may be unavailable! Try a few moment");
                    login.setTextColor(Color.RED);
                    Log.e("Login Failed!", "Server unavailable!");
                    Log.e("Error", "Error found is : " + t.getMessage());
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkLogin(LoginResponse res) {
        if(res.getResult()) {
            loadingDialog.dismissDialog();

            JSONArray array = new JSONArray();
            JSONObject obj = new JSONObject();
            int autoLog = 0;
            int val = (cbLogin.isChecked()) ? 1 : 0;
            try {
                array = myDb.LoginData();
                obj = array.getJSONObject(0);
                autoLog = obj.getInt("autoLog");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(cursorLog.getCount() == 0) {
                myDb.insertLogin(res, myPass, cbLogin.isChecked());
            } else if (val != autoLog) {
                myDb.deleteLogin("caregiver_info", res.getUsername());
            }

            // res.getUser_id(), res.getUser_name(), res.getUsername(), res.getEmail(), res.getPhone_number(), res.getAddress(),
            Toast.makeText(MainActivity.this, "Login Success!", Toast.LENGTH_LONG).show();
            dataElder = res.getElder_list();

            myDb.deleteElderAll();
            myDb.deleteAllSensor();
            myDb.deleteAllButton();
            myDb.insertElder(dataElder);

            ArrayList<Integer> getInt = new ArrayList<>();
            ArrayList<String> getHouseId = new ArrayList<>();
            JSONArray jsArray = new JSONArray(dataElder);
            JSONObject arrObj = null;
            try {
                for(int i = 0; i < jsArray.length(); i++) {
                    arrObj = jsArray.getJSONObject(i);
                    getInt.add(arrObj.getInt("elder_id"));
                    getHouseId.add(arrObj.getString("house_id"));
                }
                // SORT AND REMOVE DUPLICATES HOUSE ID
                Collections.sort(getHouseId);
                Log.e("Sort Value", String.valueOf(getHouseId));
                List<String> houseId = getHouseId.stream().distinct().collect(Collectors.toList());
                Log.e("Remove duplicates", String.valueOf(houseId));
                addSensorButtonDB(houseId);

                //START MQTT
                startMqtt(getInt);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            loadingDialog.dismissDialog();
            Toast.makeText(MainActivity.this, "Username/Password wrong!", Toast.LENGTH_LONG).show();
            login.setText("Username / Password wrong!");
            login.setTextColor(Color.RED);
            Log.e("Login", "Login Gagal!");
        }
    }

    private void goToHome(){
        Intent i = new Intent(this, ElderSelectorActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    private ArrayList<Integer> subsElder(ArrayList<Object> data) {
        ArrayList<Integer> getInt = new ArrayList<>();
        JSONArray jsArray = new JSONArray(data);
        JSONObject arrObj = null;
        try {
            for(int i = 0; i < jsArray.length(); i++) {
                arrObj = jsArray.getJSONObject(i);
                Log.e("Hasil", String.valueOf(arrObj));
                getInt.add(arrObj.getInt("elder_id"));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return getInt;
    }

    private void startMqtt(ArrayList<Integer> getData) {
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
//                        ArrayList<Integer> getData = subsElder(data);
                        for (int i = 0; i < getData.size(); i++ ) {
                            String subscriptionTopic = String.valueOf(getData.get(i))+"/#";
                            Log.d("Topic", subscriptionTopic);
                            subscribeToTopic(subscriptionTopic);
                        }
                        startService(new Intent(MainActivity.this, mqttServices.class));
                        loadingDialog.dismissDialog();
                        goToHome();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        loadingDialog.dismissDialog();
                        if(String.valueOf(exception).contains("failed to connect")) {
                            Toast.makeText(MainActivity.this, "Server unavailable! Try again in few moment!", Toast.LENGTH_LONG).show();
                            Log.e("Login Failed!", "Server unavailable!");
                        } else if(String.valueOf(exception).contains("Not authorized to connect")) {
                            Toast.makeText(MainActivity.this, "Contact admin! Something wrong with this apps!", Toast.LENGTH_LONG).show();
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

    private void addSensorButtonDB(List<String> id) {
        // TODO Retrofit get
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(myServer+":8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        for(int i = 0; i < id.size(); i++) {
            // GET Sensor
            Call<Object> callSensor = retrofitAPI.getSensor(id.get(i));
            callSensor.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    String res = new Gson().toJson(response.body());
                    try {
                        JSONArray arr = new JSONArray(res);
                        myDb.insertSensor(arr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Log.e("Error", String.valueOf(t));
                }
            });

            // GET Button
            Call<Object> callButton = retrofitAPI.getBtn(id.get(i));
            callButton.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    String res = new Gson().toJson(response.body());
                    try {
                        JSONArray arr = new JSONArray(res);
                        myDb.insertButton(arr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Log.e("Error", String.valueOf(t));
                }
            });

        }
    }
}