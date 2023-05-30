package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.service.mqttServices.CalsData;
import static com.tugasakhir.elderlycare.service.mqttServices.CalsTrend;
import static com.tugasakhir.elderlycare.service.mqttServices.HrData;
import static com.tugasakhir.elderlycare.service.mqttServices.HrTrendRec;
import static com.tugasakhir.elderlycare.service.mqttServices.StepsData;
import static com.tugasakhir.elderlycare.service.mqttServices.StepsTrend;
//import static com.tugasakhir.elderlycare.service.mqttServices.kitchen_gas;
//import static com.tugasakhir.elderlycare.service.mqttServices.kitchen_light;
//import static com.tugasakhir.elderlycare.service.mqttServices.living_light;
//import static com.tugasakhir.elderlycare.service.mqttServices.living_temp;
import static com.tugasakhir.elderlycare.service.mqttServices.onBody;
import static com.tugasakhir.elderlycare.service.mqttServices.poseDetection;
import static com.tugasakhir.elderlycare.service.mqttServices.sensorSmartHome;
import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;

import static java.lang.Integer.parseInt;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.handler.DBHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Overview#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Overview extends Fragment {

    DBHandler myDb;
    JSONObject elderData;

    TextView wearStatus, hrData, stepsData, calsData;
    TextView poseLiving1, poseLiving2, poseDining1;
    TextView lightLiving, tempLiving, lightKitchen, gasKitchen;
    TextView nameElder, dobElder, addressElder;

    Button btnEditElder;

    //Updater
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 500;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Overview() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Overview.
     */
    // TODO: Rename and change types and number of parameters
    public static Overview newInstance(String param1, String param2) {
        Overview fragment = new Overview();
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
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myDb = new DBHandler(getContext());

        try {
            elderData = myDb.getElderData(elderSelected);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        wearStatus = (TextView) view.findViewById(R.id.wearOverview);
        hrData = (TextView) view.findViewById(R.id.hrOverview);
        stepsData = (TextView) view.findViewById(R.id.stepsOverview);
        calsData = (TextView) view.findViewById(R.id.calsOverview);

        poseLiving1 = (TextView) view.findViewById(R.id.pose1Overview);
        poseLiving2 = (TextView) view.findViewById(R.id.pose2Overview);
        poseDining1 = (TextView) view.findViewById(R.id.pose3Overview);

        lightLiving = (TextView) view.findViewById(R.id.lightLivingOverview);
        tempLiving = (TextView) view.findViewById(R.id.tempLivingOverview);
        lightKitchen = (TextView) view.findViewById(R.id.lightKitchenOverview);
        gasKitchen = (TextView) view.findViewById(R.id.gasKitchenOverview);


        nameElder = (TextView) view.findViewById(R.id.nameElderOverview);
        dobElder = (TextView) view.findViewById(R.id.dobElderOverview);
        addressElder = (TextView) view.findViewById(R.id.addressOverview);

        btnEditElder = (Button) view.findViewById(R.id.btnEditElderOverview);

        btnEditElder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), EditElderActivity.class);
                startActivity(i);
            }
        });

        getData();
    }

    private void getData() {
        try {
            JSONObject elder = elderData;

            // ----------------- < BAGIAN WEARABLE > ----------------- //
            // SET Heart Rate Value
            for(int i = 0; i < HrData.length(); i++) {
                if(elder.getString("watch_id").equals(HrData.getJSONObject(i).getString("watch_id"))) {
                    hrData.setText(HrData.getJSONObject(i).getInt("hr") + " Bpm");
                }
            }

            // SET Steps Value
            for(int i = 0; i < StepsData.length(); i++) {
                if(elder.getString("watch_id").equals(StepsData.getJSONObject(i).getString("watch_id"))) {
                    stepsData.setText(StepsData.getJSONObject(i).getInt("steps") + " Steps");
                }
            }

            // SET Cals Value
            for(int i = 0; i < CalsData.length(); i++) {
                if(elder.getString("watch_id").equals(CalsData.getJSONObject(i).getString("watch_id"))) {
                    calsData.setText(CalsData.getJSONObject(i).getInt("calories") + " Cals");
                }
            }

            // SET On Body Detect Value
            for(int i = 0; i < onBody.length(); i++) {
                if(elder.getString("watch_id").equals(onBody.getJSONObject(i).getString("watch_id"))) {
                    if(onBody.getJSONObject(i).getString("onbody").equals("false")) {
                        wearStatus.setText("Jam tidak digunakan!");
                        wearStatus.setTextColor(Color.RED);
                    } else if (onBody.getJSONObject(i).getString("onbody").equals("true")) {
                        wearStatus.setText("Jam sedang digunakan!");
                        wearStatus.setTextColor(Color.GREEN);
                    }
                }
            }

            // ----------------- < BAGIAN SMART HOME > ----------------- //

            try {
                for(int i = 0; i < sensorSmartHome.length(); i++) {
                    JSONObject obj = sensorSmartHome.getJSONObject(i);
                    if(obj.getString("house_id").equals(elderData.getString("house_id"))) {
                        // LIVING ROOM LIGHT
                        if(Integer.parseInt(obj.getString("living_light")) >= 700) {
                            lightLiving.setTextColor(Color.RED);
                            lightLiving.setText("Ruangan gelap!");
                        } else {
                            tempLiving.setTextColor(Color.BLACK);
                            lightLiving.setText("Ruangan terang!");
                        }

                        // LIVING ROOM TEMP
                        if(Integer.parseInt(obj.getString("living_temp")) >= 40) {
                            tempLiving.setTextColor(Color.RED);
                            tempLiving.setText((obj.getString("living_temp")) + " \u2103");

                        } else {
                            tempLiving.setTextColor(Color.BLACK);
                            tempLiving.setText((obj.getString("living_temp")) + " \u2103");
                        }

                        // KITCHEN LIGHT
                        if(Integer.parseInt(obj.getString("kitchen_light")) >= 700) {
                            lightKitchen.setTextColor(Color.RED);
                            lightKitchen.setText("Ruangan gelap!");
                        } else {
                            tempLiving.setTextColor(Color.BLACK);
                            lightKitchen.setText("Ruangan terang!");
                        }

                        // KITCHEN GAS
                        if(Integer.parseInt(obj.getString("kitchen_gas")) >= 700) {
                            gasKitchen.setTextColor(Color.RED);
                            gasKitchen.setText("Gas rate too high!");
                        } else {
                            tempLiving.setTextColor(Color.BLACK);
                            gasKitchen.setText("Normal");
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            CARA 1
//            if(living_light!=null & living_temp!=null & kitchen_light!=null & kitchen_gas!=null) {
//
//                // LIVING ROOM LIGHT
//                if(Integer.parseInt(living_light) >= 700) {
//                    lightLiving.setTextColor(Color.RED);
//                    lightLiving.setText("Ruangan gelap!");
//                } else {
//                    lightLiving.setTextColor(Color.GREEN);
//                    lightLiving.setText("Ruangan terang!");
//                }
//
//                // LIVING ROOM TEMP
//                if(Integer.parseInt(living_temp) >= 40) {
//                    tempLiving.setTextColor(Color.RED);
//                    tempLiving.setText((living_temp) + " \u2103");
//
//                } else {
//                    tempLiving.setTextColor(Color.GREEN);
//                    tempLiving.setText((living_temp) + " \u2103");
//                }
//
//                // KITCHEN LIGHT
//                if(Integer.parseInt(kitchen_light) >= 700) {
//                    lightKitchen.setTextColor(Color.RED);
//                    lightKitchen.setText("Ruangan gelap!");
//                } else {
//                    lightKitchen.setTextColor(Color.GREEN);
//                    lightKitchen.setText("Ruangan terang!");
//                }
//
//                // KITCHEN GAS
//                if(Integer.parseInt(kitchen_gas) >= 700) {
//                    gasKitchen.setTextColor(Color.RED);
//                    gasKitchen.setText("Gas rate too high!");
//                } else {
//                    gasKitchen.setTextColor(Color.GREEN);
//                    gasKitchen.setText("Normal");
//                }
//            }

            // ----------------- < BAGIAN ELDER DATA > ----------------- //
            Date date = new Date(elderData.getString("birthdate"));
            SimpleDateFormat simpleDate = new SimpleDateFormat("dd MMMM yyyy");
            nameElder.setText(elderData.getString("name"));
            dobElder.setText(simpleDate.format(date));
            addressElder.setText(elderData.getString("address"));

            // ----------------- < BAGIAN POSE ELDER > ----------------- //
            for(int i = 0; i < poseDetection.length(); i++) {
                JSONObject obj = poseDetection.getJSONObject(i);
                if(obj.getString("house_id").equals(elderData.getString("house_id"))) {
                    if(obj.getString("camera").equals("Living Room 1")) {
                        poseLiving1.setText(obj.getString("pose"));
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