package com.compliance.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SmartPermitMonitoringSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPermitMonitoringSystemApplication.class, args);
    }
    // MySQL database is configured in application.yml
    // http://localhost:8080/api/iot/buzzer?email=shubhkumarsinha192@gmail.com
    //1

}
