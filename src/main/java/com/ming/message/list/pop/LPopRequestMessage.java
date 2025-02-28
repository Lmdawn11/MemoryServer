package com.ming.message.list.pop;

import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
public class LPopRequestMessage extends Message {
    private String key;

    public LPopRequestMessage(String key) {
        this.key = key;
    }

    @Override
    public int getMessageType() {
        return LPopRequestMessage;
    }
}

