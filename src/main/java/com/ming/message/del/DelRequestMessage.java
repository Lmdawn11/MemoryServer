package com.ming.message.del;

import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class DelRequestMessage extends Message {
    private String key;

    public DelRequestMessage(String key) {
        this.key = key;
    }

    @Override
    public int getMessageType() {
        return Message.DeleteRequestMessage;
    }
}
