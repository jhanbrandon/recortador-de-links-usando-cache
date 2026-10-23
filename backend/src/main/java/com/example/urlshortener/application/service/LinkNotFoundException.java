package com.example.urlshortener.application.service;
public class LinkNotFoundException extends RuntimeException {
 public LinkNotFoundException() { super("El enlace solicitado no existe."); }
}