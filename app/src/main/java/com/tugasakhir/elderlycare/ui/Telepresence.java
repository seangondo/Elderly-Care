package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.service.mqttServices.statusRobot;
import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;
import static com.tugasakhir.elderlycare.ui.MainActivity.client;

import android.net.http.SslError;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.handler.DBHandler;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import io.github.controlwear.virtual.joystick.android.JoystickView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Telepresence#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Telepresence extends Fragment implements View.OnClickListener,
    View.OnTouchListener{

    ImageButton bUp, bDown;
    ImageButton forward, backward, left, right;
    Button bNav, bRtc, bNavCancel, bDock;

    long lastClick = 0;

    private int strength = 0;

    int xVal, yVal;
    ArrayList<String> pointItems = new ArrayList<>();
    AutoCompleteTextView pointCoordName;
    ArrayAdapter<String> adapterPoint;

    WebView myWebView;

    boolean navigate = false;

    TextView tvWeb;

    DBHandler myDb;
    JSONObject elder;

    boolean stats = true;
    boolean statsOld = true;

    boolean bSend = false;
    boolean bSendOld = false;

    //Updater
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 100;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        JoystickView joystick = (JoystickView) view.findViewById(R.id.JoystickControl);
//        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
//            @Override
//            public void onMove(int angle, int strength) {
//                // do whatever you want
//                try {
//                    client.publish(elderSelected+"/apps/robot/controller/move", ("{\"angle\": " + angle + ", \"strength\": " + strength + "}").getBytes(),0, false);
//                    //client.publish(MainActivity.myUser+"/apps/robot/map_coord/data", ("{\"x\": " + angle + ", \"y\": " + strength + "}").getBytes(),0, true);
//                } catch (MqttException e) {
//                    e.printStackTrace();
//                }
////                Log.d("Joystick Angle", String.valueOf(angle));
////                Log.d("Joystick Strength", String.valueOf(strength));
//            }
//        }, 100);

        pointCoordName = (AutoCompleteTextView) view.findViewById(R.id.coord_map);

        try {
            myDb = new DBHandler(getContext());
            elder = myDb.getElderData(elderSelected);
            String house_id = elder.getString("house_id");
            JSONArray pointList = myDb.getPointFromId(house_id);
            myDb.close();
            for(int i = 0; i < pointList.length(); i++) {
                JSONObject arrObj = pointList.getJSONObject(i);
                pointItems.add(arrObj.getString("name"));
            }
            adapterPoint = new ArrayAdapter<String>(getContext(), R.layout.list_item_point, pointItems);
            pointCoordName.setAdapter(adapterPoint);
            pointCoordName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    pointCoordName.setListSelection(position);
                    Log.e("Selected item", selectedItem);
                    try {
                        myDb = new DBHandler(getContext());
                        JSONObject getPoint = myDb.getCoordFromPoint(selectedItem);
                        myDb.close();
                        xVal = getPoint.getInt("x");
                        yVal = getPoint.getInt("y");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }

        forward = (ImageButton) view.findViewById(R.id.controlForward);
        backward = (ImageButton) view.findViewById(R.id.controlBack);
        left = (ImageButton) view.findViewById(R.id.controlLeft);
        right = (ImageButton) view.findViewById(R.id.controlRight);

        bUp = (ImageButton) view.findViewById(R.id.teleNeckUp);
        bDown = (ImageButton) view.findViewById(R.id.teleNeckDown);
        bNav = (Button) view.findViewById(R.id.teleNav);
        bNavCancel = (Button) view.findViewById(R.id.teleStop);
        bRtc = (Button) view.findViewById(R.id.startWebrtc);
        myWebView = (WebView) view.findViewById(R.id.webrtc);
        tvWeb = (TextView) view.findViewById(R.id.msgWebview);
        bDock = (Button) view.findViewById(R.id.teleDock);

//        forward.setOnClickListener(this);
//        backward.setOnClickListener(this);
//        left.setOnClickListener(this);
//        right.setOnClickListener(this);


        forward.setOnTouchListener(this);
        backward.setOnTouchListener(this);
        left.setOnTouchListener(this);
        right.setOnTouchListener(this);

        bUp.setOnClickListener(this);
        bDown.setOnClickListener(this);
        bNavCancel.setOnClickListener(this);
        bNav.setOnClickListener(this);
        bRtc.setOnClickListener(this);
        bDock.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {

                for(int i = 0; i < statusRobot.length(); i ++) {
                    try {
                        JSONObject myObj = statusRobot.getJSONObject(i);
                        if(myObj.getString("robot_id").equals(elder.getString("robot_id"))
                                && myObj.getInt("status") == 3) {
                            if(statsOld != stats) {
                                statsOld = stats;
                                navigate = false;
                                Toast.makeText(getContext(), "Navigation done!", Toast.LENGTH_LONG).show();
                            }
                        } else if (myObj.getString("robot_id").equals(elder.getString("robot_id"))
                                && myObj.getInt("status") == 1){
                            navigate = true;
                            statsOld = false;
                        } else if (myObj.getString("robot_id").equals(elder.getString("robot_id"))
                                && myObj.getInt("status") == 2){
                            if(statsOld != stats) {
                                statsOld = stats;
                                navigate = false;
                                Toast.makeText(getContext(), "Navigation Stopped!", Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if(!navigate) {
                    if (bUp.isPressed()) {
                        try {
                            bSend = true;
                            Log.e("Topic", String.valueOf(elderSelected) + "/apps/robot/controller/neck");
                            Log.e("MSG", "{\"angle\": \"up\" }");
                            client.publish(String.valueOf(elderSelected) + "/apps/robot/controller/neck", ("{\"angle\": \"up\"}").getBytes(), 0, false);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                    if (bDown.isPressed()) {
                        try {
                            bSend = true;
                            client.publish(elderSelected + "/apps/robot/controller/neck", ("{\"angle\": \"down\" }").getBytes(), 0, false);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }
                handler.postDelayed(runnable, delay);
                if(!forward.isPressed() && !backward.isPressed() && !left.isPressed() && !right.isPressed() && strength > 0) {
                    try {
                        if(bSend) {
                            bSend = false;
                            client.publish(elderSelected+"/apps/robot/controller/move", ("{\"angle\": 90 , \"strength\": 0 }").getBytes(),0, false);
                            strength = 0;
                        }

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
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
        long currentClick = System.currentTimeMillis();
        if (currentClick - lastClick > 500) {
            lastClick = currentClick;
            switch (view.getId()) {
                case R.id.teleNeckUp:
                    if (!navigate) {
                        try {
                            client.publish(elderSelected + "/apps/robot/controller/neck", ("{\"angle\": \"up\" }").getBytes(), 0, false);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case R.id.teleNeckDown:
                    if (!navigate) {
                        try {
                            client.publish(elderSelected + "/apps/robot/controller/neck", ("{\"angle\": \"down\" }").getBytes(), 0, false);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case R.id.teleNav:
                    Log.e("X Coord", String.valueOf(xVal));
                    Log.e("Y Coord", String.valueOf(yVal));
                    for (int i = 0; i < statusRobot.length(); i++) {
                        try {
                            JSONObject myObj = statusRobot.getJSONObject(i);
                            if (myObj.getString("robot_id").equals(elder.getString("robot_id"))
                                    && myObj.getInt("status") != 1) {
                                try {
                                    client.publish(elderSelected + "/apps/robot/controller/map_coord", ("{\"x\": " + xVal + ", \"y\": " + yVal + " }").getBytes(), 0, false);
                                    Toast.makeText(getContext(), "Navigating robot!", Toast.LENGTH_LONG).show();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getContext(), "Robot still navigating, please wait!", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case R.id.startWebrtc:
                    Log.e("Val", bRtc.getText().toString());
                    if (bRtc.getText().toString().equals("Start WebRTC")) {
                        Log.e("RTC", "Masok Start");
                        bRtc.setText("Stop WebRTC");
                        myDb = new DBHandler(getActivity());
                        try {
                            JSONArray array = myDb.LoginData();
                            myDb.close();
                            JSONObject obj;
                            for (int i = 0; i < array.length(); i++) {
                                obj = array.getJSONObject(i);
                                if (obj.getInt("autoLog") == 1) {
                                    startWebRTC(obj.getString("username"), obj.getString("password"), String.valueOf(elderSelected));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (bRtc.getText().toString().equals("Stop WebRTC")) {
                        bRtc.setText("Start WebRTC");
                        tvWeb.setText("Start WebRTC");
                        Log.e("RTC", "Masok STOP");
                        myWebView.setVisibility(View.INVISIBLE);
                        myWebView.loadUrl("https://private-server.uk.to/");
                    }
                    break;

                case R.id.teleStop:
                    try {
                        client.publish(elderSelected + "/apps/robot/controller/cancel", ("{\"command\": \"stop\"}").getBytes(), 0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.teleDock:
                    for (int i = 0; i < statusRobot.length(); i++) {
                        try {
                            JSONObject myObj = statusRobot.getJSONObject(i);
                            if (myObj.getString("robot_id").equals(elder.getString("robot_id"))
                                    && myObj.getInt("status") != 1) {
                                try {
                                    client.publish(elderSelected + "/apps/robot/controller/map_coord", ("{\"x\": 2, \"y\": 0 }").getBytes(), 0, false);
                                    Toast.makeText(getContext(), "Navigating robot!", Toast.LENGTH_LONG).show();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getContext(), "Robot still navigating, please wait!", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            // CONTROL ROBOT CONTROLLER
            case R.id.controlForward:
                try {
                    bSend = true;
                    client.publish(elderSelected+"/apps/robot/controller/move", ("{\"angle\": 90 , \"strength\": 100 }").getBytes(),0, false);
                    strength = 100;
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.controlBack:
                try {
                    bSend = true;
                    client.publish(elderSelected + "/apps/robot/controller/move", ("{\"angle\": 270 , \"strength\": 100 }").getBytes(), 0, false);
                    strength = 100;
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.controlLeft:
                try {
                    bSend = true;
                    client.publish(elderSelected + "/apps/robot/controller/move", ("{\"angle\": 180 , \"strength\": 100 }").getBytes(), 0, false);
                    strength = 100;
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.controlRight:
                try {
                    bSend = true;
                    client.publish(elderSelected + "/apps/robot/controller/move", ("{\"angle\": 0 , \"strength\": 100 }").getBytes(), 0, false);
                    strength = 100;
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;
        }
        return false;
    }

    private void startWebRTC(String caregiver, String caregiverPass, String id){
        Log.e("Data", caregiver + "; " + caregiverPass + "; " + id);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
//        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.e("MyApplication", consoleMessage.message() + " -- From line " + consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
                if(consoleMessage.message().contains("connection is closed")) {
                    tvWeb.setText("Start WebRTC");
                    bRtc.setText("Start WebRTC");
                    Log.e("RTC", "Masok STOP");
                    myWebView.setVisibility(View.INVISIBLE);
                    myWebView.loadUrl("https://private-server.uk.to/");
//                } else if(consoleMessage.message().contains("Room full")) {
//                    tvWeb.setText("Room full, try again later!");
//                    bRtc.setText("Start WebRTC");
//                    Log.e("RTC", "Masok STOP");
//                    myWebView.setVisibility(View.INVISIBLE);
//                    myWebView.loadUrl("https://private-server.uk.to/");
//                }
                }
                return true;
            }
        });
        myWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed();
            }
        });
        String Post = "";
        try {
            Post += "username="+ URLEncoder.encode(caregiver,"UTF-8")
                    + "&password=" + URLEncoder.encode(caregiverPass,"UTF-8")
                    + "&elder=" + URLEncoder.encode(id,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        myWebView.postUrl("https://private-server.uk.to/caregiver/webrtc.php", Post.getBytes());
        myWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        myWebView.setVisibility(View.VISIBLE);
    }

}