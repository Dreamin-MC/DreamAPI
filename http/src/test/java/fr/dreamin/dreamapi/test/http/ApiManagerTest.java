package fr.dreamin.dreamapi.test.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.HttpServer;
import fr.dreamin.dreamapi.api.http.ApiManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApiManager Native Java 25 HttpClient Tests (Feature F8)")
class ApiManagerTest {

  public record UserDto(int id, String name) {}

  private HttpServer server;
  private int port;
  private final AtomicInteger getCount = new AtomicInteger();

  @BeforeEach
  void setUp() throws IOException {
    getCount.set(0);
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    port = server.getAddress().getPort();

    server.createContext("/", exchange -> {
      final var response = "pong".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, response.length);
      try (final var os = exchange.getResponseBody()) {
        os.write(response);
      }
    });

    server.createContext("/users/1", exchange -> {
      getCount.incrementAndGet();
      final var response = "{\"id\":1,\"name\":\"Dreamin\"}".getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, response.length);
      try (final var os = exchange.getResponseBody()) {
        os.write(response);
      }
    });

    server.createContext("/users", exchange -> {
      final var method = exchange.getRequestMethod();
      if ("POST".equalsIgnoreCase(method)) {
        final var response = "{\"id\":2,\"name\":\"Created\"}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(201, response.length);
        try (final var os = exchange.getResponseBody()) {
          os.write(response);
        }
      } else {
        final var response = "[{\"id\":1,\"name\":\"Dreamin\"},{\"id\":2,\"name\":\"Created\"}]".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (final var os = exchange.getResponseBody()) {
          os.write(response);
        }
      }
    });

    server.createContext("/error", exchange -> {
      final var response = "{\"error\":\"not found\"}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(404, response.length);
      try (final var os = exchange.getResponseBody()) {
        os.write(response);
      }
    });

    server.start();
  }

  @AfterEach
  void tearDown() {
    if (server != null)
      server.stop(0);
  }

  @Test
  @DisplayName("Ping check synchronous and asynchronous")
  void testPing() throws Exception {
    try (final var client = new ApiManager.Builder()
      .baseUrl("http://127.0.0.1:" + port)
      .build()) {

      assertTrue(client.ping());
      assertTrue(client.pingAsync().get(5, TimeUnit.SECONDS));

      try (final var badClient = new ApiManager.Builder()
        .baseUrl("http://127.0.0.1:1")
        .timeout(500)
        .build()) {
        assertFalse(badClient.ping());
        assertFalse(badClient.pingAsync().get(5, TimeUnit.SECONDS));
      }
    }
  }

  @Test
  @DisplayName("GET and POST synchronous requests with deserialization")
  void testGetAndPost() throws Exception {
    try (final var client = new ApiManager.Builder()
      .baseUrl("http://127.0.0.1:" + port)
      .build()) {

      final var user = client.get("/users/1", UserDto.class);
      assertNotNull(user);
      assertEquals(1, user.id());
      assertEquals("Dreamin", user.name());

      final var users = client.getAll("/users", new TypeReference<List<UserDto>>() {});
      assertEquals(2, users.size());

      final var created = client.post("/users", "{\"name\":\"Created\"}", UserDto.class);
      assertEquals(2, created.id());
      assertEquals("Created", created.name());
    }
  }

  @Test
  @DisplayName("GET and POST asynchronous non-blocking pipelines")
  void testAsyncRequests() throws Exception {
    try (final var client = new ApiManager.Builder()
      .baseUrl("http://127.0.0.1:" + port)
      .build()) {

      final var userFuture = client.getAsync("/users/1", UserDto.class);
      final var user = userFuture.get(5, TimeUnit.SECONDS);
      assertEquals(1, user.id());

      final var allFuture = client.getAllAsync("/users", new TypeReference<List<UserDto>>() {});
      final var list = allFuture.get(5, TimeUnit.SECONDS);
      assertEquals(2, list.size());

      final var postFuture = client.postAsync("/users", "{\"name\":\"Created\"}", UserDto.class);
      final var created = postFuture.get(5, TimeUnit.SECONDS);
      assertEquals(2, created.id());
    }
  }

  @Test
  @DisplayName("HTTP 404/500 throws ApiRequestException with status code")
  void testErrorHandling() {
    try (final var client = new ApiManager.Builder()
      .baseUrl("http://127.0.0.1:" + port)
      .build()) {

      final var ex = assertThrows(Exception.class, () -> client.get("/error", UserDto.class));
      assertTrue(ex instanceof ApiManager.ApiRequestException);
      assertEquals(404, ((ApiManager.ApiRequestException) ex).getStatusCode());
    }
  }

  @Test
  @DisplayName("Caching with active TTL eviction")
  void testCacheWithTtl() throws Exception {
    try (final var client = new ApiManager.Builder()
      .baseUrl("http://127.0.0.1:" + port)
      .enableCache(true)
      .cacheTtl(80)
      .build()) {

      final var u1 = client.get("/users/1", UserDto.class);
      assertEquals(1, u1.id());
      assertEquals(1, getCount.get());

      // Second request hits cache, server count should NOT increase
      final var u2 = client.get("/users/1", UserDto.class);
      assertEquals(1, u2.id());
      assertEquals(1, getCount.get());

      // Sleep past TTL
      Thread.sleep(120);

      // Third request should evict expired entry and fetch fresh from server
      final var u3 = client.get("/users/1", UserDto.class);
      assertEquals(1, u3.id());
      assertEquals(2, getCount.get());
    }
  }
}
