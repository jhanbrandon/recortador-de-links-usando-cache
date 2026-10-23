package com.example.urlshortener.infrastructure.factory;
import com.example.urlshortener.application.ports.*;
import com.example.urlshortener.infrastructure.persistence.mongo.MongoLinkRepository;
import com.example.urlshortener.infrastructure.cache.redis.RedisLinkCache;
/** Concrete factory: reuses the managed products; never opens connections here. */
public class MongoRedisFactory implements LinkInfrastructureFactory {
 private final MongoLinkRepository repository;
 private final RedisLinkCache cache;
 public MongoRedisFactory(MongoLinkRepository repository,RedisLinkCache cache) { this.repository=repository; this.cache=cache; }
 public LinkRepository createRepository() { return repository; }
 public LinkCache createCache() { return cache; }
}