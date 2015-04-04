package ru.toxuin.sellflip.entities;

import com.google.gson.annotations.SerializedName;

public class SendFeedBackForm {
    private String email;
    private String subject;
    private String message;

    @SerializedName("api_key")
    private String apiKey;

    public SendFeedBackForm(String email, String subject, String message) {
        this.email = email;
        this.subject = subject;
        this.message = message;
        apiKey = "29820d9ba92f88c24375d299dcb946b5be82b953";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
