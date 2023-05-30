package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.api.RetrofitAPI;
import com.tugasakhir.elderlycare.handler.DBHandler;
import com.tugasakhir.elderlycare.service.loadingDialog;

import org.json.JSONArray;
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

public class AddExistingElder extends AppCompatActivity {

    Button addElder, backBtn;
    TextInputEditText elderName, elderId;

    JSONObject caregiver = new JSONObject();
    DBHandler myDb = new DBHandler(this);

    final com.tugasakhir.elderlycare.service.loadingDialog loadingDialog = new loadingDialog(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_existing_elder);

        try {
            caregiver = myDb.LoginData().getJSONObject(0);
            Log.e("Caregiver info", String.valueOf(caregiver));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        elderName = (TextInputEditText) findViewById(R.id.elderName);
        elderId = (TextInputEditText) findViewById(R.id.elderId);

        addElder = (Button) findViewById(R.id.btnAdd);
        backBtn = (Button) findViewById(R.id.backBtn);
        addElder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(String.valueOf(elderName.getText()).equals("")) {
                    elderName.setError("Name can't be empty!");
                }
                if(String.valueOf(elderId.getText()).equals("")) {
                    elderId.setError("ID can't be empty!");
                }
                if(!String.valueOf(elderName.getText()).equals("")
                        && !String.valueOf(elderId.getText()).equals("")) {
                    try {
                        addExistingElder();
                        loadingDialog.startDialog();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
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
        Intent i = new Intent(this, ElderSelectorActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    private void addExistingElder() throws JSONException {
        RequestBody caregiverId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(caregiver.getString("id")));
        RequestBody addElderName = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(elderName.getText()));
        RequestBody addElderId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(elderId.getText()));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(myServer+":8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        Call<ResponseBody> call = retrofitAPI.existingElder(caregiverId, addElderName, addElderId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    if (obj.getString("result").equals("berhasil")) {
                        Toast.makeText(AddExistingElder.this, "Add Elder success!", Toast.LENGTH_LONG).show();
                        updateElderTb();
                        loadingDialog.dismissDialog();
                    }
                    else if (obj.getString("result").equals("gagal")) {
                        Toast.makeText(AddExistingElder.this, "Add Elder failed!", Toast.LENGTH_LONG).show();
                        loadingDialog.dismissDialog();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismissDialog();
                Toast.makeText(AddExistingElder.this, "Add Elder failed!", Toast.LENGTH_LONG).show();
                Log.e("Response", String.valueOf(t));
            }
        });
    }
    private void updateElderTb() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(myServer+":8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        try {
            String usernameLogin = myDb.LoginData().getJSONObject(0).getString("username");
            Call<Object> call = retrofitAPI.getElder(usernameLogin);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    String res = new Gson().toJson(response.body());
                    try {
                        JSONArray arr = new JSONArray(res);
                        for(int i = 0; i < arr.length(); i++) {
                            JSONObject arrObj = arr.getJSONObject(i);
                            if(myDb.getElderCount(arrObj.getInt("elder_id")) == 0) {
                                myDb.insertSingleElder(arrObj);
                                Log.e("Update", String.valueOf(arrObj));
                            }
                        }
                        Log.e("Data", String.valueOf(myDb.getElderDataAll()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}