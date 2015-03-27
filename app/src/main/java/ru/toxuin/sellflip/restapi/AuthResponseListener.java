package ru.toxuin.sellflip.restapi;

public interface AuthResponseListener {
    public void onAuthSuccess();

    public void onAuthFailure();
}
