package com.yuuna.notification;

public class MessageData {

    private Integer id, sender_id, read;
    private String name, message, datetime;

    public MessageData(Integer id, Integer sender_id, String name, String message, String datetime, Integer read) {
        this.id = id;
        this.sender_id = sender_id;
        this.name = name;
        this.message = message;
        this.datetime = datetime;
        this.read = read;
    }

    public Integer getId() {
        return id;
    }

    public Integer getSender_id() {
        return sender_id;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getDatetime() {
        return datetime;
    }

    public Integer getRead() {
        return read;
    }
}
