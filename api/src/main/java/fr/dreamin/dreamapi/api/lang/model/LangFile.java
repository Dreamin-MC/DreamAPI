package fr.dreamin.dreamapi.api.lang.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public final class LangFile {
  public String namespace;
  public String value;

  @JsonProperty("default")
  public String defaultLocale;

  public List<LangEntry> keys;
}