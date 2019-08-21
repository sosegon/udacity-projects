package edu.udacity.java.nano.chat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Server
 *
 * @see ServerEndpoint WebSocket Client
 * @see Session   WebSocket Session
 */

@Component
@ServerEndpoint("/chat")
public class WebSocketChatServer {

    /**
     * All chat sessions.
     */
    private static Map<String, Session> onlineSessions = new ConcurrentHashMap<>();

    private static void sendMessageToAll(Message msg) {
        msg.setOnlineCount(onlineSessions.size());

        onlineSessions.values().forEach(session -> {
            session.getAsyncRemote().sendText(JSON.toJSONString(msg));
        });
    }

    /**
     * Open connection, 1) add session, 2) add user.
     */
    @OnOpen
    public void onOpen(Session session) {
        if(session.isOpen()) {
            String sessionId = session.getId();

            onlineSessions.put(sessionId, session);

            Message message = new Message();
            message.setType(Message.MessageType.JOIN);

            sendMessageToAll(message);
        }
    }

    /**
     * Send message, 1) get username and session, 2) send message to all.
     */
    @OnMessage
    public void onMessage(Session session, String jsonStr) {
        JSONObject jsonObject = JSON.parseObject(jsonStr);

        if(!jsonObject.containsKey("username")){
            System.out.println("No username");
            return;
        }

        if(!jsonObject.containsKey("msg")){
            System.out.println("No msg");
            return;
        }

        Message message = new Message();
        message.setType(Message.MessageType.SPEAK);
        message.setUsername(jsonObject.getString("username"));
        message.setMsg(jsonObject.getString("msg"));

        sendMessageToAll(message);
    }

    /**
     * Close connection, 1) remove session, 2) update user.
     */
    @OnClose
    public void onClose(Session session) {
        String sessionId = session.getId();

        onlineSessions.remove(sessionId);

        Message message = new Message();
        message.setType(Message.MessageType.LEAVE);

        sendMessageToAll(message);
    }

    /**
     * Print exception.
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

}
