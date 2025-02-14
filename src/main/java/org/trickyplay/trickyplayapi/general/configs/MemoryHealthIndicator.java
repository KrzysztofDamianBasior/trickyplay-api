package org.trickyplay.trickyplayapi.general.configs;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MemoryHealthIndicator implements HealthIndicator {
    @Override
    public Health health(){
        long freeMemory = Runtime.getRuntime().freeMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        double freeMemoryPercent = ((double) freeMemory / (double) totalMemory) * 100;
        if(freeMemoryPercent > 25){
//            By default, Boot defines four different values as the health Status:
//                    -UP
//                    -DOWN
//                    -OUT_OF_SERVICE     -out of service temporarily
//                    -UNKNOWN
//            These states are declared as public static final instances instead of Java enums, so it is possible to define our own custom health states. To do that, we can use the status(name) method:
//            Health.Builder warning = Health.status("WARNING");

            return Health.up()
                    .withDetail("free_memory", freeMemory + " bytes") // in addition to reporting the status, we can atatch additional key-value details using withDetail(key, value), or by passing  a Map<String, Objectt> to withDetails(map) method
                    .withDetail("total_memory", totalMemory + " bytes")
                    .withDetail("free_memory_percent", freeMemoryPercent + " %")
                    .build();
        } else {
            return Health.down()
                    .withDetail("free_memory", freeMemory + "bytes")
                    .withDetail("total_memory", totalMemory + "bytes")
                    .withDetail("free_memory_percent", freeMemoryPercent + "%")
                    .build();
        }
    }
}

/*
By deault Boot maps the DOWN and OUT_OF_SERVICE states to throw 503 status code, up and ony other unmapped statuses will be translated to a 200 OK status code. To customize this mapping, we can set to the desired HTTP status code number the management.endpoint.health.status.http-mapping
    management.endpoint.health.status.http-mapping.down=500
    management.endpoint.health.status.http-mapping.out_of_service=503
    management.endpoint.health.status.http-mapping.warning=500
mockMvc.perform(get("/actuator/health/warning").andExpect(jsonPath("$.status").value("WARNING")).andExpect(status().isInternalServerError()))

similarly we can register a bean of type HttpCodeSatusMapper to customize the HTTP status code mapping
@Component
public class CustomStatusCodeMapper implements HttpCodeStatusMapper {
    @Override
    public int getStatusCode(Status status) {
        if(status == Status.DOWN)        retrun 500;
        if(status == Status.OUT_OF_SERVICE)        retrun 503;
        if(status == Status.UNKNOWN)        retrun 500;
        if(status.getCode().equals("WARNING")) return 500;      # custom status instance
        return 200;
    }
}
by default boot registers a simple implementation of this interface with defaults mappings which is capable of reading the mappings from the configuration files
*/
