package com.ming.message.delaynx;

import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class DelayRequestMessage extends Message {
    private String key;
    private int ttl;

    public DelayRequestMessage(String key,int ttl) {
        this.key = key;
        this.ttl = ttl;
    }

    @Override
    public int getMessageType() {
        return Message.DeleteRequestMessage;
    }
}
