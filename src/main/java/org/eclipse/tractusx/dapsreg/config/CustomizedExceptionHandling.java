package org.eclipse.tractusx.dapsreg.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.time.LocalDateTime;

@ControllerAdvice
public class CustomizedExceptionHandling extends ResponseEntityExceptionHandler {

    @Autowired
    private ObjectMapper mapper;

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<JsonNode> handleExceptions(WebClientResponseException exception, WebRequest webRequest) {
        var response = mapper.createObjectNode()
                .put("timestamp", Instant.now().toString())
                .put("status", exception.getRawStatusCode())
                .put("error", exception.getMessage());
        return new ResponseEntity<>(response, exception.getStatusCode());
    }
}
