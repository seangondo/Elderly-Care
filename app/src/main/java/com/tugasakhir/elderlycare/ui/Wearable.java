package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.service.mqttServices.CalsData;
import static com.tugasakhir.elderlycare.service.mqttServices.CalsTrend;
import static com.tugasakhir.elderlycare.service.mqttServices.HrData;
import static com.tugasakhir.elderlycare.service.mqttServices.HrTrendRec;
import static com.tugasakhir.elderlycare.service.mqttServices.StepsData;
import static com.tugasakhir.elderlycare.service.mqttServices.StepsTrend;
import static com.tugasakhir.elderlycare.service.mqttServices.onBody;
import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;
import static com.tugasakhir.elderlycare.ui.MainActivity.client;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.handler.DBHandler;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

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
        steps = (TextView) view.findViewById(R.id.countSteps);
        cals = (TextView) view.findViewById(R.id.countCalories);
        hr = (TextView) view.findViewById(R.id.hrView);
        wear = (TextView) view.findViewById(R.id.wearStat);
        msgText = (EditText) view.findViewById(R.id.msgSendElder);
        msgSend = (Button) view.findViewById(R.id.btnSendMsg);

        msgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    client.publish(elderSelected+"/apps/wearable/messages", ("\"" + msgText.getText().toString() + "\"").getBytes(),0, false);
                    msgText.setText("");
                    Toast.makeText(getContext(), "Message send to elder!", Toast.LENGTH_LONG).show();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

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
        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
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

    // TODO AMBIL dari HRTrend terus di parse masing2 untuk Step dan Cals
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

}