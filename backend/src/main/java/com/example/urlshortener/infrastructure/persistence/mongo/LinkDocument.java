package com.example.urlshortener.infrastructure.persistence.mongo;
import com.example.urlshortener.domain.model.Link;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.Instant;
@Document("links")
public record LinkDocument(@Id String id, String originalUrl, @Indexed(unique=true) String shortCode, Instant createdAt) {
 public static LinkDocument from(Link link) { return new LinkDocument(link.id(),link.originalUrl(),link.shortCode(),link.createdAt()); }
 public Link toDomain() { return new Link(id,originalUrl,shortCode,createdAt); }
}