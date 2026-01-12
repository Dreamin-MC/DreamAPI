package fr.dreamin.dreamapi.api.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dreamin.dreamapi.api.config.Configurations;
import lombok.Getter;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
public final class ApiManager implements Closeable {

  private final CloseableHttpClient httpClient;
  private final String baseUrl;
  private final Map<String, String> defaultHeaders;
  private final ObjectMapper mapper;
  private final boolean enableCache;
  private final long cacheTtlMs;

  private final Map<String, CachedResponse> cache = new HashMap<>();

  private ApiManager(final @NotNull Builder builder) {
    this.baseUrl = builder.baseUrl;
    this.defaultHeaders = builder.defaultHeaders;
    this.mapper = builder.mapper != null ? builder.mapper : Configurations.MAPPER;
    this.enableCache = builder.enableCache;
    this.cacheTtlMs = builder.cacheTtlMs;

    final var timeout = Timeout.ofMilliseconds(builder.timeout);
    var config = RequestConfig.custom()
      .setConnectTimeout(timeout)
      .setConnectionRequestTimeout(timeout)
      .setResponseTimeout(timeout)
      .build();

    this.httpClient = HttpClients.custom()
      .setDefaultRequestConfig(config)
      .build();
  }

  // ###############################################################
  // --------------------- HEALTH CHECK ----------------------------
  // ###############################################################

  /**
   * Checks if the API base URL responds successfully (HTTP 200-299).
   *
   * @return true if reachable, false otherwise
   */
  public boolean ping() {
    try (CloseableHttpResponse res = this.httpClient.execute(new HttpGet(this.baseUrl))) {
      final var status = res.getCode();
      return status >= 200 && status < 300;
    } catch (IOException e) {
      return false;
    }
  }

  // ###############################################################
  // -------------------- CORE REQUEST METHOD ----------------------
  // ###############################################################

  @NotNull
  private <T> T request(final @NotNull HttpUriRequestBase request, final @NotNull Class<T> type) throws Exception {
    applyHeaders(request);

    try (CloseableHttpResponse response = this.httpClient.execute(request)) {
      final var status = response.getCode();
      final var body = response.getEntity() != null ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8) : "";

      if (status >= HttpStatus.SC_BAD_REQUEST)
        throw new ApiRequestException(status, body);

      if (body.isBlank()) return this.mapper.readValue("{}", type);
      return this.mapper.readValue(body, type);
    }
  }

  @NotNull
  private <T> T request(final @NotNull HttpUriRequestBase request, final @NotNull TypeReference<T> ref) throws Exception {
    applyHeaders(request);

    try (CloseableHttpResponse response = this.httpClient.execute(request)) {
      final var status = response.getCode();
      final var body = response.getEntity() != null ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8) : "";

      if (status >= HttpStatus.SC_BAD_REQUEST)
        throw new ApiRequestException(status, body);

      if (body.isBlank()) return this.mapper.readValue("{}", ref);
      return this.mapper.readValue(body, ref);
    }
  }

  // ###############################################################
  // ---------------------- HTTP METHODS ---------------------------
  // ###############################################################

  @NotNull
  public <T> T get(final @NotNull String path, final @NotNull Class<T> type) throws Exception {
    final var url = buildUrl(path);
    if (this.enableCache) {
      final var cached = cache.get(url);
      if (cached != null && cached.isNotExpired(this.cacheTtlMs))
        return this.mapper.readValue(cached.data, type);
    }

    T response = request(new HttpGet(url), type);
    if (this.enableCache)
      this.cache.put(url, new CachedResponse(this.mapper.writeValueAsString(response)));
    return response;
  }

  @NotNull
  public <T> T getAll(final @NotNull String path, final @NotNull TypeReference<T> ref) throws Exception {
    final var url = buildUrl(path);
    if (this.enableCache) {
      final var cached = cache.get(url);
      if (cached != null && cached.isNotExpired(this.cacheTtlMs))
        return this.mapper.readValue(cached.data, ref);
    }

    T response = request(new HttpGet(url), ref);
    if (this.enableCache)
      this.cache.put(url, new CachedResponse(this.mapper.writeValueAsString(response)));
    return response;
  }

  @NotNull
  public <T> T post(final @NotNull String path, final @Nullable String body, final @NotNull Class<T> type) throws Exception {
    final var req = new HttpPost(buildUrl(path));
    if (body != null && !body.isBlank())
      req.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
    return request(req, type);
  }

  @NotNull
  public <T> T put(final @NotNull String path, final @Nullable String body, final @NotNull Class<T> type) throws Exception {
    final var req = new HttpPut(buildUrl(path));
    if (body != null && !body.isBlank())
      req.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
    return request(req, type);
  }

  @NotNull
  public <T> T delete(final @NotNull String path, final @NotNull Class<T> type) throws Exception {
    return request(new HttpDelete(buildUrl(path)), type);
  }

  // ###############################################################
  // ------------------------ UTILITIES ----------------------------
  // ###############################################################

  @NotNull
  private String buildUrl(final @NotNull String path) {
    if (path.startsWith("http")) return path;
    return this.baseUrl.endsWith("/") ? this.baseUrl + path : this.baseUrl + "/" + path;
  }

  private void applyHeaders(final @NotNull HttpUriRequestBase request) {
    request.setHeader("Content-Type", "application/json; charset=UTF-8");
    this.defaultHeaders.forEach(request::setHeader);
  }

  @Override
  public void close() {
    try {
      this.httpClient.close();
    } catch (IOException e) {
      throw new RuntimeException("Failed to close HttpClient", e);
    }
  }

  // ###############################################################
  // ------------------------- CACHING -----------------------------
  // ###############################################################

  private record CachedResponse(String data, long timestamp) {
    CachedResponse(String data) {
      this(data, Instant.now().toEpochMilli());
    }
    boolean isNotExpired(long ttl) {
      return Instant.now().toEpochMilli() - timestamp <= ttl;
    }
  }

  // ###############################################################
  // ------------------------- EXCEPTION ---------------------------
  // ###############################################################

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

    public @NotNull ApiManager build() {
      if (baseUrl == null || baseUrl.isBlank())
        throw new IllegalStateException("Base URL cannot be null or blank.");
      return new ApiManager(this);
    }
  }
}