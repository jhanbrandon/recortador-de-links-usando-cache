package com.example.urlshortener;
import com.example.urlshortener.application.ports.*;
import com.example.urlshortener.application.service.*;
import com.example.urlshortener.domain.model.Link;
import com.example.urlshortener.infrastructure.factory.MongoRedisFactory;
import com.example.urlshortener.infrastructure.persistence.mongo.*;
import com.example.urlshortener.infrastructure.cache.redis.RedisLinkCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClients;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;
import java.time.*;
import java.util.*;
/** Opt-in: real localhost servers. Isolated database/key, never deletes production links. */
@EnabledIfSystemProperty(named="localIntegration",matches="true")
class LocalInfrastructureTest {
 @Test void realMongoRedisFamilyPersistenceAndThirtySecondExpiration() throws Exception {
  String suffix=UUID.randomUUID().toString().replace("-","");
  String database="urlshortener_test_"+suffix;
  try(var client=MongoClients.create("mongodb://localhost:27017/?serverSelectionTimeoutMS=3000")) {
   MongoTemplate mongo=new MongoTemplate(client,database);
   LettuceConnectionFactory connection=new LettuceConnectionFactory("localhost",6379);connection.afterPropertiesSet();connection.start();
   StringRedisTemplate redis=new StringRedisTemplate(connection);
   RedisLinkCache cache=new RedisLinkCache(redis,new ObjectMapper().findAndRegisterModules(),"links:test:"+suffix);
   try {
    LinkInfrastructureFactory factory=new MongoRedisFactory(new MongoLinkRepository(mongo),cache);
    assertInstanceOf(MongoLinkRepository.class,factory.createRepository());assertInstanceOf(RedisLinkCache.class,factory.createCache());
    assertSame(factory.createRepository(),factory.createRepository());
    LinkService service=new LinkService(factory.createRepository(),factory.createCache());
    Link link=service.create("https://example.com/permanent");
    assertEquals("mongo",service.list().source());assertEquals(30,service.cacheTtl());
    LinkService restarted=new LinkService(new MongoLinkRepository(new MongoTemplate(client,database)),cache);
    assertEquals(link,restarted.resolve(link.shortCode()));
    assertThrows(DuplicateCodeException.class,()->factory.createRepository().save(new Link(UUID.randomUUID().toString(),"https://example.com/other",link.shortCode(),Instant.now())));
    Thread.sleep(2100);
    var hit=service.list();assertEquals("redis",hit.source());assertTrue(hit.ttlSeconds()<=28 && hit.ttlSeconds()>0);
    // Wait out the actual original 30-second Redis TTL; no simulated deletion.
    Thread.sleep(28500);
    assertEquals(0,service.cacheTtl());assertTrue(cache.get().isEmpty());
    assertEquals(link,restarted.resolve(link.shortCode()));assertEquals(1,mongo.getCollection("links").countDocuments());
    assertEquals("mongo",restarted.list().source());assertEquals(30,restarted.cacheTtl());
    service.create("https://example.com/new");assertEquals(0,service.cacheTtl());assertEquals(2,service.list().links().size());
   } finally { cache.invalidate();mongo.getDb().drop();connection.destroy(); }
  }
 }
}