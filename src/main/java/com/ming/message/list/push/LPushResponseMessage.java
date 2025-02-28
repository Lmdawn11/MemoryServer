package com.ming.message.list.push;

import com.ming.message.AbstractResponseMessage;

public class LPushResponseMessage extends AbstractResponseMessage {
    @Override
    public int getMessageType() {
        return LPushResponseMessage;
    }
    public LPushResponseMessage(Boolean success,String reason) {
        super(success,reason);
    }
}
