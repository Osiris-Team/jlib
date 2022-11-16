package com.osiris.jlib.events;

public interface MessageEvent<Message> {
    void executeOnEvent(Message m);
}
