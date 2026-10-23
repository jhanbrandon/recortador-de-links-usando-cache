package com.example.urlshortener;
import com.example.urlshortener.application.ports.*;
import com.example.urlshortener.application.service.*;
import com.example.urlshortener.infrastructure.factory.*;
import com.example.urlshortener.infrastructure.cache.memory.*;
import com.example.urlshortener.infrastructure.persistence.memory.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.*;
import java.util.*;
class LinkServiceTest {
 static class MutableClock extends Clock {
  Instant time=Instant.parse("2026-01-01T00:00:00Z");
  public java.time.ZoneId getZone(){return ZoneOffset.UTC;}
  public Clock withZone(java.time.ZoneId zone){return this;}
  public Instant instant(){return time;}
  void advance(long seconds){time=time.plusSeconds(seconds);}
 }
 MutableClock clock; LinkInfrastructureFactory factory; LinkService service;
 @BeforeEach void setup(){clock=new MutableClock();factory=new InMemoryFactory(clock);service=new LinkService(factory.createRepository(),factory.createCache());}
 @Test void createsAndResolvesDifferentCodes(){var a=service.create("https://example.com/a");var b=service.create("http://example.com/b");assertNotEquals(a.shortCode(),b.shortCode());assertEquals(a,service.resolve(a.shortCode()));assertEquals(2,service.list().links().size());}
 @Test void rejectsInvalidUrls(){for(String url:Arrays.asList(null,""," ","javascript:alert(1)","file:///tmp/x","data:text/html,x","https://","https://bad host.com","https://user:pass@example.com","ftp://example.com"))assertThrows(IllegalArgumentException.class,()->service.create(url));}
 @Test void cacheExpiresWithoutLosingLinksOrRedirectTarget(){var link=service.create("https://example.com");assertEquals("mongo",service.list().source());assertEquals(30,service.cacheTtl());clock.advance(12);assertEquals("redis",service.list().source());assertEquals(18,service.cacheTtl());clock.advance(19);assertEquals(0,service.cacheTtl());assertEquals(link,service.resolve(link.shortCode()));assertEquals("mongo",service.list().source());assertEquals(1,service.list().links().size());}
 @Test void createInvalidatesAndNextReadStartsNewCycle(){service.list();clock.advance(10);service.create("https://example.com");assertEquals(0,service.cacheTtl());assertEquals("mongo",service.list().source());assertEquals(30,service.cacheTtl());}
 @Test void statusDoesNotCreateCache(){assertEquals(0,service.cacheTtl());assertTrue(factory.createCache().get().isEmpty());}
 @Test void factorySuppliesCompleteReusableFamily(){assertInstanceOf(InMemoryLinkRepository.class,factory.createRepository());assertInstanceOf(InMemoryLinkCache.class,factory.createCache());assertSame(factory.createRepository(),factory.createRepository());assertSame(factory.createCache(),factory.createCache());}
 @Test void duplicateCodeIsRetried(){Queue<String> codes=new ArrayDeque<>(List.of("same","same","other"));service=new LinkService(factory.createRepository(),factory.createCache(),codes::remove,clock);service.create("https://example.com/a");assertEquals("other",service.create("https://example.com/b").shortCode());}
 @Test void databaseRaceIsRetried(){LinkRepository repository=mock(LinkRepository.class);when(repository.save(any())).thenThrow(new DuplicateCodeException()).thenAnswer(i->i.getArgument(0));service=new LinkService(repository,factory.createCache(),()->"abcdefg",clock);assertNotNull(service.create("https://example.com"));verify(repository,times(2)).save(any());}
 @Test void retriesAreBounded(){LinkRepository repository=mock(LinkRepository.class);when(repository.existsByShortCode(any())).thenReturn(true);service=new LinkService(repository,factory.createCache(),()->"same",clock);assertThrows(IllegalStateException.class,()->service.create("https://example.com"));verify(repository,times(10)).existsByShortCode("same");}
 @Test void unknownCodeThrows(){assertThrows(LinkNotFoundException.class,()->service.resolve("missing"));}
}