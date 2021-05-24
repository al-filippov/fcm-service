package org.example.fcm.core.model;

class ControllerResponse<T> {
    private T response;
    private String error;

    ControllerResponse(T response, String error) {
        this.response = response;
        this.error = error == null ? "" : error;
    }

    public T getResponse() {
        return response;
    }

    public String getError() {
        return error;
    }
}
