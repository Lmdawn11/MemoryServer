package com.ming.message.aofLogger;

import com.ming.server.config.AOFManager;

public interface AOFLoggable {
    void logTo(AOFManager aofManager);
}
