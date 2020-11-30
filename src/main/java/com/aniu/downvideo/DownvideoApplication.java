package com.aniu.downvideo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan({"com.aniu.downvideo.mapper"})
public class DownvideoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DownvideoApplication.class, args);
    }

}
