package com.tugasakhir.elderlycare.ui;

import static com.tugasakhir.elderlycare.ui.MainActivity.dataElder;
import static com.tugasakhir.elderlycare.ui.MainActivity.myServer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.adapter.RecyclerViewAdapter;
import com.tugasakhir.elderlycare.api.RecyclerViewInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ElderSelectorActivity extends AppCompatActivity implements RecyclerViewInterface {

    private ArrayList<Integer> myId = new ArrayList<>();
    private ArrayList<String> myName = new ArrayList<>();
    private ArrayList<String> myAddress = new ArrayList<>();
    private ArrayList<String> myHouse = new ArrayList<>();
    private ArrayList<String> myRobot = new ArrayList<>();
    private ArrayList<String> myWearable = new ArrayList<>();
    private ArrayList<String> myImage = new ArrayList<>();

    public static int elderSelected = 0;
    public static String elderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_selector);
        detectElder();
    }

    private void detectElder() {
        JSONArray jsArray = new JSONArray(dataElder);
        JSONObject arrObj = null;
        try {
            for(int i = 0; i < jsArray.length(); i++) {
                arrObj = jsArray.getJSONObject(i);
                Log.e("Hasil", String.valueOf(arrObj));
                myId.add(arrObj.getInt("elder_id"));
                myName.add(arrObj.getString("name"));
                myAddress.add(arrObj.getString("address"));
                myHouse.add(arrObj.getString("house_id"));
                myRobot.add(arrObj.getString("robot_id"));
                myWearable.add(arrObj.getString("watch_id"));
                myImage.add(myServer+"/image/"+arrObj.getString("image"));
            }
            initRecyclerView();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = this.findViewById(R.id.elderRecycler);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, myId, myName, myAddress, myImage, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int pos) {
        elderSelected = myId.get(pos);
        elderName = myName.get(pos);
//        loadingDialog.startDialog();
//        loadingDialog.dismissDialog();
        Log.e("Pressed", elderName);
        goToHome();
//        startMqtt(myId.get(pos));
    }



    private void goToHome() {
        Intent i = new Intent(this, MainActivity2.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }
}