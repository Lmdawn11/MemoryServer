package com.ming.message.rewrite;

import com.ming.message.Message;
import lombok.Data;

@Data
public class RewriteResponseMessage extends Message {
    private final boolean success;
    private final String message;

    public RewriteResponseMessage(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    @Override
    public int getMessageType() {
        return Message.RewriteResponseMessage;
    }
}