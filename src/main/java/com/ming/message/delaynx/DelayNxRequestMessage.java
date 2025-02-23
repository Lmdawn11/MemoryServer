package com.ming.message.delaynx;

import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class DelayNxRequestMessage extends Message {
    private String key;
    private int ttl;
    private String clientId;

    public DelayNxRequestMessage(String key, int ttl, String clientId) {
        this.key = key;
        this.ttl = ttl;
        this.clientId = clientId;
    }

    @Override
    public int getMessageType() {
        return Message.DeleteRequestMessage;
    }
}
