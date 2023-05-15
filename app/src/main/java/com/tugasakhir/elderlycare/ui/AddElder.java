package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;
import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.api.RetrofitAPI;
import com.tugasakhir.elderlycare.handler.DBHandler;
import com.tugasakhir.elderlycare.service.loadingDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddElder extends AppCompatActivity {
    TextInputEditText elderName, birthday, house_id, robot_id, watch_id, elderAddress;
    //    AppCompatAutoCompleteTextView birthday;
    ImageButton elderImage;
    Button btnBack, btnSubmit;
    DBHandler myDb = new DBHandler(this);

    String extra;
    String part_image;
    String birthdayPost;

    JSONObject caregiver = new JSONObject();

    final int REQUEST_GALLERY = 9544;

    final com.tugasakhir.elderlycare.service.loadingDialog loadingDialog = new loadingDialog(this);

    private DatePickerDialog datePickerDialog;

    private View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.submitAdd:
                    try {
                        if(String.valueOf(elderName.getText()).equals("")) {
                            elderName.setError("Name can't be empty!");
                        }
                        if(String.valueOf(birthday.getText()).equals("")) {
                            birthday.setError("Username can't be empty!");
                        }
                        if(String.valueOf(house_id.getText()).equals("")) {
                            house_id.setError("House ID can't be empty!");
                        }
                        if(String.valueOf(robot_id.getText()).equals("")) {
                            robot_id.setError("Robot ID can't be empty!");
                        }
                        if(String.valueOf(watch_id.getText()).equals("")) {
                            watch_id.setError("Watch ID can't be empty!");
                        }
                        if(String.valueOf(elderAddress.getText()).equals("")) {
                            elderAddress.setError("Address can't be empty!");
                        }
                        if(!String.valueOf(elderName.getText()).equals("")
                                && !String.valueOf(birthday.getText()).equals("")
                                && !String.valueOf(house_id.getText()).equals("")
                                && !String.valueOf(robot_id.getText()).equals("")
                                && !String.valueOf(watch_id.getText()).equals("")
                                && !String.valueOf(elderAddress.getText()).equals("")) {
                            loadingDialog.startDialog();
                            submitElderAdd();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.backBtn:
                    Intent i = null;
                    if(extra.equals("overview")) {
                        i = new Intent(AddElder.this, MainActivity2.class);
                    } else if(extra.equals("selector")) {

                        i = new Intent(AddElder.this, ElderSelectorActivity.class);
                    }
                    startActivity(i);
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.elderAddImage:
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "open gallery"), REQUEST_GALLERY);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_elder);

        Intent i = getIntent();
        extra = i.getStringExtra("starting");

        try {
            caregiver = myDb.LoginData().getJSONObject(0);
            Log.e("Caregiver info", String.valueOf(caregiver));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        elderName = (TextInputEditText) findViewById(R.id.elderAddName);
        birthday = (TextInputEditText) findViewById(R.id.elderAddBirth);
        elderAddress = (TextInputEditText) findViewById(R.id.elderAddAddress);
//        btnDate = (Button) findViewById(R.id.elderEditBirth);
        house_id = (TextInputEditText) findViewById(R.id.elderAddHouseID);
        robot_id = (TextInputEditText) findViewById(R.id.elderAddRobotID);
        watch_id = (TextInputEditText) findViewById(R.id.elderAddWatchID);

        elderImage = (ImageButton) findViewById(R.id.elderAddImage);
        elderImage.setOnClickListener(myClickListener);

        btnSubmit = (Button) findViewById(R.id.submitAdd);
        btnBack = (Button) findViewById(R.id.backBtn);
        btnSubmit.setOnClickListener(myClickListener);
        btnBack.setOnClickListener(myClickListener);

        Glide.with(AddElder.this).asBitmap().load(R.drawable.empty).apply(new RequestOptions().override(300, 300)).centerCrop().into(elderImage);

        initDatePicker();
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String tgl = "";
                if(dayOfMonth > 10) {
                    tgl = dayOfMonth + " - " + month + " - " + year;
                } else {
                    tgl = "0" + dayOfMonth + " - " + month + " - " + year;
                }
                birthdayPost = year + "-" + month + "-" + dayOfMonth;
//                btnDate.setText(tgl);
                birthday.setText(tgl);
            }
        };
//        LocalDateTime cal = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
//        int year = cal.getYear();
//        int month = cal.getMonthValue() - 1;
//        int day = cal.getDayOfMonth();
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        birthdayPost = year + "-" + month + "-" + day;

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    public void openDatePicker(View view) {
        datePickerDialog.show();
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_GALLERY) {
                Uri dataImage = data.getData();
                String[] imageData = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(dataImage, imageData, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int indexImage = cursor.getColumnIndex(imageData[0]);
                    part_image = cursor.getString(indexImage);
                    if(part_image != null) {
                        File image = new File(part_image);
                        Glide.with(AddElder.this).asBitmap().load(image.getAbsolutePath()).apply(new RequestOptions().override(300, 300)).centerCrop().into(elderImage);
                    }
                }
            }
        }
    }

    private void submitElderAdd() throws JSONException {
        File imageFile;
        MultipartBody.Part body = null;
        if (part_image != null) {
            imageFile = new File(part_image);
            RequestBody reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
            body = MultipartBody.Part.createFormData("elder_image", imageFile.getName(), reqFile);
        }
        RequestBody id = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(caregiver.getInt("id")));
        RequestBody elder_name = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(elderName.getText()));
        RequestBody elder_address = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(elderAddress.getText()));
        RequestBody birthday = RequestBody.create(MediaType.parse("multipart/form-data"), birthdayPost);
        RequestBody houseid = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(house_id.getText()));
        RequestBody robotid = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(robot_id.getText()));
        RequestBody watchid = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(watch_id.getText()));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(myServer+":8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        if(body != null) {
            Call<ResponseBody> call = retrofitAPI.addElderImage(id, elder_name, elder_address, birthday, houseid, robotid, watchid, body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Toast.makeText(AddElder.this, "Add Elder success!", Toast.LENGTH_LONG).show();
                    updateElderTb();
                    loadingDialog.dismissDialog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(AddElder.this, "Add Elder failed!", Toast.LENGTH_LONG).show();
                    Log.e("Response", String.valueOf(t));
                }
            });
        }
        else {
            Call<ResponseBody> call1 = retrofitAPI.addElder(id, elder_name, elder_address, birthday, houseid, robotid, watchid);
            Log.e("Elder Name", String.valueOf(elder_address));
            call1.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(AddElder.this, "Add Elder success!", Toast.LENGTH_LONG).show();
                    updateElderTb();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(AddElder.this, "Add Elder failed!", Toast.LENGTH_LONG).show();
                    Log.e("Response", String.valueOf(t));
                }
            });
        }
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