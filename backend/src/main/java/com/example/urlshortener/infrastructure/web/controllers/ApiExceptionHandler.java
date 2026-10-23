package com.example.urlshortener.infrastructure.web.controllers;
import com.example.urlshortener.application.service.LinkNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.Map;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(LinkNotFoundException.class) ResponseEntity<?> missing(LinkNotFoundException e) { return error(404,e.getMessage()); }
 @ExceptionHandler({IllegalArgumentException.class,HttpMessageNotReadableException.class}) ResponseEntity<?> invalid(Exception e) { return error(400,e instanceof HttpMessageNotReadableException ? "El cuerpo JSON no es valido." : e.getMessage()); }
 @ExceptionHandler(DataAccessException.class) ResponseEntity<?> unavailable(Exception e) { return error(503,"No fue posible acceder a MongoDB o Redis. Comprueba los servidores locales."); }
 @ExceptionHandler(IllegalStateException.class) ResponseEntity<?> conflict(Exception e) { return error(503,e.getMessage()); }
 private ResponseEntity<?> error(int status,String message) { return ResponseEntity.status(status).body(Map.of("message",message)); }
}