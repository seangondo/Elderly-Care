package com.tugasakhir.elderlycare;

import static com.tugasakhir.elderlycare.MainActivity.client;
import static com.tugasakhir.elderlycare.MainActivity2.binding;
import static com.tugasakhir.elderlycare.mqttServices.lampKitchen;
import static com.tugasakhir.elderlycare.mqttServices.lampLiving;
import static com.tugasakhir.elderlycare.mqttServices.msg;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.loader.content.AsyncTaskLoader;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarMenu;
import com.google.android.material.navigationrail.NavigationRailView;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SmartHome#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SmartHome extends Fragment implements View.OnClickListener{
    PieChart roomTemp, roomLight, kitchenLight, kitchenGas;
//    Button buttonFan_Living, buttonLight_Living, buttonLight_Kitchen;
    CardView buttonFan_Living, buttonLight_Living, buttonLight_Kitchen;
    TextView kitchen_light_label, living_light_label, living_fan_label;
    ImageView living_light_image, living_fan_image, kitchen_light_image;
    TextView COstat;
    NavigationRailView navigationRailView;

    public static String living_temp, living_light, kitchen_light, kitchen_gas;

    //Updater
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 1*500;

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

        buttonFan_Living.setOnClickListener(this);
        buttonLight_Living.setOnClickListener(this);
        buttonLight_Kitchen.setOnClickListener(this);

        getData();

        initPieChart(roomLight);
        initPieChart(roomTemp);
        initPieChart(kitchenLight);
        initPieChart(kitchenGas);

        if(living_light!=null & living_temp!=null & kitchen_light!=null & kitchen_gas!=null) {
            showPieChart(roomLight, Integer.parseInt(living_light), 1000, "");
            showPieChart(roomTemp, Integer.parseInt(living_temp), 100, "\u2103");
            showPieChart(kitchenLight, Integer.parseInt(kitchen_light), 1000, "");
            showPieChart(kitchenGas, Integer.parseInt(kitchen_gas), 1000, "");
        }
    }

    void getData(){
        JSONArray myRec = null;
        try {
            if(mqttServices.getTopic.equals(MainActivity.myUser + "/apps/data")) {
                myRec = new JSONArray(mqttServices.msg);
                for (int i = 0; i < myRec.length(); i++) {
                    JSONObject arrObj = myRec.getJSONObject(i);
                    living_temp = arrObj.getString("living_temp");
                    living_light = arrObj.getString("living_light");
                    kitchen_light = arrObj.getString("kitchen_light");
                    kitchen_gas = arrObj.getString("kitchen_gas");

                    if(Integer.parseInt(kitchen_gas) >= 700) {
                        COstat.setTextColor(Color.RED);
                        COstat.setText("Gas rate too high!");
                    } else {
                        COstat.setTextColor(Color.GREEN);
                        COstat.setText("Normal");
                    }
                }
            }

            if(lampLiving){
                living_light_label.setText("ON");
                living_light_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lightbulb_on));
            } else {
                living_light_label.setText("OFF");
                living_light_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lightbulb_off));
            }
            if(lampKitchen){
                kitchen_light_label.setText("ON");
                kitchen_light_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lightbulb_on));
            } else {
                kitchen_light_label.setText("OFF");
                kitchen_light_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lightbulb_off));
            }

//            if(mqttServices.getTopic.equals(MainActivity.myUser + "/livingroom/light/control")) {
//                JSONObject arrObj = new JSONObject(msg);
//                if(!arrObj.getBoolean("lights")) {
//                    living_light_label.setText("OFF");
//                    living_light_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lightbulb_off));
//                } else {
//                    living_light_label.setText("ON");
//                    living_light_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lightbulb_on));
//                }
//            }
//            if(mqttServices.getTopic.equals(MainActivity.myUser + "/kitchen/light/control")) {
//                JSONObject arrObj = new JSONObject(msg);
//                if(!arrObj.getBoolean("lights")) {
//                    kitchen_light_label.setText("OFF");
//                    kitchen_light_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lightbulb_off));
//                } else {
//                    kitchen_light_label.setText("ON");
//                    kitchen_light_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lightbulb_on));
//                }
//            }
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
                //binding.navigationBar.setSelectedItemId(R.id.smartHome);
                if(living_light!=null & living_temp!=null & kitchen_light!=null & kitchen_gas!=null) {
                    showPieChart(roomLight, Integer.parseInt(living_light), 1000, "");
                    showPieChart(roomTemp, Integer.parseInt(living_temp), 100, "\u2103");
                    showPieChart(kitchenLight, Integer.parseInt(kitchen_light), 1000, "");
                    showPieChart(kitchenGas, Integer.parseInt(kitchen_gas), 1000, "");
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
        name.setRotationEnabled(true);
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
        switch (view.getId()){
            case R.id.buttonFanLiving:
                binding.navigationBar.setSelectedItemId(R.id.smartHome);
                break;
            case R.id.buttonLightsLiving:
                if(((String) living_light_label.getText()).equals("OFF")){
                    try {
                        client.publish(MainActivity.myUser+"/livingroom/light/control", "{\"lights\": true }".getBytes(),0, true);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        client.publish(MainActivity.myUser+"/livingroom/light/control", "{\"lights\": false }".getBytes(),0, true);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.buttonLightsKitchen:
                if(((String) kitchen_light_label.getText()).equals("OFF")){
                    try {
                        client.publish(MainActivity.myUser+"/kitchen/light/control", "{\"lights\": true }".getBytes(),0, true);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        client.publish(MainActivity.myUser+"/kitchen/light/control", "{\"lights\": false }".getBytes(),0, true);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}