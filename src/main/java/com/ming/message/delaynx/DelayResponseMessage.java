package com.ming.message.delaynx;

import com.ming.message.AbstractResponseMessage;
import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class DelayResponseMessage extends AbstractResponseMessage {

    public DelayResponseMessage(Boolean success, String reason) {
        super(success,reason);
    }

    @Override
    public int getMessageType() {
        return Message.DeleteResponseMessage;
    }
}
