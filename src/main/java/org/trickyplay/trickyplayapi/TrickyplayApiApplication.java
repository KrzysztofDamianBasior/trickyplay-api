package org.trickyplay.trickyplayapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TrickyplayApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrickyplayApiApplication.class, args);
	}

}
