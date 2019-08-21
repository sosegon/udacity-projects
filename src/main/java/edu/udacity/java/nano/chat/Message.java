package edu.udacity.java.nano.chat;

/**
 * WebSocket message model
 */
public class Message {

    private String username;
    private String msg;
    private String type;
    private int onlineCount;

    public interface MessageType {
        String SPEAK = "SPEAK";
        String JOIN = "JOIN";
        String LEAVE = "LEAVE";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        this.onlineCount = onlineCount;
    }
}
