package ru.demoexam.backend.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.demoexam.backend.service.ProductService;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(ProductService.ProductDeleteException.class)
    ResponseEntity<Map<String, String>> handleProductDelete(ProductService.ProductDeleteException exception) {
        HttpStatus status = switch (exception.getReason()) {
            case LINKED_TO_ORDER -> HttpStatus.CONFLICT;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
        };
        return ResponseEntity.status(status).body(Map.of("reason", exception.getReason().name()));
    }
}
