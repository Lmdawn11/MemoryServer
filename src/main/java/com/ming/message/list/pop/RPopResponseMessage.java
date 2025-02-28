package com.ming.message.list.pop;

import com.ming.message.AbstractResponseMessage;

public class RPopResponseMessage extends AbstractResponseMessage {
    @Override
    public int getMessageType() {
        return RPopResponseMessage;
    }

    public RPopResponseMessage(boolean success, String reason) {
        super(success, reason);
    }
}
