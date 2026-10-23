package com.example.urlshortener.infrastructure.web.controllers;
import com.example.urlshortener.application.ports.LinkUseCases;
import com.example.urlshortener.domain.model.Link;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.net.URI;
import java.time.Instant;
import java.util.List;
@RestController
public class LinkController {
 private final LinkUseCases service;
 private final String baseUrl;
 public LinkController(LinkUseCases service,@Value("${app.base-url}") String baseUrl) {
  URI base=URI.create(baseUrl);
  if(base.getHost()==null || !("http".equals(base.getScheme()) || "https".equals(base.getScheme())) || base.getQuery()!=null || base.getFragment()!=null) throw new IllegalArgumentException("APP_BASE_URL debe ser una URL HTTP(S) absoluta sin query ni fragmento");
  this.service=service; this.baseUrl=baseUrl.replaceAll("/+$","");
 }
 public record CreateRequest(String originalUrl) {}
 public record LinkResponse(String id,String originalUrl,String shortCode,String shortUrl,Instant createdAt) {}
 public record CacheResponse(String source,long ttlSeconds,boolean active) {}
 public record ListingResponse(List<LinkResponse> links,CacheResponse cache) {}
 private LinkResponse response(Link link) { return new LinkResponse(link.id(),link.originalUrl(),link.shortCode(),baseUrl+"/r/"+link.shortCode(),link.createdAt()); }
 @PostMapping("/api/links") public ResponseEntity<LinkResponse> create(@RequestBody CreateRequest request) {
  var result=response(service.create(request.originalUrl()));
  return ResponseEntity.created(URI.create(result.shortUrl())).body(result);
 }
 @GetMapping("/api/links") public ResponseEntity<ListingResponse> list() {
  var result=service.list();
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new ListingResponse(result.links().stream().map(this::response).toList(),new CacheResponse(result.source(),result.ttlSeconds(),result.ttlSeconds()>0)));
 }
 @GetMapping("/api/links/cache/status") public ResponseEntity<CacheResponse> status() {
  long ttl=service.cacheTtl();
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new CacheResponse("redis",ttl,ttl>0));
 }
 @GetMapping("/r/{code}") public ResponseEntity<Void> redirect(@PathVariable String code) {
  return ResponseEntity.status(HttpStatus.FOUND).cacheControl(CacheControl.noStore()).location(URI.create(service.resolve(code).originalUrl())).build();
 }
}