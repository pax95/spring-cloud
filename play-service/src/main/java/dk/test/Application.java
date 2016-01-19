package dk.test;

import com.codahale.metrics.MetricRegistry;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.metrics.routepolicy.MetricsRoutePolicyFactory;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RoutesBuilder[] routesBuilders;

    @Autowired
    private MetricRegistry metricRegistry;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CamelContext camelContext() throws Exception {
        CamelContext camelContext = new SpringCamelContext(applicationContext);
        if (routesBuilders != null) {
            for (RoutesBuilder routesBuilder : routesBuilders) {
                camelContext.addRoutes(routesBuilder);
            }
        }
        MetricsRoutePolicyFactory fac = new MetricsRoutePolicyFactory();
        fac.setMetricsRegistry(metricRegistry);
        camelContext.addRoutePolicyFactory(fac);
        return camelContext;
    }

}
