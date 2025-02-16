package com.ming.message.del;

import com.ming.message.AbstractResponseMessage;
import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class DelResponseMessage extends AbstractResponseMessage {

    public DelResponseMessage(Boolean success, String reason) {
        super(success,reason);
    }

    @Override
    public int getMessageType() {
        return Message.DeleteResponseMessage;
    }
}
