package com.ming.message;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public abstract class Message implements Serializable {

    public static Class<?> getMessageClass(int messageType) {
        return messageClasses.get(messageType);
    }

    private int sequenceId;

    private int messageType;

    public abstract int getMessageType();

    public static final int LoginRequestMessage = 0;
    public static final int LoginResponseMessage = 1;
    public static final int SetRequestMessage = 2;
    public static final int SetResponseMessage = 3;
    public static final int GetRequestMessage = 4;
    public static final int GetResponseMessage = 5;
    private static final Map<Integer, Class<?>> messageClasses = new HashMap<>();

    static {
        messageClasses.put(LoginRequestMessage, LoginRequestMessage.class);
        messageClasses.put(LoginResponseMessage, LoginResponseMessage.class);
        messageClasses.put(SetRequestMessage, com.ming.message.set.SetRequestMessage.class);
        messageClasses.put(SetResponseMessage, com.ming.message.set.SetResponseMessage.class);
        messageClasses.put(GetRequestMessage, com.ming.message.get.GetRequestMessage.class);
        messageClasses.put(GetResponseMessage, com.ming.message.get.GetResponseMessage.class);
    }
}
