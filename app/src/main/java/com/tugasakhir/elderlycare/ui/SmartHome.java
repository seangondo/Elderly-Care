package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.service.mqttServices.buttonSmartHome;
import static com.tugasakhir.elderlycare.service.mqttServices.sensorSmartHome;
import static com.tugasakhir.elderlycare.service.mqttServices.trendRec;
import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;
import static com.tugasakhir.elderlycare.ui.MainActivity.client;
import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;
import static com.tugasakhir.elderlycare.ui.MainActivity2.binding;
import static com.tugasakhir.elderlycare.ui.MainActivity2.swAuto;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.navigationrail.NavigationRailView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Use the {@link SmartHome#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SmartHome extends Fragment implements View.OnClickListener{
    PieChart roomTemp, roomLight, kitchenLight, kitchenGas;
    LineChart trendTemp, trendGas;

    CardView buttonFan_Living, buttonLight_Living, buttonLight_Kitchen;
    TextView kitchen_light_label, living_light_label, living_fan_label;
    ImageView living_light_image, living_fan_image, kitchen_light_image;
    TextView COstat;
    NavigationRailView navigationRailView;

    TextView text1;

    ArrayList<String> date;
    ArrayList<String> time;
    ArrayList<String> message;
    ArrayList<String> type;
    ArrayList<String> status;
    Button clear;
    TextView noTextAlarm;
    ListView alarmLog;

    JSONObject elderData;

    long lastClick = 0;

//    public static String living_temp, living_light, kitchen_light, kitchen_gas;

    //Updater
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 500;

    private CustomAlarmLogAdapter customAlarmLogAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SmartHome() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SmartHome.
     */
    // TODO: Rename and change types and number of parameters
    public static SmartHome newInstance(String param1, String param2) {
        SmartHome fragment = new SmartHome();
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
        View view = inflater.inflate(R.layout.fragment_smart_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        Intent intent = new Intent();
        intent.setAction("update_title");
        intent.putExtra("title", "Smart\nHome");
        getContext().sendBroadcast(intent);

        navigationRailView = (NavigationRailView) view.findViewById(R.id.navigationBar);

        COstat = (TextView) view.findViewById(R.id.COdetect);

        buttonFan_Living = (CardView) view.findViewById(R.id.buttonFanLiving);
        buttonLight_Living = (CardView) view.findViewById(R.id.buttonLightsLiving);
        buttonLight_Kitchen = (CardView) view.findViewById(R.id.buttonLightsKitchen);

        living_fan_label = (TextView) view.findViewById(R.id.livingFanLabel);
        living_light_label = (TextView) view.findViewById(R.id.livingLightsLabel);
        kitchen_light_label = (TextView) view.findViewById(R.id.kitchenLightsLabel);

        living_fan_image = (ImageView) view.findViewById(R.id.livingFanImg);
        living_light_image = (ImageView) view.findViewById(R.id.livingLightsImg);
        kitchen_light_image= (ImageView) view.findViewById(R.id.kitchenLightsImg);

        roomLight = (PieChart) view.findViewById(R.id.lightLiving);
        roomTemp = (PieChart) view.findViewById(R.id.tempLiving);
        kitchenLight = (PieChart) view.findViewById(R.id.lightKitchen);
        kitchenGas = (PieChart) view.findViewById(R.id.gasKitchen);

        trendTemp = (LineChart) view.findViewById(R.id.tempTrend);
        trendGas = (LineChart) view.findViewById(R.id.gasTrend);

        initTrend(trendTemp, 100, 0);
        initTrend(trendGas, 1024, 0);

        buttonFan_Living.setOnClickListener(this);
        buttonLight_Living.setOnClickListener(this);
        buttonLight_Kitchen.setOnClickListener(this);


        initPieChart(roomLight);
        initPieChart(roomTemp);
        initPieChart(kitchenLight);
        initPieChart(kitchenGas);

        try {
            DBHandler myDb =  new DBHandler(getContext());
            elderData = myDb.getElderData(elderSelected);
            myDb.close();
            for(int i = 0; i < sensorSmartHome.length(); i++) {
                JSONObject obj = sensorSmartHome.getJSONObject(i);
                if(obj.getString("house_id").equals(elderData.getString("house_id"))) {
                    showPieChart(roomLight, parseInt(obj.getString("living_light")), 1024, "");
                    showPieChart(roomTemp, parseInt(obj.getString("living_temp")), 100, "\u2103");
                    showPieChart(kitchenLight, parseInt(obj.getString("kitchen_light")), 1024, "");
                    showPieChart(kitchenGas, parseInt(obj.getString("kitchen_gas")), 1024, "");

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        if(living_light!=null & living_temp!=null & kitchen_light!=null & kitchen_gas!=null) {
//            showPieChart(roomLight, Integer.parseInt(living_light), 1000, "");
//            showPieChart(roomTemp, Integer.parseInt(living_temp), 100, "\u2103");
//            showPieChart(kitchenLight, Integer.parseInt(kitchen_light), 1000, "");
//            showPieChart(kitchenGas, Integer.parseInt(kitchen_gas), 1000, "");
//        }

        if(trendRec.length() != 0) {
            setData("livingroom" ,trendTemp);
            setData("kitchen" ,trendGas);
        }
        getData();

        // ALARM
        clear = (Button) view.findViewById(R.id.clearAll);
        noTextAlarm = (TextView) view.findViewById(R.id.noAlarmSmartHome);
        alarmLog = (ListView) view.findViewById(R.id.alarmSmarthome);

        clear.setOnClickListener(this);
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

    private void setData(String loc, LineChart chartName) {
        List<String> xAxisValues = new ArrayList<>();

        ArrayList<Entry> values = new ArrayList<>();
        if(loc.equals("kitchen")) {
            // CARA 1
//            xAxisValues = new ArrayList<>(mqttServices.kitchen_time);
//            for (int i = 0; i < kitchen_no.size(); i++) {
//                values.add(new Entry(i, mqttServices.kitchen_val.get(i)));
//            }

            //CARA 2

            try {
                String house_id = elderData.getString("house_id");
                int j = 0;
                for(int i = 0; i < trendRec.length(); i++) {
                    if(trendRec.getJSONObject(i).getString("house_id").equals(house_id) &&
                            (trendRec.getJSONObject(i).getString("type").equals("gas"))) {
                        values.add(new Entry(j, parseInt(trendRec.getJSONObject(i).getString("value"))));
                        xAxisValues.add(trendRec.getJSONObject(i).getString("time"));
                        j++;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        if(loc.equals("livingroom")) {
//            xAxisValues = new ArrayList<>(mqttServices.living_time);
//            for (int i = 0; i < mqttServices.living_no.size(); i++) {
//                values.add(new Entry(i, mqttServices.living_val.get(i)));
//            }

            //CARA 2

            try {
                String house_id = elderData.getString("house_id");
                int j = 0;
                for(int i = 0; i < trendRec.length(); i++) {
                    if(trendRec.getJSONObject(i).getString("house_id").equals(house_id) &&
                            (trendRec.getJSONObject(i).getString("type").equals("temp"))) {
                        values.add(new Entry(j, parseInt(trendRec.getJSONObject(i).getString("value"))));
                        xAxisValues.add(trendRec.getJSONObject(i).getString("time"));
                        j++;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
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
    }

    private void getData(){
        try {
//            elderData = myDb.getElderData(elderSelected);
            for(int i = 0; i < sensorSmartHome.length(); i++) {
                JSONObject obj = sensorSmartHome.getJSONObject(i);
                if(obj.getString("house_id").equals(elderData.getString("house_id"))) {
                    if(parseInt(obj.getString("kitchen_gas")) >= 700) {
                        COstat.setTextColor(Color.RED);
                        COstat.setText("Gas rate too high!");
                    } else {
                        COstat.setTextColor(Color.GREEN);
                        COstat.setText("Normal");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < buttonSmartHome.length(); i++) {
            try {
                JSONObject data = buttonSmartHome.getJSONObject(i);
                if(data.getString("house_id").equals(elderData.getString("house_id"))) {
                    if(data.getString("room").equals("livingroom")) {
                        if(data.getString("type").equals("light")) {
                            if(data.getString("value").equals("true")) {
                                living_light_label.setText("ON");
                                living_light_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lightbulb_on));
                            }else if (data.getString("value").equals("false")){
                                living_light_label.setText("OFF");
                                living_light_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lightbulb_off));
                            }
                        }

                        if(data.getString("type").equals("fan")) {
                            if(data.getString("value").equals("off")){
                                living_fan_label.setText("OFF");
                                living_fan_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_off_icon));
                            } else if (data.getString("value").equals("slow")){
                                living_fan_label.setText("SLOW");
                                living_fan_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_slow_icon));
                            } else if (data.getString("value").equals("fast")){
                                living_fan_label.setText("FAST");
                                living_fan_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_speed_icon));
                            }
                        }
                    }
                    if(data.getString("room").equals("kitchen")) {
                        if(data.getString("type").equals("light")) {
                            if(data.getString("value").equals("true")){
                                kitchen_light_label.setText("ON");
                                kitchen_light_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lightbulb_on));
                            } else if (data.getString("value").equals("false")){
                                kitchen_light_label.setText("OFF");
                                kitchen_light_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lightbulb_off));
                            }
                        }
                    }

                    if(data.getString("room").equals("automatic_mode")) {
                        if(data.getString("value").equals("true")){
                            swAuto.setChecked(true);
                        } else if (data.getString("value").equals("false")){
                            swAuto.setChecked(false);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                getData();
                try {
                    for(int i = 0; i < sensorSmartHome.length(); i++) {
                        JSONObject obj = null;
                            obj = sensorSmartHome.getJSONObject(i);
                        if(obj.getString("house_id").equals(elderData.getString("house_id"))) {
                            showPieChart(roomLight, parseInt(obj.getString("living_light")), 1024, "");
                            showPieChart(roomTemp, parseInt(obj.getString("living_temp")), 100, "\u2103");
                            showPieChart(kitchenLight, parseInt(obj.getString("kitchen_light")), 1024, "");
                            showPieChart(kitchenGas, parseInt(obj.getString("kitchen_gas")), 1024, "");

                        }
                    }

                    if(trendRec.length() != 0) {
                        setData("livingroom" ,trendTemp);
                        setData("kitchen" ,trendGas);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

    @SuppressLint("ResourceType")
    private void initPieChart(PieChart name){
        //using percentage as values instead of amount
        name.setUsePercentValues(true);

        //remove the description label on the lower left corner, default true if not set
        name.getDescription().setEnabled(false);

        //enabling the user to rotate the chart, default true
        name.setRotationEnabled(false);
        //adding friction when rotating the pie chart
        name.setDragDecelerationFrictionCoef(0.9f);
        //setting the first entry start from right hand side, default starting from top
        name.setRotationAngle(90);

        //highlight the entry when it is tapped, default true if not set
        name.setHighlightPerTapEnabled(false);
        //adding animation so the entries pop up from 0 degree
        //name.animateY(1400, Easing.EaseInOutQuad);
        //setting the color of the hole in the middle, default white

        name.setHoleColor(Color.parseColor(getString(com.google.android.material.R.color.cardview_dark_background)));

    }

    private void showPieChart(PieChart name, int curVal, int maxVal, String suffix){

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String label = "type";

        //initializing data
        Map<String, Integer> typeAmountMap = new HashMap<>();
        typeAmountMap.put(" ",curVal);
        typeAmountMap.put("",maxVal-curVal);

        //initializing colors for the entries
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#309967"));
        colors.add(Color.parseColor("#304567"));

        //input data and fit data into pie chart entry
        for(String type: typeAmountMap.keySet()){
            pieEntries.add(new PieEntry(typeAmountMap.get(type).floatValue(), type));
        }

        //collecting the entries with label name
        PieDataSet pieDataSet = new PieDataSet(pieEntries,"");
        //setting text size of the value
        pieDataSet.setValueTextSize(12f);
        //providing color list for coloring different entries
        pieDataSet.setColors(colors);
        //grouping the data set from entry to chart
        PieData pieData = new PieData(pieDataSet);
        //showing the value of the entries, default true if not set
        pieData.setDrawValues(false);
        name.getLegend().setEnabled(false);
        name.getDescription().setEnabled(false);

        name.setCenterText(String.valueOf(curVal)+suffix);
        name.setCenterTextColor(Color.WHITE);
        name.setCenterTextSize(14f);
        name.setCenterTextTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        name.setData(pieData);
        name.invalidate();
    }



    @Override
    public void onClick(View view) {
        if (!swAuto.isChecked()) {
            long currentClick = System.currentTimeMillis();
            if (currentClick - lastClick > 500) {
                lastClick = currentClick;
                switch (view.getId()) {
                    case R.id.buttonFanLiving:
                        if ((living_fan_label.getText()).equals("OFF")) {
                            client.publish(elderSelected + "/apps/control_button/livingroom/fan", "{\"value\": \"slow\", \"var\": 1}".getBytes(), 0, true);
                        } else if ((living_fan_label.getText()).equals("SLOW")) {
                            client.publish(elderSelected + "/apps/control_button/livingroom/fan", "{\"value\": \"fast\", \"var\": 1}".getBytes(), 0, true);
                        } else if ((living_fan_label.getText()).equals("FAST")) {
                            client.publish(elderSelected + "/apps/control_button/livingroom/fan", "{\"value\": \"off\", \"var\": 1}".getBytes(), 0, true);
                        }
                        break;
                    case R.id.buttonLightsLiving:
                        if ((living_light_label.getText()).equals("OFF")) {
                            client.publish(elderSelected + "/apps/control_button/livingroom/light", "{\"value\": \"true\", \"var\": 1}".getBytes(), 0, true);
                        } else {
                            client.publish(elderSelected + "/apps/control_button/livingroom/light", "{\"value\": \"false\", \"var\": 1}".getBytes(), 0, true);
                        }
                        break;
                    case R.id.buttonLightsKitchen:
                        if ((kitchen_light_label.getText()).equals("OFF")) {
                            client.publish(elderSelected + "/apps/control_button/kitchen/light", "{\"value\": \"true\", \"var\": 1}".getBytes(), 0, true);
                        } else {
                            client.publish(elderSelected + "/apps/control_button/kitchen/light", "{\"value\": \"false\", \"var\": 1}".getBytes(), 0, true);
                        }
                        break;
                    case R.id.clearAll:
                        clearAllLog();
                        break;
                }
            }
        }
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
                            if(arrObj.getString("type").equals("Smart Home")) {
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
                        Toast.makeText(getContext(), "Alarm Delete!", Toast.LENGTH_LONG).show();
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
//                            Toast.makeText(getContext(), "Alarm cleared!", Toast.LENGTH_LONG).show();
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