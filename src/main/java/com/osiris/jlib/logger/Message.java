/*
 * Copyright Osiris Team
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * AutoPlug License.  Please consult the file "LICENSE" for details.
 */

package com.osiris.jlib.logger;

import java.time.LocalDateTime;

public class Message {
    private LocalDateTime date;
    private String label = AL.NAME;
    private Class originClass;
    private Type type;
    private Exception exception;
    private String exMessage;
    private String content;

    public Message(Type type, String content) {
        this(LocalDateTime.now(), type, content);
    }

    /**
     * Contains all the essential information
     * to inform the user about
     * a current action/state/progress.
     * This can be used by different output sources
     * like the console, log file and web-socket.
     * Note: when details is null/empty the exception message will be used instead.
     *
     * @param date
     * @param type
     * @param content
     */
    public Message(LocalDateTime date, Type type, String content) {
        this(date, null, type, content);
    }

    public Message(LocalDateTime date, String label, Type type, String content) {
        this(date, label, null, type, content, null);
    }


    public Message(LocalDateTime date, String label, Class originClass, Type type, String content, Exception e) {
        this.date = date;
        if (label != null) this.label = label;
        this.originClass = originClass;
        this.type = type;
        this.exception = e;
        this.content = content;
        if (this.exception != null) this.exMessage = exception.getMessage();
        // Example message: [2.12.2020][MyApplication][Main.class][INFO] Some information...
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getExMessage() {
        if (exception != null) exMessage = exception.getMessage();
        return exMessage;
    }

    public void setExMessage(String exMessage) {
        this.exMessage = exMessage;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Class getOriginClass() {
        return originClass;
    }

    public void setOriginClass(Class originClass) {
        this.originClass = originClass;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public enum Type {
        INFO, DEBUG, WARN, ERROR
    }

}
