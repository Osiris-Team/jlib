package com.osiris.autoplug.core.events;

public interface MessageEvent<Message> {
    void executeOnEvent(Message m);
}
