package com.tugasakhir.elderlycare.model;

public class ButtonResponse {
    private String house_id;
    private String room;
    private String sensor_type;

    public ButtonResponse(String house_id, String room, String sensor_type) {
        this.house_id = house_id;
        this.room = room;
        this.sensor_type = sensor_type;
    }

    public String getHouse_id() {
        return house_id;
    }

    public String getRoom() {
        return room;
    }

    public String getSensor_type() {
        return sensor_type;
    }
}
