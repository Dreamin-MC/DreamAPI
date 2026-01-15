package fr.dreamin.dreamapi.api.world;

import fr.dreamin.dreamapi.api.config.Configurations;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public final class WorldDefinition {

  private final @NotNull String label;
  private final @NotNull World.Environment environment;
  private final @NotNull WorldType type;
  private final @Nullable Long seed;
  private final boolean generateStructures;
  private final @Nullable String customGenerationName;
  private final @Nullable BiomeProviderDefinition biomeProvider;
  private final @NotNull List<FlatWorldLayer> flatWorldLayers;
  private final @Nullable String flatWorldDefaultBiome;
  private final boolean flatWorldLakes;
  private final boolean flatWorldFeatures;

  private WorldDefinition(final @NotNull Builder builder) {
    this.label = builder.label;
    this.environment = builder.environment;
    this.type = builder.type;
    this.seed = builder.seed;
    this.generateStructures = builder.generateStructures;
    this.customGenerationName = builder.customGenerationName;
    this.biomeProvider = builder.biomeProvider;
    this.flatWorldLayers = builder.flatWorldLayers != null ? List.copyOf(builder.flatWorldLayers) : Collections.emptyList();
    this.flatWorldDefaultBiome = builder.flatWorldDefaultBiome;
    this.flatWorldLakes = builder.flatWorldLakes;
    this.flatWorldFeatures = builder.flatWorldFeatures;

    if (this.type == WorldType.FLAT) {
      if (this.flatWorldLayers.isEmpty())
        throw new IllegalArgumentException("Flat world layers cannot be empty");

      if (this.flatWorldDefaultBiome == null || this.flatWorldDefaultBiome.isBlank())
        throw new IllegalArgumentException("Flat world default biome cannot be null or blank.");

    }
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public @Nullable String buildFlatWorldGeneratorSettingsJson() {
    if (this.type != WorldType.FLAT)
      return null;

    final var root = Configurations.MAPPER.createObjectNode();
    final var settings = root.putObject("settings");

    settings.put("biome", this.flatWorldDefaultBiome);

    final var layersNode = settings.putArray("layers");
    for (final var layer : this.flatWorldLayers) {
      final var layerNode = layersNode.addObject();
      layerNode.put("block", layer.getBlock());
      layerNode.put("height", layer.getHeight());
    }

    settings.put("features", this.flatWorldFeatures);
    settings.put("lakes", this.flatWorldLakes);

    try {
      return Configurations.MAPPER.writeValueAsString(root);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to build flat world generator settings JSON", e);
    }

  }

  public @Nullable String getGeneratorSettingsJson() {
    return switch (this.type) {
      case FLAT -> buildFlatWorldGeneratorSettingsJson();
      default -> null;
    };
  }

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  public static Builder builder(final @NotNull String label) {
    return new Builder(label);
  }

  public static final class Builder {
    private @NotNull String label;
    private @NotNull World.Environment environment = World.Environment.NORMAL;
    private @NotNull WorldType type = WorldType.NORMAL;
    private @Nullable Long seed = null;
    private boolean generateStructures = true;
    private @Nullable String customGenerationName = null;
    private @Nullable BiomeProviderDefinition biomeProvider = null;
    private @Nullable List<FlatWorldLayer> flatWorldLayers = new ArrayList<>();
    private @Nullable String flatWorldDefaultBiome = null;
    private boolean flatWorldLakes = false;
    private boolean flatWorldFeatures = false;

    private Builder(final @NotNull String label) {
      this.label = label;
    }

    // ###############################################################
    // ----------------------- PUBLIC METHODS ------------------------
    // ###############################################################

    public Builder environment(final @NotNull World.Environment environment) {
      this.environment = environment;
      return this;
    }

    public Builder type(final @NotNull WorldType type) {
      this.type = type;
      return this;
    }

    public Builder seed(final @Nullable Long seed) {
      this.seed = seed;
      return this;
    }

    public Builder generateStructures(final boolean generateStructures) {
      this.generateStructures = generateStructures;
      return this;
    }

    public Builder customGenerationName(final @Nullable String customGenerationName) {
      this.customGenerationName = customGenerationName;
      this.type = WorldType.NORMAL;
      return this;
    }

    public Builder biomeProvider(final @Nullable BiomeProviderDefinition biomeProvider) {
      this.biomeProvider = biomeProvider;
      return this;
    }

    public Builder flatWorld(final @NotNull String defaultBiome, final @NotNull List<FlatWorldLayer> layers) {
      this.type = WorldType.FLAT;
      this.flatWorldDefaultBiome = defaultBiome;
      this.flatWorldLayers = layers;
      this.generateStructures = true;
      this.flatWorldLakes = false;

      return this;
    }

    public Builder flatWorld(final @NotNull String defaultBiome, final @NotNull List<FlatWorldLayer> layers, final boolean generateLakes, final boolean generateFeatures) {
      this.type = WorldType.FLAT;
      this.flatWorldDefaultBiome = defaultBiome;
      this.flatWorldLayers = layers;
      this.generateStructures = true;
      this.flatWorldLakes = generateLakes;
      this.flatWorldFeatures = generateFeatures;

      return this;
    }

    public Builder voidWorld() {
      this.type = WorldType.FLAT;
      this.flatWorldDefaultBiome = "minecraft:the_void";
      this.flatWorldLayers = List.of(FlatWorldLayer.of("minecraft:air", 1));
      this.generateStructures = false;
      this.flatWorldLakes = false;
      this.flatWorldFeatures = false;

      return this;
    }

    public WorldDefinition build() {
      return new WorldDefinition(this);
    }

  }

}
