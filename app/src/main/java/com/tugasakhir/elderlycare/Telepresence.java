package com.tugasakhir.elderlycare;

import static com.tugasakhir.elderlycare.MainActivity.client;
import static com.tugasakhir.elderlycare.mqttServices.kitchen_gas;
import static com.tugasakhir.elderlycare.mqttServices.kitchen_light;
import static com.tugasakhir.elderlycare.mqttServices.kitchen_no;
import static com.tugasakhir.elderlycare.mqttServices.living_light;
import static com.tugasakhir.elderlycare.mqttServices.living_no;
import static com.tugasakhir.elderlycare.mqttServices.living_temp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;

import org.eclipse.paho.client.mqttv3.MqttException;

import io.github.controlwear.virtual.joystick.android.JoystickView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Telepresence#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Telepresence extends Fragment implements View.OnClickListener {

    ImageButton bUp, bDown;

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

    public Telepresence() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Telepresence.
     */
    // TODO: Rename and change types and number of parameters
    public static Telepresence newInstance(String param1, String param2) {
        Telepresence fragment = new Telepresence();
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
        return inflater.inflate(R.layout.fragment_telepresence, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        JoystickView joystick = (JoystickView) view.findViewById(R.id.JoystickControl);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // do whatever you want
                try {
                    client.publish(MainActivity.myUser+"/apps/robot/controller/move", ("{\"angle\": " + angle + ", \"strength\": " + strength + "}").getBytes(),0, false);
                    //client.publish(MainActivity.myUser+"/apps/robot/map_coord/data", ("{\"x\": " + angle + ", \"y\": " + strength + "}").getBytes(),0, true);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                Log.d("Joystick Angle", String.valueOf(angle));
                Log.d("Joystick Strength", String.valueOf(strength));
            }
        });

        bUp = (ImageButton) view.findViewById(R.id.teleNeckUp);
        bDown = (ImageButton) view.findViewById(R.id.teleNeckDown);
        bUp.setOnClickListener(this);
        bDown.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                if(bUp.isPressed()) {
                    try {
                        client.publish(MainActivity.myUser+"/apps/robot/controller/neck", ("{\"angle\": \"up\" }").getBytes(),0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                if(bDown.isPressed()) {
                    try {
                        client.publish(MainActivity.myUser+"/apps/robot/controller/neck", ("{\"angle\": \"down\" }").getBytes(),0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.teleNeckUp:
                try {
                    client.publish(MainActivity.myUser+"/apps/robot/controller/neck", ("{\"angle\": \"up\" }").getBytes(),0, false);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.teleNeckDown:
                try {
                    client.publish(MainActivity.myUser+"/apps/robot/controller/neck", ("{\"angle\": \"down\" }").getBytes(),0, false);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}