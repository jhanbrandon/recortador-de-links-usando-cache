package com.example.urlshortener.domain.model;
import java.time.Instant;
/** Immutable domain entity. No persistence or framework annotations. */
public record Link(String id, String originalUrl, String shortCode, Instant createdAt) {}