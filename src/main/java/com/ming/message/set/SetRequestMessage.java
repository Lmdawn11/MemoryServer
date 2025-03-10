package com.ming.message.set;

import com.ming.message.Message;
import com.ming.message.aofLogger.AOFLoggable;
import com.ming.server.config.AOFManager;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper=true)
public class SetRequestMessage extends Message implements AOFLoggable {

    private String key;
    private String value;
    private int ttl;  // 过期时间（单位：秒）

    public SetRequestMessage(String key, String value) {
        this.key = key;
        this.value = value;
        this.ttl = -1; //永不过期
    }

    public SetRequestMessage(String key, String value, int ttl) {
        this.key = key;
        this.value = value;
        this.ttl = ttl;
    }

    @Override
    public int getMessageType() {
        return Message.SetRequestMessage;
    }

    @Override
    public void logTo(AOFManager aofManager) {
        aofManager.logCommand("set", key, value,String.valueOf(ttl));
    }
}
