package dk.test;

import com.codahale.metrics.MetricRegistry;

import org.apache.camel.RoutesBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Application {
    @Autowired
    private ApplicationContext applicationContext;

    private RoutesBuilder[] routesBuilders;

    @Autowired
    private MetricRegistry metricRegistry;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
