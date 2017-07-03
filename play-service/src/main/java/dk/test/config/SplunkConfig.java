package dk.test.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.codahale.metrics.MetricRegistry;

import dk.test.metrics.SplunkMetricsReporter;

@ConfigurationProperties(prefix = "splunk")
public class SplunkConfig {

    @Value("${eventCollectorEndpoint}")
    private String eventCollectorEndpoint;

    @Value("${token}")
    private String token;

    @Bean
    @ConditionalOnProperty(value = "splunk.metrics", matchIfMissing = false)
    public SplunkMetricsReporter splunkReporter(MetricRegistry metricRegistry) {
        // @formatter:off
        SplunkMetricsReporter reporter = SplunkMetricsReporter
            .forRegistry(metricRegistry)
            .withEventCollectorEndpoint(this.eventCollectorEndpoint)
            .withToken(this.token)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();
        reporter.start(1, TimeUnit.MINUTES);
        // @formatter:on
        return reporter;
    }

    public String getEventCollectorEndpoint() {
        return eventCollectorEndpoint;
    }

    public void setEventCollectorEndpoint(String eventCollectorEndpoint) {
        this.eventCollectorEndpoint = eventCollectorEndpoint;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
