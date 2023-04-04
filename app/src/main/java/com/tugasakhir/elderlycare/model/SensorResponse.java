package com.tugasakhir.elderlycare.model;

public class SensorResponse {
    private String house_id;
    private String room;
    private String sensor_type;
    private String trend;

    public SensorResponse(String house_id, String room, String sensor_type, String trend) {
        this.house_id = house_id;
        this.room = room;
        this.sensor_type = sensor_type;
        this.trend = trend;
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

    public String getTrend() {
        return trend;
    }
}
