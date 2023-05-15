package com.tugasakhir.elderlycare;

import static com.tugasakhir.elderlycare.ui.ElderSelectorActivity.elderSelected;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.tugasakhir.elderlycare.adapter.RecycleButtonAdapter;
import com.tugasakhir.elderlycare.adapter.RecycleSensorAdapter;
import com.tugasakhir.elderlycare.api.RecyclerViewInterface;
import com.tugasakhir.elderlycare.handler.DBHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TestTabLayout#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TestTabLayout extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TabLayout tabLayout;
    JSONObject elder;
    String house_id = null;
    ArrayList<String> room = new ArrayList<>();
    HashSet<String> hashSet = new HashSet<String>();
    RecyclerView recyclerView, sensorRecycler;

    public TestTabLayout() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TestTabLayout.
     */
    // TODO: Rename and change types and number of parameters
    public static TestTabLayout newInstance(String param1, String param2) {
        TestTabLayout fragment = new TestTabLayout();
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
        return inflater.inflate(R.layout.fragment_test_tab_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = (TabLayout) view.findViewById(R.id.simpleTabLayout);
        ArrayList<String> tab = new ArrayList<>();

        recyclerView = view.findViewById(R.id.itemTabButton);
        sensorRecycler = view.findViewById(R.id.itemTab);
        DBHandler myDb = new DBHandler(getContext());
        try {
            elder = myDb.getElderData(elderSelected);
            house_id = elder.getString("house_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray mySensor = myDb.getSensorFromId(house_id);
            for(int i = 0; i < mySensor.length(); i++){
                JSONObject list = mySensor.getJSONObject(i);
                room.add(list.getString("room"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray mySensor = myDb.getButtonFromHouseID(house_id);
            for(int i = 0; i < mySensor.length(); i++){
                JSONObject list = mySensor.getJSONObject(i);
                room.add(list.getString("room"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        myDb.close();

        hashSet.addAll(room);
        room.clear();
        room.addAll(hashSet);

//        tab.add("Kitchen");
//        tab.add("Living Room");
//        tab.add("Bed Room");
//        tab.add("Bathroom");

        createTab(room);
        createInside(tabLayout.getSelectedTabPosition());


        Log.e("Current", String.valueOf(tabLayout.getSelectedTabPosition()));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e("Selected", String.valueOf(tab.getPosition()));
                createInside(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.e("UnSelected", String.valueOf(tab.getPosition()));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

                Log.e("Reselected", String.valueOf(tab.getPosition()));
            }
        });
    }

    private void createTab(ArrayList<String> obj) {
        for(int i = 0; i < obj.size(); i++) {
            TabLayout.Tab add = tabLayout.newTab();
            add.setText(obj.get(i));
            tabLayout.addTab(add);
        }
    }

    private void createInside(int tabNum) {
        DBHandler myDb = new DBHandler(getContext());
        try {
            JSONArray buttonList = myDb.getButton(elder.getString("house_id"), room.get(tabNum));
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);
            ArrayList<String> type1 = new ArrayList<>();
            ArrayList<String> value = new ArrayList<>();
            ArrayList<String> type = new ArrayList<>();
            for(int i = 0; i < buttonList.length(); i++) {
                JSONObject obj = buttonList.getJSONObject(i);
                type1.add(obj.getString("type"));
                value.add("OFF");
                type.add(obj.getString("type"));
            }

            RecycleButtonAdapter adapter = new RecycleButtonAdapter(type1, type, value, getContext(), new RecyclerViewInterface() {
                @Override
                public void onItemClick(int pos) {

            //        name.get(pos);
                    Log.e("Pressed", String.valueOf(pos));
                }
            });
            recyclerView.setAdapter(adapter);


            JSONArray sensorList = myDb.getSensor(elder.getString("house_id"), room.get(tabNum));
            Log.e("Sensor list", String.valueOf(sensorList));
            LinearLayoutManager sensorLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            sensorRecycler.setLayoutManager(sensorLayout);
            ArrayList<String> title = new ArrayList<>();
            ArrayList<String> trend = new ArrayList<>();
            for(int i = 0; i < sensorList.length(); i++) {
                JSONObject obj = sensorList.getJSONObject(i);
                title.add(obj.getString("type"));
                trend.add(obj.getString("trend"));
            }

            RecycleSensorAdapter sensorAdapter = new RecycleSensorAdapter(title, trend, getContext(), new RecyclerViewInterface() {
                @Override
                public void onItemClick(int pos) {

                    //        name.get(pos);
                    Log.e("Pressed", String.valueOf(pos));
                }
            });
            sensorRecycler.setAdapter(sensorAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void onItemClick(int pos) {
////        name.get(pos);
//        Log.e("Pressed", String.valueOf(pos));
//    }
}