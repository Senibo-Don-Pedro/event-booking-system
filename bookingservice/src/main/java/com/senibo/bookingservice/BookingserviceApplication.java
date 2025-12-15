package com.senibo.bookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients // ✅ NEW: Enables OpenFeign clients
@EnableJpaAuditing // ✅ NEW: Enables @CreatedDate, @LastModifiedDate
public class BookingserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingserviceApplication.class, args);
	}

}
