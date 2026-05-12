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
    //
}
