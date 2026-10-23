package com.example.urlshortener.infrastructure.cache.redis;
import com.example.urlshortener.application.ports.LinkCache;
import com.example.urlshortener.domain.model.Link;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.time.Duration;
import java.util.*;
/** Lua reads value and PTTL atomically, without renewing the expiration. */
public class RedisLinkCache implements LinkCache {
 private static final DefaultRedisScript<List> READ = new DefaultRedisScript<>("local v=redis.call('GET',KEYS[1]); if not v then return {} end; return {v,tostring(redis.call('PTTL',KEYS[1]))}",List.class);
 private final StringRedisTemplate redis;
 private final ObjectMapper json;
 private final String key;
 public RedisLinkCache(StringRedisTemplate redis,ObjectMapper json) { this(redis,json,"links:all"); }
 public RedisLinkCache(StringRedisTemplate redis,ObjectMapper json,String key) { this.redis=redis; this.json=json; this.key=key; }
 public Optional<Snapshot> get() {
  List<?> result=redis.execute(READ,List.of(key));
  if(result==null || result.size()!=2) return Optional.empty();
  long millis=Long.parseLong(result.get(1).toString());
  if(millis<=0) return Optional.empty();
  try { return Optional.of(new Snapshot(json.readValue(result.get(0).toString(),new TypeReference<List<Link>>(){}),seconds(millis))); }
  catch(Exception invalid) { invalidate(); return Optional.empty(); }
 }
 public void put(List<Link> links,Duration ttl) {
  try { redis.opsForValue().set(key,json.writeValueAsString(links),ttl); }
  catch(com.fasterxml.jackson.core.JsonProcessingException invalid) { throw new IllegalStateException("No se pudo serializar la cache",invalid); }
 }
 public long remainingSeconds() { Long ttl=redis.getExpire(key,java.util.concurrent.TimeUnit.MILLISECONDS); return ttl==null ? 0 : seconds(ttl); }
 private static long seconds(long millis) { return millis<=0 ? 0 : (millis+999)/1000; }
 public void invalidate() { redis.delete(key); }
}