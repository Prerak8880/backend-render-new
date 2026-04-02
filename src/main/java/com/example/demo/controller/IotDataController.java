package com.example.demo.controller;

import com.example.demo.dto.DeviceReadingResponse;
import com.example.demo.dto.DeviceTelemetryRequest;
import com.example.demo.service.IotDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/farmer/iot/data")
public class IotDataController {

    @Autowired
    private IotDataService iotDataService;

    /**
     * POST endpoint - Receive device data (auto-creates device)
     */
    @PostMapping
    public ResponseEntity<?> receiveDeviceData(@RequestBody DeviceTelemetryRequest request) {
        try {
            // Validate required fields
            if (request.getApiKey() == null || request.getApiKey().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "API Key is required",
                    "status", "failed"
                ));
            }
            
            Map<String, Object> result = iotDataService.processDeviceData(request);
            return ResponseEntity.ok(result);
            
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "error", e.getMessage(),
                        "status", "failed"
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "status", "failed"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Failed to process data: " + e.getMessage(),
                        "status", "failed"
                    ));
        }
    }
    
    /**
     * GET endpoint - Get device readings with limit
     * URL Pattern: /api/farmer/iot/data/{deviceId}/{limit}
     * Example: /api/farmer/iot/data/3010/10 (last 10 readings)
     * Example: /api/farmer/iot/data/3010/1 (latest reading)
     */
    @GetMapping("/{deviceId}/{limit}")
    public ResponseEntity<?> getDeviceReadingsWithLimit(
            @PathVariable String deviceId,
            @PathVariable int limit) {
        try {
            // Validate device ID range (3000-3999)
            if (!isValidDeviceId(deviceId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Device ID must be between 3000-3999",
                    "receivedId", deviceId,
                    "status", "failed"
                ));
            }
            
            // Validate limit (must be positive)
            if (limit <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Limit must be a positive number",
                    "status", "failed"
                ));
            }
            
            List<DeviceReadingResponse> readings = iotDataService.getDeviceReadings(deviceId, limit);
            
            if (readings.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                            "error", "No readings found for device: " + deviceId,
                            "status", "failed"
                        ));
            }
            
            return ResponseEntity.ok(readings);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Failed to get readings: " + e.getMessage(),
                        "status", "failed"
                    ));
        }
    }
    
    /**
     * GET endpoint - Get device information (metadata)
     * Example: /api/farmer/iot/data/3010
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<?> getDeviceData(@PathVariable String deviceId) {
        try {
            Map<String, Object> deviceInfo = iotDataService.getDeviceInfo(deviceId);
            return ResponseEntity.ok(deviceInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "error", "Device not found: " + e.getMessage(),
                        "status", "failed"
                    ));
        }
    }
    
    /**
     * GET endpoint - Get all devices list
     * Example: /api/farmer/iot/data/devices/all
     */
    @GetMapping("/devices/all")
    public ResponseEntity<?> getAllDevices() {
        try {
            List<Map<String, Object>> devices = iotDataService.getAllDevices();
            return ResponseEntity.ok(Map.of(
                "devices", devices,
                "count", devices.size(),
                "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Failed to retrieve devices: " + e.getMessage(),
                        "status", "failed"
                    ));
        }
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