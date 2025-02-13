package com.ming.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class LoginResponseMessage extends AbstractResponseMessage {
    public LoginResponseMessage(Boolean success,String reason) {
        super(success,reason);
    }
    @Override
    public int getMessageType() {
        return LoginResponseMessage;
    }
}
