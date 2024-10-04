package org.springdemo.batchweb.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WebAdvisor {

    @ExceptionHandler
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.internalServerError().body(ex.getMessage());
    }
}
