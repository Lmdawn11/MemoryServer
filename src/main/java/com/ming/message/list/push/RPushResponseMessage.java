package com.ming.message.list.push;

import com.ming.message.AbstractResponseMessage;

public class RPushResponseMessage extends AbstractResponseMessage {
    @Override
    public int getMessageType() {
        return RPushResponseMessage;
    }
    public RPushResponseMessage(Boolean success, String reason) {
        super(success,reason);
    }
}
