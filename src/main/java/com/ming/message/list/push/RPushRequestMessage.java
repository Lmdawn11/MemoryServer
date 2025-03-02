package com.ming.message.list.push;

import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class RPushRequestMessage extends Message {

    private int ttl;
    private String key;
    private String[] values;

    public RPushRequestMessage(String key, String[] values, int ttl) {
        this.key = key;
        this.values = values;
        this.ttl = ttl;
    }

    public RPushRequestMessage(String key, String[] values) {
        this.key = key;
        this.values = values;
        this.ttl = -1;
    }

    @Override
    public int getMessageType() {
        return RPushRequestMessage;
    }

}
