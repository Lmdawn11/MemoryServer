package com.ming.message.delnx;

import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class DelNxRequestMessage extends Message {
    private String key;
    private String clientId;

    public DelNxRequestMessage(String key, String clientId) {
        this.key = key;
        this.clientId = clientId;
    }

    @Override
    public int getMessageType() {
        return Message.DeleteRequestMessage;
    }
}
