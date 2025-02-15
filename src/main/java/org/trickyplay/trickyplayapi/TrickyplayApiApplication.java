package org.trickyplay.trickyplayapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TrickyplayApiApplication {

	public static void main(String[] args) {
// set the context path and port number as a Java system property before the context is initialized
//		System.setProperty("server.servlet.context-path", "/myContext");
//		System.setProperty("server.port", "9090");

		SpringApplication.run(TrickyplayApiApplication.class, args);
		// System.out.println(System.getProperty("spring.profiles.active", "unknown profile- sth went wrong"));

		// @Value("${spring.profiles.active:Unknown}")
		// private String activeProfile;

		// @Autowired Environment env; -offers:
		// String[] getActiveProfiles(),
		// String[] getDefaultProfiles()
		// boolean acceptsProfiles(String... profiles)

		//System.getenv(String name), System.getProperty(String key)
		//Map<String, String> mapProperties = new HashMap<String, String>();
		//Properties systemProperties = System.getProperties();
		//for(Entry<Object, Object> x : systemProperties.entrySet()) {
		//    mapProperties.put((String)x.getKey(), (String)x.getValue());
		//}
		//for(Entry<String, String> x : mapProperties.entrySet()) {
		//    System.out.println(x.getKey() + " " + x.getValue());
		//}
	}
}
