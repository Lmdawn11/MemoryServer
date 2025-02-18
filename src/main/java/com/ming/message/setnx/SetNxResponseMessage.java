package com.ming.message.setnx;

import com.ming.message.AbstractResponseMessage;
import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class SetNxResponseMessage extends AbstractResponseMessage {

    public SetNxResponseMessage(Boolean success, String reason) {
        super(success,reason);
    }


    @Override
    public int getMessageType() {
        return Message.SetResponseMessage;
    }
}
