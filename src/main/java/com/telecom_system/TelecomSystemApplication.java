package com.telecom_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TelecomSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelecomSystemApplication.class, args);
        System.out.println("电信信息采集系统启动成功！");
        System.out.println("访问: http://localhost:8080");
		
	};
	

}
