package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;
import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import retrofit2.http.Multipart;

public class EditElderActivity extends AppCompatActivity {

    TextInputEditText elderName, birthday, house_id, robot_id, watch_id, elderAddress;
//    AppCompatAutoCompleteTextView birthday;
    TextView elderId;
    ImageButton elderImage;
    Button btnBack, btnSubmit, btnDelete;
    DBHandler myDb = new DBHandler(this);

    JSONObject caregiver = new JSONObject();

    String part_image;
    String birthdayPost;
    final int REQUEST_GALLERY = 9544;

    final com.tugasakhir.elderlycare.service.loadingDialog loadingDialog = new loadingDialog(this);

    private DatePickerDialog datePickerDialog;

    private View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.submitChange:

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
                        submitElderEdit();
                    }
                    break;
                case R.id.backBtn:
                    Intent i = new Intent(EditElderActivity.this, MainActivity2.class);
                    startActivity(i);
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.deleteElder:
                    loadingDialog.startDialog();
                    deleteElder();
                    break;
                case R.id.elderEditImage:
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "open gallery"), REQUEST_GALLERY);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_elder);

        elderName = (TextInputEditText) findViewById(R.id.elderEditName);
        birthday = (TextInputEditText) findViewById(R.id.elderEditBirth);
        elderAddress = (TextInputEditText) findViewById(R.id.elderEditAddress);
        house_id = (TextInputEditText) findViewById(R.id.elderEditHouseID);
        robot_id = (TextInputEditText) findViewById(R.id.elderEditRobotID);
        watch_id = (TextInputEditText) findViewById(R.id.elderEditWatchID);

        elderId = (TextView) findViewById(R.id.elderId);
        elderId.setText("Elder ID : " + String.valueOf(elderSelected));
        elderImage = (ImageButton) findViewById(R.id.elderEditImage);
        elderImage.setOnClickListener(myClickListener);

        btnSubmit = (Button) findViewById(R.id.submitChange);
        btnBack = (Button) findViewById(R.id.backBtn);
        btnDelete = (Button) findViewById(R.id.deleteElder);

        btnSubmit.setOnClickListener(myClickListener);
        btnBack.setOnClickListener(myClickListener);
        btnDelete.setOnClickListener(myClickListener);

        try {
            caregiver = myDb.LoginData().getJSONObject(0);
            Log.e("Caregiver info", String.valueOf(caregiver));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject obj = new JSONObject();
        try {
            obj = myDb.getElderData(elderSelected);
            String imgUrl = myServer+"/image/"+obj.getString("image");
            Date date = new Date(obj.getString("birthdate"));
            SimpleDateFormat simpleDate = new SimpleDateFormat("dd - MM - yyyy");
//            btnDate.setText(simpleDate.format(date));
            birthday.setText(simpleDate.format(date));
            initDatePicker(date);
//            birthday.setText(simpleDate.format(date));
            elderName.setText(obj.getString("name"));
            elderAddress.setText(obj.getString("address"));
            house_id.setText(obj.getString("house_id"));
            robot_id.setText(obj.getString("robot_id"));
            watch_id.setText(obj.getString("watch_id"));
            Glide.with(EditElderActivity.this).asBitmap().load(imgUrl).apply(new RequestOptions().override(300, 300)).centerCrop().into(elderImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initDatePicker(Date date) {
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
        LocalDateTime cal = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        int year = cal.getYear();
        int month = cal.getMonthValue() - 1;
        int day = cal.getDayOfMonth();
//        Calendar cal = Calendar.getInstance();
//        int year = cal.get(Calendar.YEAR);
//        int month = cal.get(Calendar.MONTH);
//        int day = cal.get(Calendar.DAY_OF_MONTH);

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
                        Glide.with(EditElderActivity.this).asBitmap().load(image.getAbsolutePath()).apply(new RequestOptions().override(300, 300)).centerCrop().into(elderImage);
                    }
                }
            }
        }
    }

    private void submitElderEdit() {
        File imageFile;
        MultipartBody.Part body = null;
        if (part_image != null) {
            imageFile = new File(part_image);
            RequestBody reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
            body = MultipartBody.Part.createFormData("elder_image", imageFile.getName(), reqFile);
        }
        RequestBody id = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(elderSelected));
        RequestBody elder_name = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(elderName.getText()));
        RequestBody elder_address = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(elderAddress.getText()));
        RequestBody birthday = RequestBody.create(MediaType.parse("multipart/form-data"), birthdayPost);
        RequestBody houseid = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(house_id.getText()));
        RequestBody robotid = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(robot_id.getText()));
        RequestBody watchid = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(watch_id.getText()));
        Log.e("Elder Name", String.valueOf(elder_name));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(myServer+":8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        if(body != null) {
            Call<ResponseBody> call = retrofitAPI.updateElderImage(id, elder_name, elder_address, birthday, houseid, robotid, watchid, body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Toast.makeText(EditElderActivity.this, "Edit success!", Toast.LENGTH_LONG).show();
                    updateElderTb();
                    loadingDialog.dismissDialog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(EditElderActivity.this, "Edit failed!", Toast.LENGTH_LONG).show();
                    Log.e("Response", String.valueOf(t));
                }
            });
        }
        else {
            Call<ResponseBody> call1 = retrofitAPI.updateElder(id, elder_name, elder_address, birthday, houseid, robotid, watchid);
            Log.e("Elder Name", String.valueOf(elder_address));
            call1.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(EditElderActivity.this, "Edit success!", Toast.LENGTH_LONG).show();
                    updateElderTb();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    Log.e("TES", );
                    loadingDialog.dismissDialog();
                    Toast.makeText(EditElderActivity.this, "Edit failed!", Toast.LENGTH_LONG).show();
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
                            ContentValues cv = new ContentValues();
                            cv.put("name", arrObj.getString("name"));
                            cv.put("birthdate", arrObj.getString("birthdate"));
                            cv.put("address", arrObj.getString("address"));
                            cv.put("house_id", arrObj.getString("house_id"));
                            cv.put("watch_id", arrObj.getString("watch_id"));
                            cv.put("robot_id", arrObj.getString("robot_id"));
                            cv.put("image", arrObj.getString("image"));
                            myDb.updateElder(cv, arrObj.getInt("elder_id"));
                            Log.e("Elder ID", String.valueOf(arrObj.getInt("elder_id")));
                            Log.e("CV", String.valueOf(cv));
                            Log.e("Update", "Berhasil update");
                        }
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

    private void deleteElder() {
        try {
            RequestBody caregiver_id = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(caregiver.getInt("id")));
            RequestBody elder_id = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(elderSelected));
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(myServer+":8000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

            Call<ResponseBody> call = retrofitAPI.deleteElder(caregiver_id, elder_id);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    myDb.deleteElder(elderSelected);
                    Toast.makeText(EditElderActivity.this, "Elder deleted!", Toast.LENGTH_LONG).show();
                    loadingDialog.dismissDialog();
                    Intent j = new Intent(EditElderActivity.this, ElderSelectorActivity.class);
                    startActivity(j);
                    overridePendingTransition(0, 0);
                    finish();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(EditElderActivity.this, "Delete Elder failed!", Toast.LENGTH_LONG).show();
                    Log.e("Response", String.valueOf(t));
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}