package com.tugasakhir.elderlycare.ui;

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

public class EditCaregiverPassword extends AppCompatActivity {

    TextInputEditText username, passOld, passNew;
    Button btnSubmit, btnBack;

    DBHandler myDb = new DBHandler(this);
    JSONObject caregiver = new JSONObject();

    final com.tugasakhir.elderlycare.service.loadingDialog loadingDialog = new loadingDialog(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_caregiver_password);

        username = (TextInputEditText) findViewById(R.id.caregiverUsername);
        passOld = (TextInputEditText) findViewById(R.id.caregiverPasswordOld);
        passNew = (TextInputEditText) findViewById(R.id.caregiverPasswordNew);
        try {
            caregiver = myDb.LoginData().getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnSubmit = (Button) findViewById(R.id.changePass);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(String.valueOf(username.getText()).equals("")) {
                    username.setError("Username can't be empty!");
                }
                if(String.valueOf(passOld.getText()).equals("")) {
                    passOld.setError("Email can't be empty!");
                }
                if(String.valueOf(passNew.getText()).equals("")) {
                    passNew.setError("Phone Number can't be empty!");
                }
                if(!String.valueOf(username.getText()).equals("")
                        && !String.valueOf(passOld.getText()).equals("")
                        && !String.valueOf(passNew.getText()).equals("")) {
                    editPassCaregiver();
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
        Intent i = new Intent(EditCaregiverPassword.this, EditCaregiver.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    private void editPassCaregiver() {
        try {
            RequestBody caregiverId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(caregiver.getInt("id")));
            RequestBody editUsername = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(username.getText()));
            RequestBody editPassOld = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(passOld.getText()));
            RequestBody editPassnew = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(passNew.getText()));

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(myServer+":8000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

            Call<ResponseBody> call = retrofitAPI.caregiverEditPassword(caregiverId, editUsername, editPassOld, editPassnew);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONObject obj = new JSONObject(response.body().string());
                        if (obj.getString("result").equals("berhasil")) {
                            Toast.makeText(EditCaregiverPassword.this, "Change Password success!", Toast.LENGTH_LONG).show();
                            updateCaregiverData();
                            loadingDialog.dismissDialog();
                        } else if (obj.getString("result").equals("gagal")) {
                            Toast.makeText(EditCaregiverPassword.this, "Change Password Failed!", Toast.LENGTH_LONG).show();
                            loadingDialog.dismissDialog();
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(EditCaregiverPassword.this, "Sign Up failed!", Toast.LENGTH_LONG).show();
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
            cv.put("username", String.valueOf(username.getText()));
            cv.put("password", String.valueOf(passNew.getText()));
            myDb.updateCaregiver(cv, caregiver.getInt("id"));
            username.setText("");
            passOld.setText("");
            passNew.setText("");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}