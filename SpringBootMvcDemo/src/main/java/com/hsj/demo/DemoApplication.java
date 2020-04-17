package com.hsj.demo;

import com.hsj.demo.config.PicturesUploadProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.math.BigDecimal;

@SpringBootApplication
@EnableConfigurationProperties({PicturesUploadProperties.class})
public class DemoApplication {

    public static void main(String[] args) {


        SpringApplication.run(DemoApplication.class, args);
    }

}
