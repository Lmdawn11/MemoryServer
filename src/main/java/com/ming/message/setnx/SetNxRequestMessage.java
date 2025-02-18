package com.ming.message.setnx;

import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class SetNxRequestMessage extends Message {

    private String key;
    private String value;
    private int ttl;  // 过期时间（单位：秒）

    public SetNxRequestMessage(String key, String value) {
        this.key = key;
        this.value = value;
        this.ttl = -1; //永不过期
    }

    public SetNxRequestMessage(String key, String value, int ttl) {
        this.key = key;
        this.value = value;
        this.ttl = ttl;
    }

    @Override
    public int getMessageType() {
        return Message.SetRequestMessage;
    }
}
