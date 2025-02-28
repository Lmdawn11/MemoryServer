package com.ming.message.list.pop;

import com.ming.message.Message;
import lombok.Data;

@Data
public class RPopRequestMessage extends Message {
    private String key;
    @Override
    public int getMessageType() {
        return RPopRequestMessage;
    }

    public RPopRequestMessage(String key) {
        this.key = key;
    }
}
