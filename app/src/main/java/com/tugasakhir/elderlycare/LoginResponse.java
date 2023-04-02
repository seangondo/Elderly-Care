package com.tugasakhir.elderlycare;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class LoginResponse {
    private int user_id;
    private String user_name;
    private String username;
    private String email;
    private String phone_number;
    private String address;
    private String command;
    private Boolean result;

    @SerializedName("elder_list")
    private final ArrayList<Object> elder_list;

    public LoginResponse(int user_id, String user_name, String username, String email, String phone_number, String address, String command, Boolean result, ArrayList<Object> elder_list) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.username = username;
        this.email = email;
        this.phone_number = phone_number;
        this.address = address;
        this.command = command;
        this.result = result;
        this.elder_list = elder_list;
    }


    public int getUser_id() {
        return user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public String getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }

    public String getCommand() {
        return command;
    }

    public Boolean getResult() {
        return result;
    }

    public ArrayList<Object> getElder_list() {
        return elder_list;
    }
}