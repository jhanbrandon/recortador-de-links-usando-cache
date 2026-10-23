package com.example.urlshortener.application.ports;
import com.example.urlshortener.domain.model.Link;
import java.util.List;
import java.util.Optional;
public interface LinkRepository {
 Link save(Link link) throws DuplicateCodeException;
 List<Link> findAll();
 Optional<Link> findByShortCode(String code);
 boolean existsByShortCode(String code);
}