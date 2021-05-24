package org.example.fcm.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.example.fcm.core.model.Response;

@RestController
@ControllerAdvice
public class AdviceController {
    private final Logger log = LoggerFactory.getLogger(AdviceController.class);

    private Response<String> handleException(String msg, Throwable e) {
        log.error("{}: {}", msg, e.getMessage());
        return new Response<>(null, String.format("%s: %s", msg, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Response<String> handleUnknownException(Throwable e) {
        e.printStackTrace();
        return handleException("Internal error", e);
    }
}
