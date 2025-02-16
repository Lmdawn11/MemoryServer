package com.ming.message.get;

import com.ming.message.AbstractResponseMessage;
import com.ming.message.Message;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class GetResponseMessage extends AbstractResponseMessage {

    public GetResponseMessage(Boolean success,String reason) {
        super(success,reason);
    }

    @Override
    public int getMessageType() {
        return Message.GetResponseMessage;
    }
}
