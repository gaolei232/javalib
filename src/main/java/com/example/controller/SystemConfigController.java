package com.example.controller;

import com.example.entity.SystemConfig;
import com.example.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemConfigController {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig(@RequestParam String key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key);
        Map<String, Object> response = new HashMap<>();
        if (config != null) {
            response.put("key", config.getConfigKey());
            response.put("value", config.getConfigValue());
        } else {
            response.put("key", key);
            response.put("value", null);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody Map<String, String> request) {
        String key = request.get("key");
        String value = request.get("value");
        Map<String, Object> response = new HashMap<>();

        if (key == null || key.isBlank()) {
            response.put("success", false);
            response.put("message", "配置键不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        SystemConfig config = systemConfigRepository.findByConfigKey(key);
        if (config == null) {
            config = new SystemConfig(key, value);
        } else {
            config.setConfigValue(value);
        }
        systemConfigRepository.save(config);

        response.put("success", true);
        response.put("message", "配置已更新");
        return ResponseEntity.ok(response);
    }
}
