package com.ming.message.del;

import com.ming.message.Message;
import com.ming.message.aofLogger.AOFLoggable;
import com.ming.server.config.AOFManager;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class DelRequestMessage extends Message implements AOFLoggable {
    private String key;


    public DelRequestMessage(String key) {
        this.key = key;
    }

    @Override
    public int getMessageType() {
        return Message.DeleteRequestMessage;
    }

    @Override
    public void logTo(AOFManager aofManager) {
        aofManager.logCommand("del", key);
    }
}
