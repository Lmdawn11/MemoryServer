package com.ming.message.rewrite;

import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class RewriteRequestMessage extends Message {
    private String command;

    public RewriteRequestMessage(String command) {
        this.command = command;
    }
    @Override
    public int getMessageType() {
        return Message.RewriteRequestMessage;
    }
}