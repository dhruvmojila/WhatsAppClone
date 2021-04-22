package com.example.whatsappclone.Modals;

public class Message {

    private String messageId;
    private String message;
    private String senderId;

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    private String imageUri;
    private String timeStamp;
    private int emotion=-1;

    public Message(String messageId, String message, String senderId, String timeStamp, int emotion) {
        this.messageId = messageId;
        this.message = message;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.emotion = emotion;
    }
    public Message(String message, String senderId, String timeStamp) {
        this.message = message;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
    }
    public Message() {

    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getEmotion() {
        return emotion;
    }

    public void setEmotion(int emotion) {
        this.emotion = emotion;
    }
}
