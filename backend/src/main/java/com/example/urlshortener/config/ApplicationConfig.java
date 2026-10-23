package com.example.urlshortener.config;
import com.example.urlshortener.application.ports.*;
import com.example.urlshortener.application.service.LinkService;
import com.example.urlshortener.infrastructure.factory.MongoRedisFactory;
import com.example.urlshortener.infrastructure.persistence.mongo.MongoLinkRepository;
import com.example.urlshortener.infrastructure.cache.redis.RedisLinkCache;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
@Configuration
public class ApplicationConfig {
 @Bean MongoLinkRepository mongoLinkRepository(MongoTemplate mongo) { return new MongoLinkRepository(mongo); }
 @Bean RedisLinkCache redisLinkCache(StringRedisTemplate redis,ObjectMapper json) { return new RedisLinkCache(redis,json); }
 @Bean LinkInfrastructureFactory infrastructureFactory(MongoLinkRepository repository,RedisLinkCache cache) { return new MongoRedisFactory(repository,cache); }
 @Bean LinkUseCases linkUseCases(LinkInfrastructureFactory factory) {
  return new LinkService(factory.createRepository(),factory.createCache());
 }
 @Bean WebMvcConfigurer cors(@Value("${app.frontend-origin}") String origin) {
  return new WebMvcConfigurer() { @Override public void addCorsMappings(CorsRegistry registry) { registry.addMapping("/api/**").allowedOrigins(origin).allowedMethods("GET","POST"); } };
 }
}