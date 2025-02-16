package com.ming.message.get;

import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class GetRequestMessage extends Message {
    private String key;

    public GetRequestMessage(String key) {
        this.key = key;
    }

    @Override
    public int getMessageType() {
        return Message.GetRequestMessage;
    }
}
