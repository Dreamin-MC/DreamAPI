package fr.dreamin.dreamapi.api.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dreamin.dreamapi.api.config.Configurations;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * REST HTTP Client built on top of native Java 25 {@link HttpClient}.
 * Features non-blocking asynchronous operations, thread-safe response caching with active TTL eviction,
 * and zero external HTTP library dependencies.
 */
@Getter
public final class ApiManager implements Closeable {

  private final HttpClient httpClient;
  private final String baseUrl;
  private final Map<String, String> defaultHeaders;
  private final ObjectMapper mapper;
  private final boolean enableCache;
  private final long cacheTtlMs;
  private final long timeout;

  private final ConcurrentHashMap<String, CachedResponse> cache = new ConcurrentHashMap<>();

  private ApiManager(final @NotNull Builder builder) {
    this.baseUrl = builder.baseUrl;
    this.defaultHeaders = Collections.unmodifiableMap(new HashMap<>(builder.defaultHeaders));
    this.mapper = builder.mapper != null ? builder.mapper : Configurations.MAPPER;
    this.enableCache = builder.enableCache;
    this.cacheTtlMs = builder.cacheTtlMs;
    this.timeout = builder.timeout;

    if (builder.customHttpClient != null)
      this.httpClient = builder.customHttpClient;
    else {
      final var clientBuilder = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(builder.timeout))
        .version(builder.httpVersion)
        .followRedirects(builder.followRedirects);

      if (builder.executor != null)
        clientBuilder.executor(builder.executor);

      this.httpClient = clientBuilder.build();
    }
  }

  // ###############################################################
  // --------------------- HEALTH CHECK ----------------------------
  // ###############################################################

  /**
   * Synchronous check if the API base URL responds successfully (HTTP 200-299).
   */
  public boolean ping() {
    try {
      final var request = HttpRequest.newBuilder()
        .uri(URI.create(this.baseUrl))
        .timeout(Duration.ofMillis(this.timeout))
        .GET()
        .build();

      final var response = this.httpClient.send(request, HttpResponse.BodyHandlers.discarding());
      return response.statusCode() >= 200 && response.statusCode() < 300;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Asynchronous check if the API base URL responds successfully (HTTP 200-299).
   */
  public CompletableFuture<Boolean> pingAsync() {
    final var request = HttpRequest.newBuilder()
      .uri(URI.create(this.baseUrl))
      .timeout(Duration.ofMillis(this.timeout))
      .GET()
      .build();

    return this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
      .thenApply(res -> res.statusCode() >= 200 && res.statusCode() < 300)
      .exceptionally(ex -> false);
  }

  // ###############################################################
  // -------------------- CORE REQUEST METHODS ---------------------
  // ###############################################################

  @NotNull
  private <T> T request(final @NotNull HttpRequest request, final @NotNull Class<T> type) throws Exception {
    final var response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    final var status = response.statusCode();
    final var body = response.body();

    if (status >= 400)
      throw new ApiRequestException(status, body);

    if (body == null || body.isBlank())
      return this.mapper.readValue("{}", type);
    return this.mapper.readValue(body, type);
  }

  @NotNull
  private <T> T request(final @NotNull HttpRequest request, final @NotNull TypeReference<T> ref) throws Exception {
    final var response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    final var status = response.statusCode();
    final var body = response.body();

    if (status >= 400)
      throw new ApiRequestException(status, body);

    if (body == null || body.isBlank())
      return this.mapper.readValue("{}", ref);
    return this.mapper.readValue(body, ref);
  }

  @NotNull
  private <T> CompletableFuture<T> requestAsync(final @NotNull HttpRequest request, final @NotNull Class<T> type) {
    return this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
      .thenApply(response -> {
        final var status = response.statusCode();
        final var body = response.body();

        if (status >= 400)
          throw new ApiRequestException(status, body);

        try {
          if (body == null || body.isBlank())
            return this.mapper.readValue("{}", type);
          return this.mapper.readValue(body, type);
        } catch (Exception e) {
          throw new RuntimeException("Failed to deserialize response body to " + type.getSimpleName(), e);
        }
      });
  }

  @NotNull
  private <T> CompletableFuture<T> requestAsync(final @NotNull HttpRequest request, final @NotNull TypeReference<T> ref) {
    return this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
      .thenApply(response -> {
        final var status = response.statusCode();
        final var body = response.body();

        if (status >= 400)
          throw new ApiRequestException(status, body);

        try {
          if (body == null || body.isBlank())
            return this.mapper.readValue("{}", ref);
          return this.mapper.readValue(body, ref);
        } catch (Exception e) {
          throw new RuntimeException("Failed to deserialize response body to TypeReference", e);
        }
      });
  }

  // ###############################################################
  // ------------------ SYNCHRONOUS HTTP METHODS -------------------
  // ###############################################################

  @NotNull
  public <T> T get(final @NotNull String path, final @NotNull Class<T> type) throws Exception {
    final var url = buildUrl(path);
    if (this.enableCache) {
      final var cached = cache.get(url);
      if (cached != null) {
        if (cached.isNotExpired(this.cacheTtlMs))
          return this.mapper.readValue(cached.data(), type);
        cache.remove(url, cached);
      }
    }

    final var req = createRequestBuilder(url).GET().build();
    final var response = request(req, type);
    if (this.enableCache)
      this.cache.put(url, new CachedResponse(this.mapper.writeValueAsString(response)));
    return response;
  }

  @NotNull
  public <T> T getAll(final @NotNull String path, final @NotNull TypeReference<T> ref) throws Exception {
    final var url = buildUrl(path);
    if (this.enableCache) {
      final var cached = cache.get(url);
      if (cached != null) {
        if (cached.isNotExpired(this.cacheTtlMs))
          return this.mapper.readValue(cached.data(), ref);
        cache.remove(url, cached);
      }
    }

    final var req = createRequestBuilder(url).GET().build();
    final var response = request(req, ref);
    if (this.enableCache)
      this.cache.put(url, new CachedResponse(this.mapper.writeValueAsString(response)));
    return response;
  }

  @NotNull
  public <T> T post(final @NotNull String path, final @Nullable String body, final @NotNull Class<T> type) throws Exception {
    final var publisher = (body != null && !body.isBlank())
      ? HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)
      : HttpRequest.BodyPublishers.noBody();
    final var req = createRequestBuilder(buildUrl(path)).POST(publisher).build();
    return request(req, type);
  }

  @NotNull
  public <T> T put(final @NotNull String path, final @Nullable String body, final @NotNull Class<T> type) throws Exception {
    final var publisher = (body != null && !body.isBlank())
      ? HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)
      : HttpRequest.BodyPublishers.noBody();
    final var req = createRequestBuilder(buildUrl(path)).PUT(publisher).build();
    return request(req, type);
  }

  @NotNull
  public <T> T delete(final @NotNull String path, final @NotNull Class<T> type) throws Exception {
    final var req = createRequestBuilder(buildUrl(path)).DELETE().build();
    return request(req, type);
  }

  // ###############################################################
  // ----------------- ASYNCHRONOUS HTTP METHODS -------------------
  // ###############################################################

  @NotNull
  public <T> CompletableFuture<T> getAsync(final @NotNull String path, final @NotNull Class<T> type) {
    final var url = buildUrl(path);
    if (this.enableCache) {
      final var cached = cache.get(url);
      if (cached != null) {
        if (cached.isNotExpired(this.cacheTtlMs)) {
          try {
            return CompletableFuture.completedFuture(this.mapper.readValue(cached.data(), type));
          } catch (Exception e) {
            cache.remove(url, cached);
          }
        } else
          cache.remove(url, cached);
      }
    }

    final var req = createRequestBuilder(url).GET().build();
    return requestAsync(req, type).thenApply(response -> {
      if (this.enableCache) {
        try {
          this.cache.put(url, new CachedResponse(this.mapper.writeValueAsString(response)));
        } catch (Exception ignored) {}
      }
      return response;
    });
  }

  @NotNull
  public <T> CompletableFuture<T> getAllAsync(final @NotNull String path, final @NotNull TypeReference<T> ref) {
    final var url = buildUrl(path);
    if (this.enableCache) {
      final var cached = cache.get(url);
      if (cached != null) {
        if (cached.isNotExpired(this.cacheTtlMs)) {
          try {
            return CompletableFuture.completedFuture(this.mapper.readValue(cached.data(), ref));
          } catch (Exception e) {
            cache.remove(url, cached);
          }
        } else
          cache.remove(url, cached);
      }
    }

    final var req = createRequestBuilder(url).GET().build();
    return requestAsync(req, ref).thenApply(response -> {
      if (this.enableCache) {
        try {
          this.cache.put(url, new CachedResponse(this.mapper.writeValueAsString(response)));
        } catch (Exception ignored) {}
      }
      return response;
    });
  }

  @NotNull
  public <T> CompletableFuture<T> postAsync(final @NotNull String path, final @Nullable String body, final @NotNull Class<T> type) {
    final var publisher = (body != null && !body.isBlank())
      ? HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)
      : HttpRequest.BodyPublishers.noBody();
    final var req = createRequestBuilder(buildUrl(path)).POST(publisher).build();
    return requestAsync(req, type);
  }

  @NotNull
  public <T> CompletableFuture<T> putAsync(final @NotNull String path, final @Nullable String body, final @NotNull Class<T> type) {
    final var publisher = (body != null && !body.isBlank())
      ? HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)
      : HttpRequest.BodyPublishers.noBody();
    final var req = createRequestBuilder(buildUrl(path)).PUT(publisher).build();
    return requestAsync(req, type);
  }

  @NotNull
  public <T> CompletableFuture<T> deleteAsync(final @NotNull String path, final @NotNull Class<T> type) {
    final var req = createRequestBuilder(buildUrl(path)).DELETE().build();
    return requestAsync(req, type);
  }

  // ###############################################################
  // ------------------------ UTILITIES ----------------------------
  // ###############################################################

  @NotNull
  private String buildUrl(final @NotNull String path) {
    if (path.startsWith("http://") || path.startsWith("https://"))
      return path;
    final var baseHasSlash = this.baseUrl.endsWith("/");
    final var pathHasSlash = path.startsWith("/");
    if (baseHasSlash && pathHasSlash)
      return this.baseUrl + path.substring(1);
    if (!baseHasSlash && !pathHasSlash)
      return this.baseUrl + "/" + path;
    return this.baseUrl + path;
  }

  private HttpRequest.Builder createRequestBuilder(final @NotNull String url) {
    final var builder = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .timeout(Duration.ofMillis(this.timeout))
      .header("Content-Type", "application/json; charset=UTF-8");

    this.defaultHeaders.forEach(builder::header);
    return builder;
  }

  public void clearCache() {
    this.cache.clear();
  }

  public void pruneExpiredCache() {
    final var now = Instant.now().toEpochMilli();
    this.cache.entrySet().removeIf(entry -> (now - entry.getValue().timestamp()) > this.cacheTtlMs);
  }

  @Override
  public void close() {
    this.cache.clear();
    this.httpClient.close();
  }

  // ###############################################################
  // ------------------------- CACHING -----------------------------
  // ###############################################################

  public record CachedResponse(String data, long timestamp) {
    public CachedResponse(String data) {
      this(data, Instant.now().toEpochMilli());
    }

    public boolean isNotExpired(long ttl) {
      return (Instant.now().toEpochMilli() - timestamp) <= ttl;
    }

    public boolean isExpired(long ttl) {
      return !isNotExpired(ttl);
    }
  }

  // ###############################################################
  // ------------------------- EXCEPTION ---------------------------
  // ###############################################################

  @Getter
  public static class ApiRequestException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public ApiRequestException(int statusCode, String responseBody) {
      super("API request failed with status " + statusCode + ": " + responseBody);
      this.statusCode = statusCode;
      this.responseBody = responseBody;
    }
  }

  // ###############################################################
  // ------------------------- BUILDER -----------------------------
  // ###############################################################

  public static class Builder {
    private String baseUrl;
    private long timeout = 5000;
    private boolean enableCache = false;
    private long cacheTtlMs = 10_000;
    private ObjectMapper mapper;
    private final Map<String, String> defaultHeaders = new HashMap<>();
    private HttpClient customHttpClient;
    private HttpClient.Version httpVersion = HttpClient.Version.HTTP_2;
    private HttpClient.Redirect followRedirects = HttpClient.Redirect.NORMAL;
    private Executor executor;

    public @NotNull Builder baseUrl(final @NotNull String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public @NotNull Builder timeout(long millis) {
      this.timeout = millis;
      return this;
    }

    public @NotNull Builder mapper(ObjectMapper mapper) {
      this.mapper = mapper;
      return this;
    }

    public @NotNull Builder header(String key, String value) {
      this.defaultHeaders.put(key, value);
      return this;
    }

    public @NotNull Builder enableCache(boolean enabled) {
      this.enableCache = enabled;
      return this;
    }

    public @NotNull Builder cacheTtl(long millis) {
      this.cacheTtlMs = millis;
      return this;
    }

    public @NotNull Builder httpClient(HttpClient httpClient) {
      this.customHttpClient = httpClient;
      return this;
    }

    public @NotNull Builder version(HttpClient.Version version) {
      this.httpVersion = version;
      return this;
    }

    public @NotNull Builder followRedirects(HttpClient.Redirect redirect) {
      this.followRedirects = redirect;
      return this;
    }

    public @NotNull Builder executor(Executor executor) {
      this.executor = executor;
      return this;
    }

    public @NotNull ApiManager build() {
      if (baseUrl == null || baseUrl.isBlank())
        throw new IllegalStateException("Base URL cannot be null or blank.");
      return new ApiManager(this);
    }
  }
}