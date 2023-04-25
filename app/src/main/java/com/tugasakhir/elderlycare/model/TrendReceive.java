package com.tugasakhir.elderlycare.model;

public class TrendReceive {
    private String house_id;
    private String type;
    private Integer number;
    private Integer value;
    private String date;
    private String time;

    public TrendReceive(String house_id, String type, Integer number, Integer value, String date, String time) {
        this.house_id = house_id;
        this.type = type;
        this.number = number;
        this.value = value;
        this.date = date;
        this.time = time;
    }

    public String getHouse_id() {
        return house_id;
    }

    public void setHouse_id(String house_id) {
        this.house_id = house_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
