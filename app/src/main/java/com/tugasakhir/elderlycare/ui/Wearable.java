package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.service.mqttServices.CalsData;
import static com.tugasakhir.elderlycare.service.mqttServices.CalsTrend;
import static com.tugasakhir.elderlycare.service.mqttServices.HrData;
import static com.tugasakhir.elderlycare.service.mqttServices.HrTrendRec;
import static com.tugasakhir.elderlycare.service.mqttServices.StepsData;
import static com.tugasakhir.elderlycare.service.mqttServices.StepsTrend;
import static com.tugasakhir.elderlycare.service.mqttServices.onBody;
import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;
import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;
import static com.tugasakhir.elderlycare.ui.MainActivity2.title;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.adapter.CustomAlarmLogAdapter;
import com.tugasakhir.elderlycare.api.RetrofitAPI;
import com.tugasakhir.elderlycare.handler.DBHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Wearable#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Wearable extends Fragment {

    DBHandler myDb;
    JSONObject elderData;

    TextView steps, cals, hr, wear;
    EditText msgText;
    Button msgSend;
    ImageView img;

    LineChart hrTrend;
    BarChart stepsTrend, calsTrend;

    FrameLayout simpleFrameLayout;
    TabLayout tabLayout;

    //Updater
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 1*100;


    ArrayList<String> date;
    ArrayList<String> time;
    ArrayList<String> message;
    ArrayList<String> type;
    ArrayList<String> status;
    Button clear;
    TextView noTextAlarm;
    ListView alarmLog;

    private CustomAlarmLogAdapter customAlarmLogAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Wearable() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Wearable.
     */
    // TODO: Rename and change types and number of parameters
    public static Wearable newInstance(String param1, String param2) {
        Wearable fragment = new Wearable();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wearable, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Intent intent = new Intent();
        intent.setAction("update_title");
        intent.putExtra("title", "Wearable");
        getContext().sendBroadcast(intent);

        steps = (TextView) view.findViewById(R.id.countSteps);
        cals = (TextView) view.findViewById(R.id.countCalories);
        hr = (TextView) view.findViewById(R.id.hrView);
        wear = (TextView) view.findViewById(R.id.wearStat);

        myDb = new DBHandler(getContext());

        hrTrend = (LineChart) view.findViewById(R.id.hrTrend);
        stepsTrend = (BarChart) view.findViewById(R.id.stepsTrend);
        calsTrend = (BarChart) view.findViewById(R.id.trendCals);

        initTrend(hrTrend, 200, 0);

        initBar(stepsTrend);
        initBar(calsTrend);
        try {
            elderData = myDb.getElderData(elderSelected);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(HrTrendRec.length() != 0) {
            for(int i = 0; i < HrTrendRec.length(); i ++) {
                try {
//                    JSONObject elder = myDb.getElderData(elderSelected);
                    JSONObject elder = elderData;
                    if(HrTrendRec.getJSONObject(i).getString("watch_id").equals(elder.getString("watch_id"))) {
                        setChart(hrTrend);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setChart(hrTrend);
        }
        if(StepsTrend.length() != 0) {
            for(int i = 0; i < StepsTrend.length(); i ++) {
                try {
//                    JSONObject elder = myDb.getElderData(elderSelected);
                    JSONObject elder = elderData;
                    if(StepsTrend.getJSONObject(i).getString("watch_id").equals(elder.getString("watch_id"))) {
                        setBarData(stepsTrend, "steps", StepsTrend);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if(CalsTrend.length() != 0) {
            for(int i = 0; i < CalsTrend.length(); i ++) {
                try {
//                    JSONObject elder = myDb.getElderData(elderSelected);
                    JSONObject elder = elderData;
                    if(CalsTrend.getJSONObject(i).getString("watch_id").equals(elder.getString("watch_id"))) {
                        setBarData(calsTrend, "calories", CalsTrend);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
//        Log.e("Elder Id", String.valueOf(elderSelected));
        Log.e("Data", String.valueOf(StepsTrend));
        Log.e("Data", String.valueOf(CalsTrend));
        Log.e("Data", String.valueOf(HrTrendRec));
        getData();

        // ALARM
        clear = (Button) view.findViewById(R.id.clearAll);
        noTextAlarm = (TextView) view.findViewById(R.id.noAlarmWearable);
        alarmLog = (ListView) view.findViewById(R.id.alarmWearable);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllLog();
            }
        });

        BroadcastReceiver buttonDelete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("delete_log")) {
                    if(title.equals("Wearable")) {
                        int id = intent.getIntExtra("id", 0);
                        String date = intent.getStringExtra("date");
                        String time = intent.getStringExtra("time");
                        String type = intent.getStringExtra("type");
                        String stats = intent.getStringExtra("stats");
                        String msg = intent.getStringExtra("msg");

                        deleteSelected(id, date, time, type, stats, msg);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter("delete_log");
        requireActivity().registerReceiver(buttonDelete, filter);

        setDataAlarm();

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

    private void initTrend(LineChart chartName,int maxVal, int minVal){

        chartName.setBackgroundColor(Color.WHITE);
        chartName.setTouchEnabled(true);
        chartName.setDragEnabled(true);
        chartName.setScaleEnabled(true);
        chartName.setPinchZoom(true);

        XAxis xAxis = chartName.getXAxis();
        XAxis.XAxisPosition position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setPosition(position);
        xAxis.enableGridDashedLine(10f, 5f, 0f);


        YAxis yAxis = chartName.getAxisLeft();
        chartName.getAxisRight().setEnabled(false);
        yAxis.enableGridDashedLine(10f, 10f, 0f);
        yAxis.setAxisMaximum(maxVal);
        yAxis.setAxisMinimum(minVal);

        chartName.getLegend().setEnabled(false);
        chartName.getDescription().setEnabled(false);
    }

    private void setChart(LineChart chartName) {
        List<String> xAxisValues = new ArrayList<>();
        ArrayList<Entry> values = new ArrayList<>();

        try {
//            JSONObject elder = myDb.getElderData(elderSelected);
            JSONObject elder = elderData;

            for(int i = 0; i < HrTrendRec.length(); i++) {
                JSONObject obj = HrTrendRec.getJSONObject(i);
                if(elder.getString("watch_id").equals(obj.getString("watch_id"))) {
                    values.add(new Entry(i, obj.getInt("value")));
                    xAxisValues.add(obj.getString("time"));
                }
            }

            XAxis xAxis = chartName.getXAxis();
            xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xAxisValues));
            LineDataSet set1;

            set1 = new LineDataSet(values, "");

            set1.setDrawIcons(false);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(2f);
            set1.setDrawCircleHole(false);
            set1.setFormLineWidth(1f);
            set1.setFormSize(15.f);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return chartName.getAxisLeft().getAxisMinimum();
                }
            });

            set1.setFillColor(Color.BLACK);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData data = new LineData(dataSets);

            chartName.setData(data);
            chartName.invalidate();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void getData() {
        try {
//            JSONObject elder = myDb.getElderData(elderSelected);
            JSONObject elder = elderData;

            // SET Heart Rate Value
            for(int i = 0; i < HrData.length(); i++) {
                if(elder.getString("watch_id").equals(HrData.getJSONObject(i).getString("watch_id"))) {
                    hr.setText(HrData.getJSONObject(i).getInt("hr") + " Bpm");
                }
            }

            // SET Steps Value
            for(int i = 0; i < StepsData.length(); i++) {
                if(elder.getString("watch_id").equals(StepsData.getJSONObject(i).getString("watch_id"))) {
                    steps.setText(StepsData.getJSONObject(i).getInt("steps") + " Steps");
                }
            }

            // SET Cals Value
            for(int i = 0; i < CalsData.length(); i++) {
                if(elder.getString("watch_id").equals(CalsData.getJSONObject(i).getString("watch_id"))) {
                    cals.setText(CalsData.getJSONObject(i).getInt("calories") + " Cals");
                }
            }

            // SET On Body Detect Value
            for(int i = 0; i < onBody.length(); i++) {
                if(elder.getString("watch_id").equals(onBody.getJSONObject(i).getString("watch_id"))) {
                    if(onBody.getJSONObject(i).getString("onbody").equals("false")) {
                        wear.setText("Jam tidak digunakan!");
                        wear.setTextColor(Color.RED);
                    } else if (onBody.getJSONObject(i).getString("onbody").equals("true")) {
                        wear.setText("Jam sedang digunakan!");
                        wear.setTextColor(Color.GREEN);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initBar(BarChart chart) {

        chart.setMaxVisibleValueCount(60);
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        chart.getAxisLeft().setDrawGridLines(false);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
    }


    private void setBarData(BarChart chart, String type, JSONArray dataType) {

        ArrayList<BarEntry> values = new ArrayList<>();
        List<String> xAxisValues = new ArrayList<>();

        try {
//            JSONObject elder = myDb.getElderData(elderSelected);
            JSONObject elder = elderData;
            for(int i = 0; i < dataType.length(); i ++) {
                JSONObject obj = dataType.getJSONObject(i);
                if(elder.getString("watch_id").equals(obj.getString("watch_id"))
                        && obj.getString("type").equals(type)) {
                    values.add(new BarEntry(i, obj.getInt("value")));
                    xAxisValues.add(obj.getString("date"));
                }
            }

            XAxis xAxis = chart.getXAxis();
            xAxis.setLabelCount(xAxisValues.size());
            xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xAxisValues));
            BarDataSet set1;

            set1 = new BarDataSet(values, "");

            set1.setDrawIcons(false);
            set1.setColor(Color.BLACK);
            set1.setFormLineWidth(1f);
            set1.setFormSize(15.f);
            set1.setValueTextSize(9f);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            chart.setData(data);

            chart.invalidate();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                getData();

                initTrend(hrTrend, 200, 0);
                initBar(stepsTrend);
                initBar(calsTrend);

                if(HrTrendRec.length() != 0) {
                    for(int i = 0; i < HrTrendRec.length(); i ++) {
                        try {
//                            JSONObject elder = myDb.getElderData(elderSelected);
                            JSONObject elder = elderData;
                            if(HrTrendRec.getJSONObject(i).getString("watch_id").equals(elder.getString("watch_id"))) {
                                setChart(hrTrend);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    setChart(hrTrend);
                }
                if(StepsTrend.length() != 0) {
                    for(int i = 0; i < StepsTrend.length(); i ++) {
                        try {
//                            JSONObject elder = myDb.getElderData(elderSelected);
                            JSONObject elder = elderData;
                            if(StepsTrend.getJSONObject(i).getString("watch_id").equals(elder.getString("watch_id"))) {
                                setBarData(stepsTrend, "steps", StepsTrend);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(CalsTrend.length() != 0) {
                    for(int i = 0; i < CalsTrend.length(); i ++) {
                        try {
//                            JSONObject elder = myDb.getElderData(elderSelected);
                            JSONObject elder = elderData;
                            if(CalsTrend.getJSONObject(i).getString("watch_id").equals(elder.getString("watch_id"))) {
                                setBarData(calsTrend, "calories", CalsTrend);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }

                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();

    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    private void setDataAlarm() {
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
                        noTextAlarm.setVisibility(View.GONE);
                        for(int i = 0; i < arr.length(); i++) {
                            JSONObject arrObj = arr.getJSONObject(i);
                            if(arrObj.getString("type").equals("SOS") || arrObj.getString("type").equals("Heart Rate")) {
                                date.add(arrObj.getString("date"));
                                time.add(arrObj.getString("time"));
                                message.add(arrObj.getString("message"));
                                type.add(arrObj.getString("type"));
                                status.add(String.valueOf(arrObj.getInt("status")));
                            }
                        }
                        if(date.size() <= 0) {
                            alarmLog.setVisibility(View.GONE);
                            noTextAlarm.setVisibility(View.VISIBLE);
                        }
                        if (date.size()>0  || time.size()>0 || type.size()>0 || message.size()>0 || status.size()>0) {
                            customAlarmLogAdapter = new CustomAlarmLogAdapter(getContext(), date, time, type, message, status);
                            alarmLog.setAdapter(customAlarmLogAdapter);
                        }
                    } else {
                        alarmLog.setVisibility(View.GONE);
                        noTextAlarm.setVisibility(View.VISIBLE);
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
                        status.set(i, "0");
//                        CustomAlarmLogAdapter customAlarmLogAdapter = new CustomAlarmLogAdapter(getContext(), date, time, type, message, status);
                        customAlarmLogAdapter.notifyDataSetChanged();
//                        setDataAlarm();
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
                        Toast.makeText(MainActivity2.getAppContext(), "Alarm Delete!", Toast.LENGTH_LONG).show();
                        date.remove(id);
                        time.remove(id);
                        type.remove(id);
                        status.remove(id);
                        message.remove(id);
                        customAlarmLogAdapter.notifyDataSetChanged();
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
        for(int i = 0; i < date.size(); i++) {
            RequestBody elder_id = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(elderSelected));
            RequestBody mydate = RequestBody.create(MediaType.parse("multipart/form-data"), date.get(i));
            RequestBody mytime = RequestBody.create(MediaType.parse("multipart/form-data"), time.get(i));
            RequestBody mytype = RequestBody.create(MediaType.parse("multipart/form-data"), type.get(i));
            RequestBody mystats = RequestBody.create(MediaType.parse("multipart/form-data"), status.get(i));
            RequestBody mymsg = RequestBody.create(MediaType.parse("multipart/form-data"), message.get(i));

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
                            setDataAlarm();
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
    }
}