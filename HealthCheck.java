import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class HealthCheck {
    public static void main(String[] args) throws InterruptedException, IOException {
        // java -Durl=http://localhost:80/actuator/health HealthCheck.java
        String urlSysProp = System.getProperty("url", "http://localhost:80/actuator/health");
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(urlSysProp))
                .header("accept", "application/json")
                .build();
        var response = client.send(request, BodyHandlers.ofString());
        if(response.statusCode() != 200 || !response.body().contains("UP")) {
            throw new RuntimeException("Healthcheck failed");
        }
    }
}
// https://www.baeldung.com/java-httpclient-ssl
//Bypassing SSL Certificate Verification
//Properties props = System.getProperties();
//props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());
//HttpClient httpClient = HttpClient.newHttpClient();