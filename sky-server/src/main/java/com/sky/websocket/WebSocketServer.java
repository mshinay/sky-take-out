package com.sky.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {

    private static final Set<WebSocketServer> WEB_SOCKET_SET = new CopyOnWriteArraySet<>();
    private static final ConcurrentHashMap<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    private Session session;
    private String sid;

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        this.sid = sid;
        WEB_SOCKET_SET.add(this);
        SESSION_POOL.put(sid, session);
        log.info("WebSocket连接建立，sid={}, 当前在线={}", sid, WEB_SOCKET_SET.size());
    }

    @OnClose
    public void onClose() {
        WEB_SOCKET_SET.remove(this);
        SESSION_POOL.remove(this.sid);
        log.info("WebSocket连接关闭，sid={}, 当前在线={}", sid, WEB_SOCKET_SET.size());
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("收到WebSocket消息，sid={}, message={}", sid, message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误，sid={}", sid, error);
    }

    public static void sendToAllClient(String message) {
        log.info("WebSocket广播消息，在线连接数={}, message={}", WEB_SOCKET_SET.size(), message);
        for (WebSocketServer webSocketServer : WEB_SOCKET_SET) {
            try {
                webSocketServer.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("WebSocket广播失败，sid={}", webSocketServer.sid, e);
            }
        }
    }
}
