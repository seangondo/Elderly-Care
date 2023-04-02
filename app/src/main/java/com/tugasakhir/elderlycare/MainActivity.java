package com.tugasakhir.elderlycare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    public String subscriptionTopic;
    public static ArrayList<Object> dataElder = new ArrayList<>();

    public String serverUri;

//    static TextView connections;
    TextInputEditText user, passw;
    TextView login;
    Button button;
    CheckBox cbLogin;

    public String myUser, myPass;

    final loadingDialog loadingDialog = new loadingDialog(MainActivity.this);

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
        serverUri = getString(R.string.ipServer);

        user = (TextInputEditText) findViewById(R.id.user_id);
        passw = (TextInputEditText) findViewById(R.id.user_passw);
        button = (Button) findViewById(R.id.button);
        login = (TextView) findViewById(R.id.tvLogin);
        cbLogin = (CheckBox) findViewById(R.id.cbAutoLogin);

        button.setOnClickListener(myClickListener);

    }


    //RETROFIT FUNCTION
    private void postData(String username, String password) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.30:8000/")
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

            // TODO Tambahkan auto login dan simpan data ke sqlite
            // res.getUser_id(), res.getUser_name(), res.getUsername(), res.getEmail(), res.getPhone_number(), res.getAddress(),
            Toast.makeText(MainActivity.this, "Login Success!", Toast.LENGTH_LONG).show();
            dataElder = res.getElder_list();
            goToHome();

        } else {
            loadingDialog.dismissDialog();
            Toast.makeText(MainActivity.this, "Username/Password wrong!", Toast.LENGTH_LONG).show();
            login.setText("Username / Password wrong!");
            login.setTextColor(Color.RED);
            Log.e("Login", "Login Gagal!");
        }
    }

    public void goToHome(){
        Intent i = new Intent(this, ElderSelectorActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }
}