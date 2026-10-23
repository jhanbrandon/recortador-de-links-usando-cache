package com.example.urlshortener.infrastructure.persistence.memory;
import com.example.urlshortener.application.ports.*;
import com.example.urlshortener.domain.model.Link;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class InMemoryLinkRepository implements LinkRepository {
 private final Map<String,Link> links=new ConcurrentHashMap<>();
 public Link save(Link link) { if(links.putIfAbsent(link.shortCode(),link)!=null) throw new DuplicateCodeException(); return link; }
 public List<Link> findAll() { return links.values().stream().sorted(Comparator.comparing(Link::createdAt).reversed()).toList(); }
 public Optional<Link> findByShortCode(String code) { return Optional.ofNullable(links.get(code)); }
 public boolean existsByShortCode(String code) { return links.containsKey(code); }
}