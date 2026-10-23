package com.example.urlshortener.application.ports;
import com.example.urlshortener.domain.model.Link;
import java.util.List;
public interface LinkUseCases {
 record Listing(List<Link> links, String source, long ttlSeconds) {}
 Link create(String originalUrl);
 Listing list();
 Link resolve(String shortCode);
 long cacheTtl();
}