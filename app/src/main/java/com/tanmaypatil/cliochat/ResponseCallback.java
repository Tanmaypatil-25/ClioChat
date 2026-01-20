package com.tanmaypatil.cliochat;

public interface ResponseCallback {
    void onResponse(String response);
    void onError(Throwable throwable);
}
