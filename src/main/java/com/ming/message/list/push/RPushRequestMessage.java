package com.ming.message.list.push;

import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class RPushRequestMessage extends Message {

    private String key;
    private String[] values;

    public RPushRequestMessage(String key, String[] values) {
        this.key = key;
        this.values = values;
    }

    @Override
    public int getMessageType() {
        return RPushRequestMessage;
    }

}
