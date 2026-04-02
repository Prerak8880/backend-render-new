package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class DeviceReadingResponse {
    
    @JsonProperty("id")
    private long id;
    
    @JsonProperty("deviceId")
    private int deviceId;
    
    @JsonProperty("deviceReading")
    private Map<String, Object> deviceReading;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("deviceStatus")
    private boolean deviceStatus;
    
    public DeviceReadingResponse() {}
    
    public DeviceReadingResponse(long id, int deviceId, Map<String, Object> deviceReading, 
                                String timestamp, boolean deviceStatus) {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceReading = deviceReading;
        this.timestamp = timestamp;
        this.deviceStatus = deviceStatus;
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public int getDeviceId() { return deviceId; }
    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }
    
    public Map<String, Object> getDeviceReading() { return deviceReading; }
    public void setDeviceReading(Map<String, Object> deviceReading) { this.deviceReading = deviceReading; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public boolean isDeviceStatus() { return deviceStatus; }
    public void setDeviceStatus(boolean deviceStatus) { this.deviceStatus = deviceStatus; }
}