package dk.test.play;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@SpringBootApplication
@EnableJms
public class ClientApplication {

    private static final Logger log = LoggerFactory.getLogger(ClientApplication.class);

    private static List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

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
            log.info("Closing sesseion {}", session);
            sessions.remove(sessions);
        }
    }

    @JmsListener(destination = "playlist")
    public void handleMessage(String payload) throws IOException {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(payload));
            }
        }
    }

}
