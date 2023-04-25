package com.tugasakhir.elderlycare.handler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tugasakhir.elderlycare.model.LoginResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    static int DB_ver = 1;
    static String DB_name = "elderlyApps_db";

    //Auto login DB
    static String INFO_TABLE = "caregiver_info";
    static String INFO_id = "id";
    static String INFO_name = "name";
    static String INFO_username = "username";
    static String INFO_password = "password";
    static String INFO_email = "email";
    static String INFO_phoneNumber = "phoneNumber";
    static String INFO_address = "address";
    static String INFO_autologin = "autologin";

    //Elder Table
    static String ELDER_TABLE = "elder_info";
    static String ELDER_id = "id";
    static String ELDER_name = "name";
    static String ELDER_address = "address";
    static String ELDER_birthdate = "birthdate";
    static String ELDER_house = "house_id";
    static String ELDER_robot = "robot_id";
    static String ELDER_watch = "watch_id";
    static String ELDER_image = "image";

    //Sensor Table
    static String SENSOR_TABLE = "db_sensor";
    static String SENSOR_houseID = "house_id";
    static String SENSOR_room = "room";
    static String SENSOR_type = "sensor_type";
    static String SENSOR_trend = "trend";

    //Button Table
    static String BUTTON_TABLE = "db_button";
    static String BUTTON_houseID = "house_id";
    static String BUTTON_room = "room";
    static String BUTTON_type = "button_type";


    public DBHandler (Context c) {
        super(c, DB_name, null, DB_ver);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREATE USER LOGIN INFO
        String tbUser = "CREATE TABLE " + INFO_TABLE + " ("
                + INFO_id + " INTEGER, "
                + INFO_name + " VARCHAR(50), "
                + INFO_username + " VARCHAR(50), "
                + INFO_password + " VARCHAR(50), "
                + INFO_email + " VARCHAR(50), "
                + INFO_phoneNumber + " VARCHAR(50), "
                + INFO_address + " VARCHAR(50), "
                + INFO_autologin + " BOOLEAN);";
        db.execSQL(tbUser);
        Log.e("Database", tbUser);

        // CREATE ELDER TABLE INFO
        String tbElder = "CREATE TABLE " + ELDER_TABLE + " ("
                + ELDER_id + " INTEGER, "
                + ELDER_name + " VARCHAR(50), "
                + ELDER_address + " VARCHAR(50), "
                + ELDER_birthdate + " DATE, "
                + ELDER_house + " VARCHAR(50), "
                + ELDER_robot + " VARCHAR(50), "
                + ELDER_watch + " VARCHAR(50), "
                + ELDER_image + " VARCHAR(50));";
        db.execSQL(tbElder);
        Log.e("Database", tbElder);

        // CREATE SENSOR LIST
        String tbSensor = "CREATE TABLE " + SENSOR_TABLE + " ("
                + SENSOR_houseID + " VARCHAR(50), "
                + SENSOR_room + " VARCHAR(50), "
                + SENSOR_type + " VARCHAR(50), "
                + SENSOR_trend + " VARCHAR(50));";
        db.execSQL(tbSensor);
        Log.e("Database", tbSensor);


        // CREATE SENSOR LIST
        String tbButton = "CREATE TABLE " + BUTTON_TABLE + " ("
                + BUTTON_houseID + " VARCHAR(50), "
                + BUTTON_room + " VARCHAR(50), "
                + BUTTON_type + " VARCHAR(50));";
        db.execSQL(tbButton);
        Log.e("Database", tbButton);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+INFO_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ELDER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+SENSOR_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+BUTTON_TABLE);
        onCreate(db);
    }

    // ---------------------------------------- < LOGIN DATABASE > ---------------------------------------- //
    public void insertLogin(LoginResponse user, String password, boolean autoLog) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(INFO_id, user.getUser_id());
        cv.put(INFO_name, user.getUser_name());
        cv.put(INFO_username, user.getUsername());
        cv.put(INFO_password, password);
        cv.put(INFO_email, user.getEmail());
        cv.put(INFO_phoneNumber, user.getPhone_number());
        cv.put(INFO_address, user.getAddress());
        cv.put(INFO_autologin, autoLog);
        db.insert(INFO_TABLE, null, cv);
        Log.e("Entry Data", " SUCCESS!");
    }

    public Cursor getLoginInfo() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + INFO_TABLE, null);

        return cursor;
    }

    public JSONArray LoginData() throws JSONException {
        JSONArray allData = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + INFO_TABLE, null);

        if(c.moveToFirst()) {
            do {
                JSONObject dataObj = new JSONObject();
                dataObj.put("username", c.getString(2));
                dataObj.put("password", c.getString(3));
                dataObj.put("autoLog", c.getInt(7));
                allData.put(dataObj);

            } while (c.moveToNext());
        }

        return allData;
    }

    public void deleteLogin(String table, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table, INFO_username + "=" + username, null);
    }

    public void deleteLoginAll() {
        SQLiteDatabase dbR = this.getReadableDatabase();
        Cursor c = dbR.rawQuery("SELECT * FROM " + INFO_TABLE, null);
        ArrayList<Integer> info = new ArrayList<>();

        if(c.moveToFirst()) {
            do {
                info.add(c.getInt(0));
            } while (c.moveToNext());
        }

        SQLiteDatabase dbW = this.getWritableDatabase();
        for(int i = 0; i < info.size(); i++) {
            dbW.delete(INFO_TABLE, INFO_id + "=" + info.get(i), null);
        }
    }


    // ---------------------------------------- < ELDER DATABASE > ---------------------------------------- //
    public void insertElder(ArrayList<Object> data) {
        JSONArray jsArray = new JSONArray(data);
        JSONObject arrObj = null;
        try {
            for(int i = 0; i < jsArray.length(); i++) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues cv = new ContentValues();
                arrObj = jsArray.getJSONObject(i);
                cv.put(ELDER_id, arrObj.getInt("elder_id"));
                cv.put(ELDER_name, arrObj.getString("name"));
                cv.put(ELDER_address, arrObj.getString("address"));
                cv.put(ELDER_birthdate, arrObj.getString("birthdate"));
                cv.put(ELDER_house, arrObj.getString("house_id"));
                cv.put(ELDER_robot, arrObj.getString("robot_id"));
                cv.put(ELDER_watch, arrObj.getString("watch_id"));
                cv.put(ELDER_image, arrObj.getString("image"));
                db.insert(ELDER_TABLE, null, cv);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject getElderData (int id) throws JSONException {
        JSONObject data = new JSONObject();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + ELDER_TABLE + " WHERE id=" + id, null);

        if(c.moveToFirst()) {
            do {
                data.put("elder_id", c.getInt(0));
                data.put("name", c.getString(1));
                data.put("address", c.getString(2));
                data.put("birthdate", c.getString(3));
                data.put("house_id", c.getString(4));
                data.put("robot_id", c.getString(5));
                data.put("watch_id", c.getString(6));
                data.put("image", c.getString(7));
            } while (c.moveToNext());
        }

        return data;
    }

    public JSONArray getElderDataAll() throws JSONException {
        JSONArray allData = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + INFO_TABLE, null);

        if(c.moveToFirst()) {
            do {
                JSONObject dataObj = new JSONObject();
                dataObj.put("elder_id", c.getString(0));
                dataObj.put("name", c.getString(1));
                dataObj.put("address", c.getInt(2));
                dataObj.put("house_id", c.getInt(3));
                dataObj.put("robot_id", c.getInt(4));
                dataObj.put("watch_id", c.getInt(5));
                dataObj.put("image", c.getInt(6));
                allData.put(dataObj);

            } while (c.moveToNext());
        }
        return allData;
    }

    public void deleteElderAll() {
        SQLiteDatabase dbR = this.getReadableDatabase();
        Cursor c = dbR.rawQuery("SELECT * FROM " + ELDER_TABLE, null);
        ArrayList<Integer> elderId = new ArrayList<>();

        if(c.moveToFirst()) {
            do {
                elderId.add(c.getInt(0));
            } while (c.moveToNext());
        }

        SQLiteDatabase dbW = this.getWritableDatabase();
        for(int i = 0; i < elderId.size(); i++) {
            dbW.delete(ELDER_TABLE, ELDER_id + "=" + elderId.get(i), null);
        }
    }


    // ---------------------------------------- < SENSOR DATABASE > ---------------------------------------- //
    // TODO Create sensor table function
    public void insertSensor(JSONArray data) {
        JSONObject arrObj = null;
        try {
            for(int i = 0; i < data.length(); i++) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues cv = new ContentValues();
                arrObj = data.getJSONObject(i);
                cv.put(SENSOR_houseID, arrObj.getString("house_id"));
                cv.put(SENSOR_room, arrObj.getString("room"));
                cv.put(SENSOR_type, arrObj.getString("sensor_type"));
                cv.put(SENSOR_trend, arrObj.getString("trend"));
                db.insert(SENSOR_TABLE, null, cv);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject getSensorFromId (String house_id) throws JSONException {
        JSONObject data = new JSONObject();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SENSOR_TABLE + " WHERE house_id= '" + house_id +"'", null);

        if(c.moveToFirst()) {
            do {
                data.put("house_id", c.getInt(0));
                data.put("room", c.getString(1));
                data.put("type", c.getString(2));
                data.put("trend", c.getString(3));

            } while (c.moveToNext());
        }

        return data;
    }

    public void deleteAllSensor() {
        SQLiteDatabase dbR = this.getReadableDatabase();
        Cursor c = dbR.rawQuery("SELECT * FROM " + SENSOR_TABLE, null);
        ArrayList<String> sensor = new ArrayList<>();

        if(c.moveToFirst()) {
            do {
                sensor.add(c.getString(0));
            } while (c.moveToNext());
        }

        SQLiteDatabase dbW = this.getWritableDatabase();
        for(int i = 0; i < sensor.size(); i++) {
            dbW.delete(SENSOR_TABLE, SENSOR_houseID + "=" + sensor.get(i), null);
        }
    }



    // ---------------------------------------- < BUTTON DATABASE > ---------------------------------------- //
    // TODO Create button table function
    public void insertButton(JSONArray data) {
        JSONObject arrObj = null;
        try {
            for(int i = 0; i < data.length(); i++) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues cv = new ContentValues();
                arrObj = data.getJSONObject(i);
                cv.put(BUTTON_houseID, arrObj.getInt("house_id"));
                cv.put(BUTTON_room, arrObj.getString("room"));
                cv.put(BUTTON_type, arrObj.getString("button_type"));
                db.insert(BUTTON_TABLE, null, cv);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllButton() {
        SQLiteDatabase dbR = this.getReadableDatabase();
        Cursor c = dbR.rawQuery("SELECT * FROM " + BUTTON_TABLE, null);
        ArrayList<String> button = new ArrayList<>();

        if(c.moveToFirst()) {
            do {
                button.add(c.getString(0));
            } while (c.moveToNext());
        }

        SQLiteDatabase dbW = this.getWritableDatabase();
        for(int i = 0; i < button.size(); i++) {
            dbW.delete(BUTTON_TABLE, BUTTON_houseID + "=" + button.get(i), null);
        }
    }
}
