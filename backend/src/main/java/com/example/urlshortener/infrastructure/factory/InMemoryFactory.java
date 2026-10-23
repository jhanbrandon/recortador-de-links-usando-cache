package com.example.urlshortener.infrastructure.factory;
import com.example.urlshortener.application.ports.*;
import com.example.urlshortener.infrastructure.persistence.memory.InMemoryLinkRepository;
import com.example.urlshortener.infrastructure.cache.memory.InMemoryLinkCache;
import java.time.Clock;
/** Alternative complete product family, exercised by the unit tests. */
public class InMemoryFactory implements LinkInfrastructureFactory {
 private final LinkRepository repository=new InMemoryLinkRepository();
 private final LinkCache cache;
 public InMemoryFactory() { this(Clock.systemUTC()); }
 public InMemoryFactory(Clock clock) { cache=new InMemoryLinkCache(clock); }
 public LinkRepository createRepository() { return repository; }
 public LinkCache createCache() { return cache; }
}