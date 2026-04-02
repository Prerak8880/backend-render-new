package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class DeviceTelemetryRequest {
    
    @JsonProperty("apiKey")
    private String apiKey;
    
    @JsonProperty("deviceId")
    private int deviceId;
    
    @JsonProperty("sensors")
    private Map<String, Object> sensors;
    
    @JsonProperty("deviceStatus")
    private boolean deviceStatus;
    
    // Constructors
    public DeviceTelemetryRequest() {}
    
    // Getters and Setters
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    
    public int getDeviceId() { return deviceId; }
    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }
    
    public Map<String, Object> getSensors() { return sensors; }
    public void setSensors(Map<String, Object> sensors) { this.sensors = sensors; }
    
    public boolean isDeviceStatus() { return deviceStatus; }
    public void setDeviceStatus(boolean deviceStatus) { this.deviceStatus = deviceStatus; }
    
    // Helper methods to easily get sensor values
    public Double getTemperature() {
        if (sensors != null && sensors.containsKey("temperature")) {
            Object temp = sensors.get("temperature");
            if (temp instanceof Integer) {
                return ((Integer) temp).doubleValue();
            } else if (temp instanceof Double) {
                return (Double) temp;
            }
        }
        return null;
    }
    
    public Double getHumidity() {
        if (sensors != null && sensors.containsKey("humidity")) {
            Object hum = sensors.get("humidity");
            if (hum instanceof Integer) {
                return ((Integer) hum).doubleValue();
            } else if (hum instanceof Double) {
                return (Double) hum;
            }
        }
        return null;
    }
}