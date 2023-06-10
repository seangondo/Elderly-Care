package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.api.RetrofitAPI;
import com.tugasakhir.elderlycare.service.loadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddCaregiver extends AppCompatActivity {

    TextInputEditText name, username, password, email, phoneNum, address;
    Button btnSubmit, btnBack;

    final com.tugasakhir.elderlycare.service.loadingDialog loadingDialog = new loadingDialog(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_caregiver);

        name = (TextInputEditText) findViewById(R.id.caregiverName);
        username = (TextInputEditText) findViewById(R.id.caregiverUsername);
        password = (TextInputEditText) findViewById(R.id.caregiverPassword);
        email = (TextInputEditText) findViewById(R.id.caregiverEmail);
        phoneNum = (TextInputEditText) findViewById(R.id.caregiverPhoneNumber);
        address = (TextInputEditText) findViewById(R.id.caregiverAddress);

        btnSubmit = (Button) findViewById(R.id.btnSignUp);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(String.valueOf(name.getText()).equals("")) {
                    name.setError("Name can't be empty!");
                }
                if(String.valueOf(username.getText()).equals("")) {
                    username.setError("Username can't be empty!");
                }
                if(String.valueOf(password.getText()).equals("")) {
                    password.setError("Password can't be empty!");
                }
                if(String.valueOf(email.getText()).equals("")) {
                    email.setError("Email can't be empty!");
                }
                if(String.valueOf(phoneNum.getText()).equals("")) {
                    phoneNum.setError("Phone Number can't be empty!");
                }
                if(String.valueOf(address.getText()).equals("")) {
                    address.setError("Address can't be empty!");
                }
                if(!String.valueOf(name.getText()).equals("")
                        && !String.valueOf(name.getText()).equals("")
                        && !String.valueOf(username.getText()).equals("")
                        && !String.valueOf(password.getText()).equals("")
                        && !String.valueOf(email.getText()).equals("")
                        && !String.valueOf(phoneNum.getText()).equals("")
                        && !String.valueOf(address.getText()).equals("")) {
                    signUpCaregiver();
                    loadingDialog.startDialog();
                }
            }
        });
        btnBack = (Button) findViewById(R.id.backBtn);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    private void goBack() {
        Intent i = new Intent(AddCaregiver.this, MainActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    private void signUpCaregiver() {
        RequestBody signName = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(name.getText()));
        RequestBody signUsername = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(username.getText()));
        RequestBody signPassword = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(password.getText()));
        RequestBody signEmail = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(email.getText()));
        RequestBody signPhone = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(phoneNum.getText()));
        RequestBody signAddress = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(address.getText()));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(myServer+":8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        Call<ResponseBody> call = retrofitAPI.caregiverSignup(signName, signUsername, signPassword, signEmail, signPhone, signAddress);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    if (obj.getString("result").equals("berhasil")) {
                        Toast.makeText(AddCaregiver.this, "Sign Up success!", Toast.LENGTH_LONG).show();
                        clearData();
                        loadingDialog.dismissDialog();
                    } else if (obj.getString("result").equals("gagal")) {
                        if(obj.getBoolean("username"))
                            username.setError("Username already registered!");
                            loadingDialog.dismissDialog();
                        if(obj.getBoolean("email"))
                            email.setError("Email already registered!");
                            loadingDialog.dismissDialog();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismissDialog();
                Toast.makeText(AddCaregiver.this, "Sign Up failed!", Toast.LENGTH_LONG).show();
                Log.e("Response", String.valueOf(t));
            }
        });
    }

    private void clearData() {
        name.setText("");
        username.setText("");
        password.setText("");
        email.setText("");
        phoneNum.setText("");
        address.setText("");
    }
}