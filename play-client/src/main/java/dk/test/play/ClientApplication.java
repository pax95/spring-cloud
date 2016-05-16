package dk.test.play;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.SimpleKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@SpringBootApplication
public class ClientApplication {

    private static List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Value("${kafkaHost}")
    private String kafkaHost;

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Configuration
    @EnableWebSocket
    static class WebSocketConfiguration implements WebSocketConfigurer {


        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry.addHandler(new WsHandler(), "/tracks");
        }
    }

    static class WsHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            sessions.add(session);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            sessions.remove(sessions);
        }
    }

    @Bean
    SimpleKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        SimpleKafkaListenerContainerFactory<String, String> factory = new SimpleKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaHost);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "somegroup");

        return props;
    }

    @Configuration
    @EnableKafka
    static class KafkaConfiguration {

        @Bean
        public Listener listener(){
            return new Listener();
        }

    }

    static class Listener {

        @KafkaListener(topics = "playlist")
        public void handleKafkaMessage(String payload) throws IOException {
            for (WebSocketSession session: sessions) {
                session.sendMessage(new TextMessage(payload));
            }
        }
    }


}
