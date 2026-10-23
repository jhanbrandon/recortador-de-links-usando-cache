package com.example.urlshortener.infrastructure.persistence.mongo;
import com.example.urlshortener.application.ports.*;
import com.example.urlshortener.domain.model.Link;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DuplicateKeyException;
import java.util.*;
public class MongoLinkRepository implements LinkRepository {
 private final MongoTemplate mongo;
 public MongoLinkRepository(MongoTemplate mongo) {
  this.mongo=mongo;
  mongo.indexOps(LinkDocument.class).ensureIndex(new Index().on("shortCode",Sort.Direction.ASC).unique());
 }
 public Link save(Link link) {
  try { return mongo.insert(LinkDocument.from(link)).toDomain(); }
  catch(DuplicateKeyException conflict) { throw new DuplicateCodeException(); }
 }
 public List<Link> findAll() { return mongo.find(new Query().with(Sort.by(Sort.Direction.DESC,"createdAt")),LinkDocument.class).stream().map(LinkDocument::toDomain).toList(); }
 private Query byCode(String code) { return Query.query(Criteria.where("shortCode").is(code)); }
 public Optional<Link> findByShortCode(String code) { return Optional.ofNullable(mongo.findOne(byCode(code),LinkDocument.class)).map(LinkDocument::toDomain); }
 public boolean existsByShortCode(String code) { return mongo.exists(byCode(code),LinkDocument.class); }
}