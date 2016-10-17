package com.bridgelabz.chat.model;

import com.google.firebase.database.Exclude;

import java.util.Date;

/**
 * Created by bridgeit on 29/8/16.
 */

public class Message {

    private String msg;
    private String date;
    private String userId;
    private long timeStump;
    private String imageUrl;

    @Exclude
    String actualImageUrl;

    @Exclude
    boolean sync=false;

    public Message() {
    }

    public Message(String msg, String userId) {
        this.msg = msg;
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getActualImageUrl() {
        return actualImageUrl;
    }

    public void setActualImageUrl(String actualImageUrl) {
        this.actualImageUrl = actualImageUrl;
    }

    @Exclude
    public boolean isSync() {
        return sync;
    }

    @Exclude
    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public long getTimeStump() {
        return timeStump;
    }

    public void setTimeStump(long timeStump) {
        this.timeStump = timeStump;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (timeStump != message.timeStump) return false;
        if (msg != null ? !msg.equals(message.msg) : message.msg != null) return false;
        if (date != null ? !date.equals(message.date) : message.date != null) return false;
        return userId != null ? userId.equals(message.userId) : message.userId == null;

    }

    @Override
    public int hashCode() {
        int result = msg != null ? msg.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (int) (timeStump ^ (timeStump >>> 32));
        return result;
    }
}
