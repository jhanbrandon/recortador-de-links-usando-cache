package com.example.urlshortener.application.ports;
/** GoF Abstract Factory: each family supplies both abstract products. */
public interface LinkInfrastructureFactory {
 LinkRepository createRepository();
 LinkCache createCache();
}