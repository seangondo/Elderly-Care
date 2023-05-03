package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.service.mqttServices.HrData;
import static com.tugasakhir.elderlycare.service.mqttServices.HrTrendRec;
import static com.tugasakhir.elderlycare.service.mqttServices.onBody;
import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;
import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;

import static java.time.LocalDate.from;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.tabs.TabLayout;
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.handler.DBHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Wearable#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Wearable extends Fragment {

    DBHandler myDb;

    TextView name, dob, address, hr, wear, age;
    ImageView img;

    LineChart hrTrend;

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
        name = (TextView) view.findViewById(R.id.infoElderName);
        dob = (TextView) view.findViewById(R.id.infoElderBirthdate);
        address = (TextView) view.findViewById(R.id.infoElderAddress);
        age = (TextView) view.findViewById(R.id.infoElderAge);
        hr = (TextView) view.findViewById(R.id.hrView);
        wear = (TextView) view.findViewById(R.id.wearStat);

        img = (ImageView) view.findViewById(R.id.infoElderImage);

        hrTrend = (LineChart) view.findViewById(R.id.hrTrend);

        initTrend(hrTrend, 200, 0);

        if(HrTrendRec.length() != 0) {
            setChart(hrTrend);
        }

        Log.e("Elder Id", String.valueOf(elderSelected));
        setData();
    }

    private void setData(){
        myDb = new DBHandler(getContext());
        JSONObject obj = new JSONObject();
        try {
            obj = myDb.getElderData(elderSelected);
            Log.e("Hasil", String.valueOf(obj.getString("image")));
            String imgUrl = null;
            imgUrl = myServer+"/image/"+obj.getString("image");
            name.setText(obj.getString("name"));

            Date birthday = new Date(obj.getString("birthdate"));
            SimpleDateFormat simpleDate = new SimpleDateFormat("dd MMMM yyyy");

            // Calculate Birthday
            LocalDateTime birth = LocalDateTime.ofInstant(birthday.toInstant(), ZoneId.systemDefault());
            int getAge = Period.between(LocalDate.of(birth.getYear(), birth.getMonth(), birth.getDayOfMonth()), LocalDate.now()).getYears();

            age.setText("( " + getAge + " Years Old )");
            dob.setText(simpleDate.format(birthday));
            address.setText(obj.getString("address"));
            Glide.with(getContext()).asBitmap().load(imgUrl).apply(new RequestOptions().override(300, 300)).centerCrop().into(img);
        } catch (JSONException e) {
            e.printStackTrace();
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
        DBHandler myDb = new DBHandler(getContext());
        List<String> xAxisValues = new ArrayList<>();
        ArrayList<Entry> values = new ArrayList<>();

        try {
            JSONObject elder = myDb.getElderData(elderSelected);

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
        DBHandler myDb = new DBHandler(getContext());
        try {
            JSONObject elder = myDb.getElderData(elderSelected);

            // SET Heart Rate Value
            for(int i = 0; i < HrData.length(); i++) {
                if(elder.getString("watch_id").equals(HrData.getJSONObject(i).getString("watch_id"))) {
                    hr.setText(HrData.getJSONObject(i).getInt("hr") + " Bpm");
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

    @Override
    public void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                getData();

                initTrend(hrTrend, 200, 0);
                setChart(hrTrend);
                // TODO TAMBAH DISINI
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