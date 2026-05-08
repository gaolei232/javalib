package com.example.config;

import com.example.entity.SystemConfig;
import com.example.entity.User;
import com.example.repository.SystemConfigRepository;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在管理员账号
        User adminUser = userService.findByEmail("admin@example.com");
        
        if (adminUser == null) {
            // 创建管理员账号
            User user = new User();
            user.setName("Admin");
            user.setEmail("admin@example.com");
            user.setPassword("123123");
            user.setRole("ADMIN");
            userService.createUser(user);
            System.out.println("管理员账号已创建: admin@example.com / 123123");
        } else {
            System.out.println("管理员账号已存在");
        }

        if (systemConfigRepository.findByConfigKey("integrity_threshold") == null) {
            SystemConfig config = new SystemConfig("integrity_threshold", "0.6");
            systemConfigRepository.save(config);
        }
    }
}
