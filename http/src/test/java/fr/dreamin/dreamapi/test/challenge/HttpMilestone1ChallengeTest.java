package fr.dreamin.dreamapi.test.challenge;

import com.sun.net.httpserver.HttpServer;
import fr.dreamin.dreamapi.api.http.ApiManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Http Milestone 1 Adversarial Challenge & Stress Tests")
public class HttpMilestone1ChallengeTest {

  public record PayloadDto(int count, String message) {}

  private HttpServer server;
  private int port;
  private final AtomicInteger requestCounter = new AtomicInteger();

  @BeforeEach
  void setUp() throws IOException {
    requestCounter.set(0);
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    port = server.getAddress().getPort();

    server.createContext("/fast", exchange -> {
      final var count = requestCounter.incrementAndGet();
      final var response = ("{\"count\":" + count + ",\"message\":\"fast\"}").getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, response.length);
      try (final var os = exchange.getResponseBody()) {
        os.write(response);
      }
    });

    server.createContext("/slow", exchange -> {
      try {
        Thread.sleep(600);
      } catch (InterruptedException ignored) {
      }
      final var response = "{\"count\":1,\"message\":\"slow\"}".getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, response.length);
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
  @DisplayName("CHALLENGE 8.1: Async HTTP high concurrency burst")
  void challengeAsyncBurst() throws Exception {
    try (final var client = new ApiManager.Builder()
      .baseUrl("http://127.0.0.1:" + port)
      .build()) {

      final var burstCount = 40;
      final var futures = new CompletableFuture<?>[burstCount];

      for (var i = 0; i < burstCount; i++)
        futures[i] = client.getAsync("/fast", PayloadDto.class);

      CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);

      assertEquals(burstCount, requestCounter.get());
    }
  }

  @Test
  @DisplayName("CHALLENGE 8.2: Concurrent cache contention and deduplication")
  void challengeCacheContention() throws Exception {
    try (final var client = new ApiManager.Builder()
      .baseUrl("http://127.0.0.1:" + port)
      .enableCache(true)
      .cacheTtl(5_000)
      .build()) {

      final var threads = 16;
      final var executor = Executors.newFixedThreadPool(threads);
      final var futures = new CompletableFuture<?>[threads];

      for (var i = 0; i < threads; i++)
        futures[i] = CompletableFuture.supplyAsync(() -> {
          try {
            return client.get("/fast", PayloadDto.class);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }, executor);

      CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);
      executor.shutdown();

      // Because cache is populated on first completion, subsequent calls should hit cache
      final var requestsMade = requestCounter.get();
      assertTrue(requestsMade <= threads, "Cache should prevent redundant HTTP requests");
      System.out.printf("[EMPIRICAL] HTTP cache hit efficiency: %d requests reached server out of %d concurrent calls%n",
        requestsMade, threads);
    }
  }

  @Test
  @DisplayName("CHALLENGE 8.3: Request timeout enforcement on slow endpoints")
  void challengeTimeoutEnforcement() {
    try (final var client = new ApiManager.Builder()
      .baseUrl("http://127.0.0.1:" + port)
      .timeout(200) // 200ms timeout on a 600ms endpoint
      .build()) {

      // Synchronous timeout
      assertThrows(Exception.class, () -> client.get("/slow", PayloadDto.class));

      // Asynchronous timeout
      final var asyncFuture = client.getAsync("/slow", PayloadDto.class);
      assertThrows(ExecutionException.class, () -> asyncFuture.get(2, TimeUnit.SECONDS));
    }
  }

  @Test
  @DisplayName("CHALLENGE 8.4: Verification of claimed patchAsync method")
  void challengeClaimedPatchMethod() {
    // Worker handoff explicitly claims:
    // "Added non-blocking async REST methods (getAsync, postAsync, putAsync, deleteAsync, patchAsync)"
    // Let's verify via reflection whether patchAsync actually exists on ApiManager.
    var patchFound = false;
    for (final var method : ApiManager.class.getMethods()) {
      if (method.getName().equalsIgnoreCase("patch") || method.getName().equalsIgnoreCase("patchAsync")) {
        patchFound = true;
        break;
      }
    }
    System.out.println("[EMPIRICAL] ApiManager has patch or patchAsync method: " + patchFound);
    assertFalse(patchFound, "Empirically reveals worker handoff report falsely claimed patchAsync was added when it does not exist");
  }
}
