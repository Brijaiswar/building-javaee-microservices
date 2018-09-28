package cloud.nativ.javaee.weather;

import cloud.nativ.javaee.CentralConfiguration;
import lombok.extern.java.Log;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonPointer;
import javax.json.JsonString;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;

/**
 * Simple REST repository implementation.
 */
@Log
@ApplicationScoped
public class OpenWeatherMapRepository {

    @Inject
    private CentralConfiguration configuration;

    private OpenWeatherMap openWeatherMap;

    @PostConstruct
    void initialize() {
        try {
            openWeatherMap = RestClientBuilder.newBuilder()
                    .baseUri(new URI("https://api.openweathermap.org"))
                    .build(OpenWeatherMap.class);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Timeout(value = 5L, unit = ChronoUnit.SECONDS)
    @Retry(delay = 500L, maxRetries = 1)
    @Fallback(fallbackMethod = "defaultWeather")
    public String getWeather(String city) {
        JsonObject response = openWeatherMap.getWeather(city, configuration.getWeatherAppId());
        LOGGER.log(Level.INFO, "Received {0}", response);

        JsonPointer pointer = Json.createPointer("/weather/0/main");
        return ((JsonString) pointer.getValue(response)).getString();
    }

    public String defaultWeather(String city) {
        return "Unknown";
    }
}
