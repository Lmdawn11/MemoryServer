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

    public static final int RewriteRequestMessage = 101;
    public static final int RewriteResponseMessage = 102;
    public static final int SetNxRequestMessage = 103;
    public static final int SetNxResponseMessage = 104;
    public static final int DelNxRequestMessage = 105;
    public static final int DelNxResponseMessage = 106;
    public static final int DelayNxRequestMessage = 107;
    public static final int DelayNxResponseMessage = 108;
    public static final int LoginRequestMessage = 0;
    public static final int LoginResponseMessage = 1;
    public static final int SetRequestMessage = 2;
    public static final int SetResponseMessage = 3;
    public static final int GetRequestMessage = 4;
    public static final int GetResponseMessage = 5;
    public static final int DeleteRequestMessage = 6;
    public static final int DeleteResponseMessage = 7;

    private static final Map<Integer, Class<?>> messageClasses = new HashMap<>();

    static {
        messageClasses.put(LoginRequestMessage, LoginRequestMessage.class);
        messageClasses.put(LoginResponseMessage, LoginResponseMessage.class);
        messageClasses.put(SetRequestMessage, com.ming.message.set.SetRequestMessage.class);
        messageClasses.put(SetResponseMessage, com.ming.message.set.SetResponseMessage.class);
        messageClasses.put(GetRequestMessage, com.ming.message.get.GetRequestMessage.class);
        messageClasses.put(GetResponseMessage, com.ming.message.get.GetResponseMessage.class);
        messageClasses.put(DeleteRequestMessage,com.ming.message.del.DelRequestMessage.class);
        messageClasses.put(DeleteResponseMessage,com.ming.message.del.DelResponseMessage.class);
        messageClasses.put(SetNxRequestMessage,com.ming.message.setnx.SetNxRequestMessage.class);
        messageClasses.put(SetNxResponseMessage,com.ming.message.setnx.SetNxResponseMessage.class);
    }
}
