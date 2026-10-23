package com.example.urlshortener.application.ports;
/** Translates an infrastructure uniqueness conflict into an application contract. */
public class DuplicateCodeException extends RuntimeException {
 public DuplicateCodeException() { super("El codigo corto ya existe"); }
}