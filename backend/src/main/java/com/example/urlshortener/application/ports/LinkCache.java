package com.example.urlshortener.application.ports;
import com.example.urlshortener.domain.model.Link;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
public interface LinkCache {
 record Snapshot(List<Link> links, long ttlSeconds) {}
 Optional<Snapshot> get();
 void put(List<Link> links, Duration ttl);
 long remainingSeconds();
 void invalidate();
}