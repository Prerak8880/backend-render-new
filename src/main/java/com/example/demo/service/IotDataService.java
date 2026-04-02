package com.example.demo.service;

import com.example.demo.dto.DeviceReadingResponse;
import com.example.demo.dto.DeviceTelemetryRequest;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.TimeZone;

@Service
public class IotDataService {

    @Autowired
    private Firestore firestore;
    
    private static final String DEVICES_COLLECTION = "devices";
    private static final String TELEMETRY_COLLECTION = "telemetry_history";
    private static final String VALID_API_KEY = "dataiot2026";
    private static final String IST_TIMEZONE = "Asia/Kolkata";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * Get current date in IST
     */
    private Date getCurrentDateInIST() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(IST_TIMEZONE));
        return calendar.getTime();
    }
    
    /**
     * Format date to IST string
     */
    private String formatToIST(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(IST_TIMEZONE));
        return sdf.format(date);
    }
    
    /**
     * Process device data with your specific format
     */
    public Map<String, Object> processDeviceData(DeviceTelemetryRequest request) 
            throws ExecutionException, InterruptedException {
        
        // Validate API Key
        if (!VALID_API_KEY.equals(request.getApiKey())) {
            throw new SecurityException("Invalid API Key");
        }
        
        String deviceId = String.valueOf(request.getDeviceId());
        
        // Validate device ID range (3000-3999)
        if (!isValidDeviceId(deviceId)) {
            throw new IllegalArgumentException("Device ID must be between 3000-3999. Received: " + deviceId);
        }
        
        // Check if device exists, create if it doesn't
        if (!deviceExists(deviceId)) {
            createNewDevice(request);
        }
        
        // Update device's latest telemetry
        updateDeviceTelemetry(request);
        
        // Store historical data
        storeTelemetryHistory(request);
        
        // Return response with IST timestamp
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("deviceId", deviceId);
        response.put("timestamp", formatToIST(getCurrentDateInIST()));
        response.put("message", "Data received successfully");
        response.put("deviceStatus", request.isDeviceStatus());
        
        return response;
    }
    
    private void createNewDevice(DeviceTelemetryRequest request) 
            throws ExecutionException, InterruptedException {
        
        String deviceId = String.valueOf(request.getDeviceId());
        Date nowIST = getCurrentDateInIST();
        
        Map<String, Object> newDevice = new HashMap<>();
        newDevice.put("deviceId", deviceId);
        newDevice.put("deviceStatus", request.isDeviceStatus());
        newDevice.put("status", request.isDeviceStatus() ? "ONLINE" : "OFFLINE");
        newDevice.put("createdAt", nowIST);
        newDevice.put("firstSeen", nowIST);
        newDevice.put("lastSeen", nowIST);
        newDevice.put("dataCount", 0L);
        
        // Store sensor data
        Map<String, Object> sensors = new HashMap<>();
        if (request.getSensors() != null) {
            sensors.putAll(request.getSensors());
        }
        newDevice.put("sensors", sensors);
        
        // Extract temperature and humidity for easy access
        if (request.getTemperature() != null) {
            newDevice.put("temperature", request.getTemperature());
        }
        if (request.getHumidity() != null) {
            newDevice.put("humidity", request.getHumidity());
        }
        
        // Metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("autoCreated", true);
        metadata.put("creationSource", "IoT API");
        metadata.put("apiKeyUsed", request.getApiKey());
        metadata.put("timezone", "IST");
        newDevice.put("metadata", metadata);
        
        DocumentReference deviceRef = firestore.collection(DEVICES_COLLECTION).document(deviceId);
        ApiFuture<WriteResult> future = deviceRef.set(newDevice);
        future.get();
        
        System.out.println("✅ Auto-created device: " + deviceId + " at " + formatToIST(nowIST));
    }
    
    private void updateDeviceTelemetry(DeviceTelemetryRequest request) 
            throws ExecutionException, InterruptedException {
        
        String deviceId = String.valueOf(request.getDeviceId());
        DocumentReference deviceRef = firestore.collection(DEVICES_COLLECTION).document(deviceId);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastSeen", getCurrentDateInIST());
        updates.put("deviceStatus", request.isDeviceStatus());
        updates.put("status", request.isDeviceStatus() ? "ONLINE" : "OFFLINE");
        
        // Update sensors
        if (request.getSensors() != null) {
            updates.put("sensors", request.getSensors());
        }
        
        // Update temperature and humidity fields
        if (request.getTemperature() != null) {
            updates.put("temperature", request.getTemperature());
        }
        if (request.getHumidity() != null) {
            updates.put("humidity", request.getHumidity());
        }
        
        // Increment data counter
        DocumentSnapshot snapshot = deviceRef.get().get();
        Long currentCount = snapshot.getLong("dataCount");
        updates.put("dataCount", (currentCount != null) ? currentCount + 1 : 1);
        
        deviceRef.update(updates);
    }
    
    private void storeTelemetryHistory(DeviceTelemetryRequest request) {
        try {
            String deviceId = String.valueOf(request.getDeviceId());
            
            Map<String, Object> historyRecord = new HashMap<>();
            historyRecord.put("timestamp", getCurrentDateInIST()); // Store as Date object in IST
            historyRecord.put("deviceId", deviceId);
            historyRecord.put("deviceStatus", request.isDeviceStatus());
            
            // Store complete sensor data
            if (request.getSensors() != null) {
                historyRecord.put("sensors", request.getSensors());
            }
            
            // Store individual readings for easy querying
            if (request.getTemperature() != null) {
                historyRecord.put("temperature", request.getTemperature());
            }
            if (request.getHumidity() != null) {
                historyRecord.put("humidity", request.getHumidity());
            }
            
            // Add timezone info
            historyRecord.put("timezone", "IST");
            
            firestore.collection(DEVICES_COLLECTION)
                    .document(deviceId)
                    .collection(TELEMETRY_COLLECTION)
                    .add(historyRecord);
                    
        } catch (Exception e) {
            System.err.println("Failed to store history: " + e.getMessage());
        }
    }
    
    /**
     * Get device readings in the specified format (ordered by timestamp DESC) with IST timestamps
     */
    public List<DeviceReadingResponse> getDeviceReadings(String deviceId, int limit) 
            throws ExecutionException, InterruptedException {
        
        List<DeviceReadingResponse> readings = new ArrayList<>();
        
        // Get telemetry history from Firestore - ordered by timestamp descending (newest first)
        ApiFuture<QuerySnapshot> future = firestore.collection(DEVICES_COLLECTION)
                .document(deviceId)
                .collection(TELEMETRY_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
        
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data != null) {
                DeviceReadingResponse reading = new DeviceReadingResponse();
                
                // Generate unique ID (using timestamp from Firestore or current time)
                Object timestampObj = data.get("timestamp");
                if (timestampObj instanceof Date) {
                    reading.setId(((Date) timestampObj).getTime());
                } else {
                    reading.setId(System.currentTimeMillis());
                }
                
                // Set device ID
                reading.setDeviceId(Integer.parseInt(deviceId));
                
                // Extract device reading (sensors data)
                Map<String, Object> deviceReading = new HashMap<>();
                Object sensors = data.get("sensors");
                if (sensors instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> sensorsMap = (Map<String, Object>) sensors;
                    deviceReading.putAll(sensorsMap);
                } else {
                    // Fallback to individual fields
                    if (data.containsKey("temperature")) {
                        deviceReading.put("temperature", data.get("temperature"));
                    }
                    if (data.containsKey("humidity")) {
                        deviceReading.put("humidity", data.get("humidity"));
                    }
                }
                reading.setDeviceReading(deviceReading);
                
                // Format timestamp to IST string
                if (timestampObj instanceof Date) {
                    reading.setTimestamp(formatToIST((Date) timestampObj));
                } else if (timestampObj != null) {
                    reading.setTimestamp(timestampObj.toString());
                } else {
                    reading.setTimestamp(formatToIST(getCurrentDateInIST()));
                }
                
                // Set device status
                Object status = data.get("deviceStatus");
                reading.setDeviceStatus(status instanceof Boolean ? (Boolean) status : false);
                
                readings.add(reading);
            }
        }
        
        return readings;
    }
    
    /**
     * Get latest reading for a device with IST timestamp
     */
    public DeviceReadingResponse getLatestDeviceReading(String deviceId) 
            throws ExecutionException, InterruptedException {
        
        List<DeviceReadingResponse> readings = getDeviceReadings(deviceId, 1);
        if (readings.isEmpty()) {
            throw new RuntimeException("No readings found for device: " + deviceId);
        }
        return readings.get(0);
    }
    
    /**
     * Get device information (metadata) with IST timestamps
     */
    public Map<String, Object> getDeviceInfo(String deviceId) throws ExecutionException, InterruptedException {
        DocumentReference deviceRef = firestore.collection(DEVICES_COLLECTION).document(deviceId);
        DocumentSnapshot snapshot = deviceRef.get().get();
        
        if (!snapshot.exists()) {
            throw new RuntimeException("Device not found: " + deviceId);
        }
        
        Map<String, Object> deviceInfo = snapshot.getData();
        deviceInfo.put("deviceId", deviceId);
        
        // Convert timestamps to IST strings for response
        if (deviceInfo.containsKey("createdAt") && deviceInfo.get("createdAt") instanceof Date) {
            deviceInfo.put("createdAtIST", formatToIST((Date) deviceInfo.get("createdAt")));
        }
        if (deviceInfo.containsKey("lastSeen") && deviceInfo.get("lastSeen") instanceof Date) {
            deviceInfo.put("lastSeenIST", formatToIST((Date) deviceInfo.get("lastSeen")));
        }
        
        // Get recent history (last 10 readings) with IST timestamps
        ApiFuture<QuerySnapshot> historyFuture = firestore.collection(DEVICES_COLLECTION)
                .document(deviceId)
                .collection(TELEMETRY_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get();
        
        List<Map<String, Object>> recentHistory = new ArrayList<>();
        for (DocumentSnapshot doc : historyFuture.get().getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data != null) {
                if (data.containsKey("timestamp") && data.get("timestamp") instanceof Date) {
                    data.put("timestampIST", formatToIST((Date) data.get("timestamp")));
                }
                data.put("recordId", doc.getId());
                recentHistory.add(data);
            }
        }
        
        deviceInfo.put("recentTelemetry", recentHistory);
        deviceInfo.put("timezone", "IST");
        return deviceInfo;
    }
    
    /**
     * Get all devices list with IST timestamps
     */
    public List<Map<String, Object>> getAllDevices() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(DEVICES_COLLECTION).get();
        List<Map<String, Object>> devices = new ArrayList<>();
        
        for (DocumentSnapshot document : future.get().getDocuments()) {
            Map<String, Object> device = document.getData();
            if (device != null) {
                device.put("deviceId", document.getId());
                
                // Convert timestamps to IST strings
                if (device.containsKey("createdAt") && device.get("createdAt") instanceof Date) {
                    device.put("createdAtIST", formatToIST((Date) device.get("createdAt")));
                }
                if (device.containsKey("lastSeen") && device.get("lastSeen") instanceof Date) {
                    device.put("lastSeenIST", formatToIST((Date) device.get("lastSeen")));
                }
                
                devices.add(device);
            }
        }
        return devices;
    }
    
    private boolean deviceExists(String deviceId) throws ExecutionException, InterruptedException {
        DocumentReference deviceRef = firestore.collection(DEVICES_COLLECTION).document(deviceId);
        return deviceRef.get().get().exists();
    }
    
    private boolean isValidDeviceId(String deviceId) {
        if (deviceId == null) return false;
        try {
            int id = Integer.parseInt(deviceId);
            return id >= 3000 && id <= 3999;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}