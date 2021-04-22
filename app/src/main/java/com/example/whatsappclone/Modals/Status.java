package com.example.whatsappclone.Modals;

public class Status {

    private String ImageUrl,uid;
    private long timeStamp;


    public Status(String imageUrl, long timeStamp, String uid) {
        ImageUrl = imageUrl;
        this.timeStamp = timeStamp;
        this.uid = uid;
    }
    public Status() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
