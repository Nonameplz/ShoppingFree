package com.nonameplz.sfservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@MapperScan("com.nonameplz.sfservice.mapper")
@ConfigurationPropertiesScan
@SpringBootApplication
public class SfServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SfServiceApplication.class, args);
    }

}
