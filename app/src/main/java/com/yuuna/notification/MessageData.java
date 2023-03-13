package com.yuuna.notification;

public class MessageData {

    private Integer id, read;
    private String name, message, datetime;

    public MessageData(Integer id, String name, String message, String datetime, Integer read) {
        this.id = id;
        this.name = name;
        this.message = message;
        this.datetime = datetime;
        this.read = read;
    }

    public Integer getId() {
        return id;
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
