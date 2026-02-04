package com.pulsewire.dataplane;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pulsewire.dataplane", "com.pulsewire.core.backbone"})
public class PulsewireDataPlaneApplication {

    public static void main(String[] args) {
        SpringApplication.run(PulsewireDataPlaneApplication.class, args);
    }
}
