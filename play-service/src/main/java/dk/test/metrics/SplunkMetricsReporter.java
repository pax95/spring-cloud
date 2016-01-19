package dk.test.metrics;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class SplunkMetricsReporter extends ScheduledReporter {
    private RestTemplate restTemplate;
    private String eventCollectorEndpoint;
    private ObjectMapper mapper;
    private HttpHeaders headers;
    private String source;

    protected SplunkMetricsReporter(Builder builder) {
        super(builder.registry, builder.prefix, builder.filter, builder.rateUnit, builder.durationUnit);
        this.source = builder.source;
        this.eventCollectorEndpoint = builder.eventCollectorEndpoing;
        // create strategy that accepts all certificates
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] arg0, String arg1) -> true;

        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSSLContext(sslContext).build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            restTemplate = new RestTemplate(requestFactory);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
        }
        mapper = new ObjectMapper().registerModule(new MetricsModule(builder.rateUnit, builder.durationUnit, false));
        headers = new HttpHeaders();
        headers.add("Authorization", "Splunk " + builder.token);
    }

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    @Override
    public void report(@SuppressWarnings("rawtypes") SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        if (gauges.size() > 0 || counters.size() > 0 || histograms.size() > 0 || meters.size() > 0 || timers.size() > 0) {
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            result.addAll(rearrangeMetrics(gauges, "gauges"));
            result.addAll(rearrangeMetrics(counters, "counters"));
            result.addAll(rearrangeMetrics(histograms, "histograms"));
            result.addAll(rearrangeMetrics(meters, "meters"));
            result.addAll(rearrangeMetrics(timers, "timers"));
            result.forEach(e -> {
                try {
                    Map<String, Object> event = new HashMap<String, Object>();
                    if (this.source != null) {
                        event.put("source", source);
                    }
                    event.put("time", LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond());
                    event.put("event", e);
                    HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(event), headers);
                    restTemplate.postForLocation(this.eventCollectorEndpoint, entity);
                } catch (JsonProcessingException e1) {
                    throw new RuntimeException("Unable to write json", e1);
                }
            });
        }
    }

    private List<Map<String, Object>> rearrangeMetrics(Map<String, ? extends Object> metrics, String label) {
        List<Map<String, Object>> retval = new ArrayList<Map<String, Object>>(metrics.size());
        metrics.entrySet().forEach(s -> {
            Map<String, Object> metric = new HashMap<String, Object>();
            metric.put("name", s.getKey());
            metric.put("val", s.getValue());
            metric.put("type", label);
            retval.add(metric);
        });
        return retval;
    }

    public static class Builder {
        private final MetricRegistry registry;
        private String prefix = null;
        private String eventCollectorEndpoing;
        private String token;
        private TimeUnit rateUnit = TimeUnit.SECONDS;
        private TimeUnit durationUnit = TimeUnit.MILLISECONDS;
        private MetricFilter filter = MetricFilter.ALL;
        public String source = "metrics";
        private Map<String, Object> extraAttributes = new HashMap<String, Object>();

        private Builder(MetricRegistry registry) {
            this.registry = registry;
        }

        public Builder prefixedWith(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder withEventCollectorEndpoint(String endpoint) {
            this.eventCollectorEndpoing = endpoint;
            return this;
        }

        public Builder withToken(String token) {
            this.token = token;
            return this;
        }

        public Builder withSource(String source) {
            this.source = source;
            return this;
        }

        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder addAttribute(String name, Object value) {
            extraAttributes.put(name, value);
            return this;
        }

        public SplunkMetricsReporter build() {
            return new SplunkMetricsReporter(this);
        }

    }
}
