package dk.test.play;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@SpringBootApplication
@EnableBinding(Sink.class)
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

    @StreamListener(Sink.INPUT)
    public void handleKafkaMessage(String payload) throws IOException {
        for (WebSocketSession session : sessions) {
            session.sendMessage(new TextMessage(payload));
        }
    }

}
