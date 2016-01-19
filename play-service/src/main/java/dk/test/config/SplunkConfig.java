package dk.test.config;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricRegistry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dk.test.metrics.SplunkMetricsReporter;

@Configuration
public class SplunkConfig {

    @Value("${splunk.eventCollectorEndpoint}")
    private String eventCollectorEndpoint;

    @Value("${splunk.token}")
    private String token;

    @Bean
    @ConditionalOnProperty(value = "splunk.metrics", matchIfMissing = false)
    public SplunkMetricsReporter splunkReporter(MetricRegistry metricRegistry) {
        //@formatter:off
        SplunkMetricsReporter reporter = SplunkMetricsReporter
            .forRegistry(metricRegistry)
            .withEventCollectorEndpoint(this.eventCollectorEndpoint)
            .withToken(this.token)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();
        reporter.start(1, TimeUnit.MINUTES);
        //@formatter:on
        return reporter;
    }
}
