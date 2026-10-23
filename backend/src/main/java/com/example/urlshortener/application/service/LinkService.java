package com.example.urlshortener.application.service;
import com.example.urlshortener.application.ports.*;
import com.example.urlshortener.domain.model.Link;
import java.net.URI;
import java.security.SecureRandom;
import java.time.*;
import java.util.UUID;
import java.util.function.Supplier;
/** Application core only depends on ports and JDK. */
public class LinkService implements LinkUseCases {
 public static final Duration CACHE_TTL = Duration.ofSeconds(30);
 private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
 private final LinkRepository repository;
 private final LinkCache cache;
 private final Supplier<String> codes;
 private final Clock clock;
 public LinkService(LinkRepository repository, LinkCache cache) {
  this(repository, cache, randomCodes(), Clock.systemUTC());
 }
 public LinkService(LinkRepository repository, LinkCache cache, Supplier<String> codes, Clock clock) {
  this.repository=repository; this.cache=cache; this.codes=codes; this.clock=clock;
 }
 private static Supplier<String> randomCodes() {
  SecureRandom random=new SecureRandom();
  return () -> { StringBuilder result=new StringBuilder(); for(int i=0;i<7;i++) result.append(ALPHABET[random.nextInt(ALPHABET.length)]); return result.toString(); };
 }
 public synchronized Link create(String originalUrl) {
  String url=validate(originalUrl);
  for(int attempt=0;attempt<10;attempt++) {
   String code=codes.get();
   if(repository.existsByShortCode(code)) continue;
   Link saved;
   try { saved=repository.save(new Link(UUID.randomUUID().toString(),url,code,clock.instant().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))); }
   catch(DuplicateCodeException collision) { continue; }
   cache.invalidate();
   return saved;
  }
  throw new IllegalStateException("No se pudo generar un codigo unico. Intenta nuevamente.");
 }
 public synchronized Listing list() {
  var hit=cache.get();
  if(hit.isPresent()) return new Listing(hit.get().links(),"redis",hit.get().ttlSeconds());
  var links=repository.findAll();
  cache.put(links,CACHE_TTL);
  return new Listing(links,"mongo",cache.remainingSeconds());
 }
 public Link resolve(String code) { return repository.findByShortCode(code).orElseThrow(LinkNotFoundException::new); }
 public long cacheTtl() { return cache.remainingSeconds(); }
 private String validate(String input) {
  if(input==null || input.isBlank()) throw new IllegalArgumentException("Ingresa una URL HTTP o HTTPS valida.");
  String value=input.trim();
  try {
   URI uri=new URI(value);
   if(!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) || uri.getHost()==null || uri.getUserInfo()!=null || uri.getPort()>65535)
    throw new IllegalArgumentException();
   return uri.toASCIIString();
  } catch(Exception invalid) { throw new IllegalArgumentException("Ingresa una URL HTTP o HTTPS valida, sin credenciales ni espacios."); }
 }
}