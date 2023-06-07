package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;
import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.adapter.AlarmLogAdapter;
import com.tugasakhir.elderlycare.api.RetrofitAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class alarm_log extends AppCompatActivity {

    Button clear, back;

    ArrayList<String> date;
    ArrayList<String> time;
    ArrayList<String> message;
    ArrayList<String> type;
    ArrayList<String> status;
    TextView tvnoAlarm;
    ListView alarmLog;

    private AlarmLogAdapter alarmLogAdapter;

    View.OnClickListener myClickList = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.backBtn:
                    goBack();
                    break;
                case R.id.btnClearAll:
                    clearAllLog();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_log);

        clear = (Button) findViewById(R.id.btnClearAll);
        back = (Button) findViewById(R.id.backBtn);

        tvnoAlarm = (TextView) findViewById(R.id.noAlrmText);

        clear.setOnClickListener(myClickList);
        back.setOnClickListener(myClickList);

        alarmLog = (ListView) findViewById(R.id.alarmLog);

        BroadcastReceiver buttonDelete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("delete_log")) {
                    int id = intent.getIntExtra("id", 0);
                    String date = intent.getStringExtra("date");
                    String time = intent.getStringExtra("time");
                    String type = intent.getStringExtra("type");
                    String stats = intent.getStringExtra("stats");
                    String msg = intent.getStringExtra("msg");

                    deleteSelected(id, date, time, type, stats, msg);
                }
            }
        };
        IntentFilter filter = new IntentFilter("delete_log");
        registerReceiver(buttonDelete, filter);

        setData();

        alarmLog.setClickable(true);
        alarmLog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.e("Status List", String.valueOf(date));
                if(status.get(i).equals("1")) {
                    changeStatus(i);
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    private void setData() {
        date = new ArrayList<>();
        time= new ArrayList<>();
        message = new ArrayList<>();
        type = new ArrayList<>();
        status = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(myServer+":8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<Object> call = retrofitAPI.getLog(String.valueOf(elderSelected));
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                String res = new Gson().toJson(response.body());
                try {
                    JSONArray arr = new JSONArray(res);
                    if(arr.length() > 0) {
                        alarmLog.setVisibility(View.VISIBLE);
                        tvnoAlarm.setVisibility(View.GONE);
                        for(int i = 0; i < arr.length(); i++) {
                            JSONObject arrObj = arr.getJSONObject(i);
                            date.add(arrObj.getString("date"));
                            time.add(arrObj.getString("time"));
                            message.add(arrObj.getString("message"));
                            type.add(arrObj.getString("type"));
                            status.add(String.valueOf(arrObj.getInt("status")));
                        }
                        alarmLogAdapter = new AlarmLogAdapter(getApplicationContext(), date, time, type, message, status);
                        alarmLog.setAdapter(alarmLogAdapter);
                    } else {
                        alarmLog.setVisibility(View.GONE);
                        tvnoAlarm.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {

            }
        });
    }

    private void changeStatus(int i) {
        Log.e("Update log", String.valueOf(i));
        RequestBody elder_id = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(elderSelected));
        RequestBody mydate = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(date.get(i)));
        RequestBody mytime = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(time.get(i)));
        RequestBody mytype = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(type.get(i)));
        RequestBody mystats = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(status.get(i)));
        RequestBody mymsg = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(message.get(i)));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(myServer+":8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<ResponseBody> call = retrofitAPI.alarmUpdate(elder_id, mydate, mytime, mytype, mystats, mymsg);


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String res = new Gson().toJson(response.body());
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    if (obj.getString("result").equals("berhasil")) {
//                        Toast.makeText(alarm_log.this, "Updated!", Toast.LENGTH_LONG).show();
                        status.set(i, "0");
                        alarmLogAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Failed", String.valueOf(t));
            }
        });
    }

    private void deleteSelected(int id, String mdate, String mtime, String mtype, String stats, String msg) {
        RequestBody elder_id = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(elderSelected));
        RequestBody mydate = RequestBody.create(MediaType.parse("multipart/form-data"), mdate);
        RequestBody mytime = RequestBody.create(MediaType.parse("multipart/form-data"), mtime);
        RequestBody mytype = RequestBody.create(MediaType.parse("multipart/form-data"), mtype);
        RequestBody mystats = RequestBody.create(MediaType.parse("multipart/form-data"), stats);
        RequestBody mymsg = RequestBody.create(MediaType.parse("multipart/form-data"), msg);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(myServer+":8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<ResponseBody> call = retrofitAPI.alarmDelete(elder_id, mydate, mytime, mytype, mystats, mymsg);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String res = new Gson().toJson(response.body());
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    if (obj.getString("result").equals("berhasil")) {
                        Toast.makeText(alarm_log.this, "Alarm Delete!", Toast.LENGTH_LONG).show();
                        date.remove(id);
                        time.remove(id);
                        type.remove(id);
                        status.remove(id);
                        message.remove(id);
                        alarmLogAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Failed", String.valueOf(t));
            }
        });
    }

    private void clearAllLog(){
        RequestBody elder_id = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(elderSelected));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(myServer+":8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<ResponseBody> call = retrofitAPI.alarmClear(elder_id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String res = new Gson().toJson(response.body());
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    if (obj.getString("result").equals("berhasil")) {
//                        Toast.makeText(alarm_log.this, "Updated!", Toast.LENGTH_LONG).show();
                        setData();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Failed", String.valueOf(t));
            }
        });
    }

    private void goBack() {
        Intent i = new Intent(this, MainActivity2.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }
}