package com.ming.message.list.pop;

import com.ming.message.AbstractResponseMessage;
import lombok.Data;

@Data
public class LPopResponseMessage extends AbstractResponseMessage {
    @Override
    public int getMessageType() {
        return LPopResponseMessage;
    }

    public LPopResponseMessage(Boolean success,String reason) {
        super(success,reason);
    }
}
