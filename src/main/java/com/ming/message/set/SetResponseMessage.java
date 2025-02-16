package com.ming.message.set;

import com.ming.message.AbstractResponseMessage;
import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class SetResponseMessage extends AbstractResponseMessage {

    public SetResponseMessage(Boolean success,String reason) {
        super(success,reason);
    }


    @Override
    public int getMessageType() {
        return Message.SetResponseMessage;
    }
}
