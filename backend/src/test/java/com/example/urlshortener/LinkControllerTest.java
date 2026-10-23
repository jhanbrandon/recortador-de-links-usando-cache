package com.example.urlshortener;
import com.example.urlshortener.application.service.LinkService;
import com.example.urlshortener.infrastructure.factory.InMemoryFactory;
import com.example.urlshortener.infrastructure.web.controllers.*;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.Duration;
class LinkControllerTest {
 MockMvc mvc; LinkService service; InMemoryFactory factory;
 @BeforeEach void setup(){factory=new InMemoryFactory();service=new LinkService(factory.createRepository(),factory.createCache());mvc=MockMvcBuilders.standaloneSetup(new LinkController(service,"http://localhost:8080")).setControllerAdvice(new ApiExceptionHandler()).build();}
 @Test void createReturnsAbsoluteUrl() throws Exception {mvc.perform(post("/api/links").contentType("application/json").content("{\"originalUrl\":\"https://example.com\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.shortUrl").value(org.hamcrest.Matchers.startsWith("http://localhost:8080/r/")));}
 @Test void redirectSurvivesCacheExpiration() throws Exception {var link=service.create("https://example.com/path?a=1");factory.createCache().put(java.util.List.of(link),Duration.ZERO);mvc.perform(get("/r/"+link.shortCode())).andExpect(status().isFound()).andExpect(header().string("Location",link.originalUrl()));}
 @Test void unknownRedirectIs404() throws Exception {mvc.perform(get("/r/missing")).andExpect(status().isNotFound());}
 @Test void invalidUrlIs400() throws Exception {mvc.perform(post("/api/links").contentType("application/json").content("{\"originalUrl\":\"javascript:alert(1)\"}")).andExpect(status().isBadRequest());}
 @Test void listAndStatusExposeCache() throws Exception {mvc.perform(get("/api/links/cache/status")).andExpect(jsonPath("$.active").value(false));mvc.perform(get("/api/links")).andExpect(jsonPath("$.cache.source").value("mongo"));mvc.perform(get("/api/links")).andExpect(jsonPath("$.cache.source").value("redis"));}
}