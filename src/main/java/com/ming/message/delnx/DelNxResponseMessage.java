package com.ming.message.delnx;

import com.ming.message.AbstractResponseMessage;
import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class DelNxResponseMessage extends AbstractResponseMessage {

    public DelNxResponseMessage(Boolean success, String reason) {
        super(success,reason);
    }

    @Override
    public int getMessageType() {
        return Message.DeleteResponseMessage;
    }
}
