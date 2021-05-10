package com.osiris.autoplug.core.events;

import com.osiris.autoplug.core.logger.Message;

public interface MessageEvent<Message> {
    void executeOnEvent(Message m);
}
