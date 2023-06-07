package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;
import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.api.RetrofitAPI;
import com.tugasakhir.elderlycare.handler.DBHandler;
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

public class EditCaregiver extends AppCompatActivity {

    TextInputEditText name, username, email, phoneNum, address;
    Button btnSubmit, btnBack, btnPass;

    DBHandler myDb = new DBHandler(this);
    JSONObject caregiver = new JSONObject();

    String extra;

    final com.tugasakhir.elderlycare.service.loadingDialog loadingDialog = new loadingDialog(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_caregiver);

        Intent i = getIntent();
        extra = i.getStringExtra("starting");

        name = (TextInputEditText) findViewById(R.id.caregiverName);
        username = (TextInputEditText) findViewById(R.id.caregiverUsername);
        email = (TextInputEditText) findViewById(R.id.caregiverEmail);
        phoneNum = (TextInputEditText) findViewById(R.id.caregiverPhoneNumber);
        address = (TextInputEditText) findViewById(R.id.caregiverAddress);

        try {
            caregiver = myDb.LoginData().getJSONObject(0);
            name.setText(caregiver.getString("name"));
            username.setText(caregiver.getString("username"));
            email.setText(caregiver.getString("email"));
            phoneNum.setText(caregiver.getString("phone_number"));
            address.setText(caregiver.getString("address"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnSubmit = (Button) findViewById(R.id.submitEdit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(String.valueOf(name.getText()).equals("")) {
                    name.setError("Name can't be empty!");
                }
                if(String.valueOf(username.getText()).equals("")) {
                    username.setError("Username can't be empty!");
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
                        && !String.valueOf(email.getText()).equals("")
                        && !String.valueOf(phoneNum.getText()).equals("")
                        && !String.valueOf(address.getText()).equals("")) {
                    editCaregiver();
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

        btnPass = (Button) findViewById(R.id.changePassword);
        btnPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditCaregiver.this, EditCaregiverPassword.class);
                startActivity(i);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    private void editCaregiver() {
        try {
            RequestBody caregiverId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(caregiver.getInt("id")));
            RequestBody editName = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(name.getText()));
            RequestBody editUsername = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(username.getText()));
            RequestBody editEmail = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(email.getText()));
            RequestBody editPhone = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(phoneNum.getText()));
            RequestBody editAddress = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(address.getText()));

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(myServer+":8000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

            Call<ResponseBody> call = retrofitAPI.caregiverEdit(caregiverId, editName, editUsername, editEmail, editPhone, editAddress);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONObject obj = new JSONObject(response.body().string());
                        if (obj.getString("result").equals("berhasil")) {
                            Toast.makeText(EditCaregiver.this, "Edit success!", Toast.LENGTH_LONG).show();
                            updateCaregiverData();
                            loadingDialog.dismissDialog();
                        } else if (obj.getString("result").equals("gagal")) {
                            if(obj.getBoolean("username"))
                                username.setError("Username already registered!");
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
                    Toast.makeText(EditCaregiver.this, "Sign Up failed!", Toast.LENGTH_LONG).show();
                    Log.e("Response", String.valueOf(t));
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateCaregiverData() {
        try {
            ContentValues cv = new ContentValues();
            cv.put("name", String.valueOf(name.getText()));
            cv.put("username", String.valueOf(username.getText()));
            cv.put("email", String.valueOf(email.getText()));
            cv.put("phoneNumber", String.valueOf(phoneNum.getText()));
            cv.put("address", String.valueOf(address.getText()));
            myDb.updateCaregiver(cv, caregiver.getInt("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void goBack() {
        if(elderSelected == 0) {
            Intent i = new Intent(EditCaregiver.this, MainActivity2.class);
            startActivity(i);
            overridePendingTransition(0, 0);
            finish();
        } else {
            Intent i = new Intent(EditCaregiver.this, ElderSelectorActivity.class);
            startActivity(i);
            overridePendingTransition(0, 0);
            finish();
        }
    }
}