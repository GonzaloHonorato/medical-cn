package com.medicalapp.medicalapp.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.EventoClinicoResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class EventosClinicosWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    public EventosClinicosWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void broadcast(EventoClinicoResponse evento) {
        try {
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(evento));

            for (WebSocketSession session : sessions) {
                sendIfOpen(session, message);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo serializar el evento clinico.", exception);
        }
    }

    private void sendIfOpen(WebSocketSession session, TextMessage message) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(message);
        } else {
            sessions.remove(session);
        }
    }
}
