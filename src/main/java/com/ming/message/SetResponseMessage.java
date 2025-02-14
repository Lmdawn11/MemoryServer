package com.ming.message;

public class SetResponseMessage extends AbstractResponseMessage{

    public SetResponseMessage(Boolean success,String reason) {
        super(success,reason);
    }

    @Override
    public int getMessageType() {
        return SetResponseMessage;
    }
}
