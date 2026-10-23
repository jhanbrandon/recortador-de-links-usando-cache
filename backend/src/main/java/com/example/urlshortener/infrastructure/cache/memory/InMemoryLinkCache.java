package com.example.urlshortener.infrastructure.cache.memory;
import com.example.urlshortener.application.ports.LinkCache;
import com.example.urlshortener.domain.model.Link;
import java.time.*;
import java.util.*;
public class InMemoryLinkCache implements LinkCache {
 private final Clock clock;
 private List<Link> links;
 private Instant expires=Instant.MIN;
 public InMemoryLinkCache(Clock clock) { this.clock=clock; }
 public synchronized Optional<Snapshot> get() { long ttl=remainingSeconds(); return ttl>0 && links!=null ? Optional.of(new Snapshot(links,ttl)) : Optional.empty(); }
 public synchronized void put(List<Link> links,Duration ttl) { this.links=List.copyOf(links); expires=clock.instant().plus(ttl); }
 public synchronized long remainingSeconds() { if(links==null || !expires.isAfter(clock.instant())) return 0; long millis=Duration.between(clock.instant(),expires).toMillis(); return millis<=0 ? 0 : (millis+999)/1000; }
 public synchronized void invalidate() { links=null; expires=Instant.MIN; }
}