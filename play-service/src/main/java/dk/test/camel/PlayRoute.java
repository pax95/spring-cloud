package dk.test.camel;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.component.rabbitmq.RabbitMQConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.processor.idempotent.MemoryIdempotentRepository;
import org.apache.camel.spring.boot.FatJarRouter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@EnableAutoConfiguration
public class PlayRoute extends FatJarRouter {
    private final static String RABBIT_EXCHANGE_NAME = "/track-exchange";
    private final static List<String> CHANNELS = Arrays.asList("P3", "AR4", "P8J", "DRM", "P6B", "P7M", "RAM", "P5D", "KH4");
    @Value("${lastFmApiKey}")
    private String lastFmApiKey;

    @Value("${rabbitMQHost}")
    private String rabbitMQHost;

    @Override
    public void configure() throws Exception {
        //@formatter:off
        from("timer://pollingTimer?fixedRate=true&period=3000")
            .split().method(PlayRoute.class, "getChannels").parallelProcessing()
                .setHeader(Exchange.HTTP_METHOD, simple("GET"))
                .setHeader(Exchange.HTTP_URI, simple("http://www.dr.dk/playlister/feeds/nowNext/nowPrev.drxml?items=0&cid=${body}"))
                .to("http4://dummy").filter()
                .simple("${body} != null")
                .unmarshal()
                .json(JsonLibrary.Gson)
                .filter(simple("${body[now][status]} == 'music'"))
                    .convertBodyTo(Track.class)
                    .setHeader("artist", simple("${body.artist}"))
                    .setHeader("title", simple("${body.title}"))
                    .setHeader("channel", simple("${body.channel}"))
                    .idempotentConsumer(simple("${body.time}-${body.channel}"), MemoryIdempotentRepository.memoryIdempotentRepository(300))
                    .to("direct:process");
        
        from("direct:process")
            .enrich("direct:lastfmEnricher", new LastFmAggregationStrategy())
            .marshal()
            .json(JsonLibrary.Gson, Track.class)
            .setHeader(RabbitMQConstants.ROUTING_KEY, simple("tracks.${in.header.channel}"))
            .to("rabbitmq://" + rabbitMQHost + RABBIT_EXCHANGE_NAME + "?exchangeType=topic&autoDelete=false");
        
        from("direct:lastfmEnricher")
            .onException(Exception.class)
                .to("log:dk.test?showAll=true&multiline=true")
                .handled(true)
            .end()
           .removeHeader(Exchange.HTTP_URI)
           .setProperty(Exchange.CHARSET_NAME, constant("UTF-8"))
           .setHeader(Exchange.HTTP_QUERY, simple("method=track.getInfo&api_key=" + lastFmApiKey + "&artist=${header.artist}&track=${header.title}"))
           .setBody().simple("")
           .to("http4://ws.audioscrobbler.com/2.0/?throwExceptionOnFailure=false");
        

        from("rabbitmq://" + rabbitMQHost + RABBIT_EXCHANGE_NAME + "?exchangeType=topic&routingKey=tracks.*&durable=true&queue=tracks&autoDelete=false")
            .log("got track :${body}");
        //@formatter:on
    }

    public List<String> getChannels() {
        return CHANNELS;
    }
}
