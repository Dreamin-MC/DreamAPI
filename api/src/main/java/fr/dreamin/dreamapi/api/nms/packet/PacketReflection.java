package fr.dreamin.dreamapi.api.nms.packet;

import io.netty.channel.Channel;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Centralized reflection access for NMS 1.21.10
 */
@UtilityClass
public class PacketReflection {

  private static boolean initialized = false;

  // ###############################################################
  // ----------------------- CRAFTBUKKIT ---------------------------
  // ###############################################################

  private static String craftPackage;
  private static Class<?> craftEntityClass;

  // ###############################################################
  // ---------------------- ENTITY & DATA --------------------------
  // ###############################################################

  private static Class<?> nmsEntityClass;
  private static Class<?> synchedDataClass;
  private static Class<?> dataAccessorClass;
  private static Class<?> dataValueClass;
  private static Class<?> dataSerializerClass;

  private static Method getHandleMethod;
  private static Method getEntityDataMethod;
  private static Method watcherGetMethod;
  private static Field dataSharedFlagsField;
  private static Object watcherObjectFlags;

  private static Method dataValueCreateMethod;
  private static Method dataValueIdMethod;
  private static Method dataValueSerializerMethod;
  private static Method dataValueValueMethod;
  private static Method serializerCreateAccessorMethod;

  // ###############################################################
  // ------------------------- PACKETS -----------------------------
  // ###############################################################

  private static Class<?> packetClass;
  private static Class<?> packetMetadataClass;
  private static Class<?> packetTeamClass;
  private static Class<?> packetTeamParamsClass;
  private static Class<?> packetAddEntityClass;
  private static Class<?> packetRemoveEntitiesClass;
  private static Class<?> packetBlockUpdateClass;

  private static Constructor<?> packetMetadataConstructor;
  private static Field packetMetadataEntityIdField;
  private static Field packetMetadataItemsField;

  private static Constructor<?> packetTeamConstructor;
  private static Constructor<?> packetTeamParamsConstructor;
  private static Constructor<?> packetAddEntityConstructor;
  private static Constructor<?> packetRemoveEntitiesConstructor;
  private static Constructor<?> packetBlockUpdateConstructor;

  // ###############################################################
  // ------------------------ NETWORKING ---------------------------
  // ###############################################################

  private static Class<?> serverPlayerClass;
  private static Class<?> serverPacketListenerClass;
  private static Class<?> connectionClass;

  private static Field playerConnectionField;
  private static Method sendPacketMethod;
  private static Field networkManagerField;
  private static Field channelField;

  // ###############################################################
  // --------------------------- TEAM ------------------------------
  // ###############################################################

  private static Class<?> playerTeamClass;
  private static Class<?> scoreboardClass;
  private static Class<?> collisionRuleClass;
  private static Class<?> visibilityClass;
  private static Class<?> chatFormattingClass;
  private static Class<?> componentClass;

  private static Constructor<?> playerTeamConstructor;
  private static Object scoreboardDummy;

  private static Method setTeamColorMethod;
  private static Method setCollisionRuleMethod;
  private static Method setNameTagVisibilityMethod;
  private static Method setAllowFriendlyFireMethod;
  private static Method setSeeFriendlyInvisiblesMethod;
  private static Method setTeamPrefixMethod;
  private static Method setTeamSuffixMethod;

  private static Method getColorConstantMethod;
  private static Method componentEmptyMethod;
  private static Method componentLiteralMethod;

  // ###############################################################
  // ---------------------- ENTITY SPAWNING ------------------------
  // ###############################################################

  private static Class<?> entityTypeClass;
  private static Class<?> vec3Class;

  private static Object shulkerEntityType;
  private static Object armorStandEntityType;
  private static Object zombieEntityType;
  private static Object villagerEntityType;
  private static Object playerEntityType;
  private static Object itemEntityType;

  private static Object vec3Zero;

  // ###############################################################
  // -------------------------- BLOCKS -----------------------------
  // ###############################################################

  private static Class<?> blockPosClass;
  private static Class<?> blockClass;
  private static Class<?> blockStateClass;

  private static Constructor<?> blockPosConstructor;

  // ###############################################################
  // ----------------------- INITIALIZATION ------------------------
  // ###############################################################

  public static synchronized void initialize() {
    if (initialized) return;

    try {
      loadClasses();
      loadMethods();
      loadFields();
      loadConstructors();
      loadConstants();

      initialized = true;
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize PacketReflection for 1.21.10", e);
    }
  }

  private static void loadClasses() throws ClassNotFoundException {
    // CraftBukkit
    craftPackage = Bukkit.getServer().getClass().getPackage().getName();
    craftEntityClass = Class.forName(craftPackage + "." + PacketConstants.CRAFT_ENTITY);

    // Entity & Data
    nmsEntityClass = Class.forName(PacketConstants.NMS_ENTITY);
    synchedDataClass = Class.forName(PacketConstants.NMS_SYNCHED_DATA);
    dataAccessorClass = Class.forName(PacketConstants.NMS_DATA_ACCESSOR);
    dataValueClass = Class.forName(PacketConstants.NMS_DATA_VALUE);
    dataSerializerClass = Class.forName(PacketConstants.NMS_DATA_SERIALIZER);

    // Packets
    packetClass = Class.forName(PacketConstants.NMS_PACKET);
    packetMetadataClass = Class.forName(PacketConstants.NMS_PACKET_METADATA);
    packetTeamClass = Class.forName(PacketConstants.NMS_PACKET_TEAM);
    packetTeamParamsClass = Class.forName(PacketConstants.NMS_PACKET_TEAM_PARAMS);
    packetAddEntityClass = Class.forName(PacketConstants.NMS_PACKET_ADD_ENTITY);
    packetRemoveEntitiesClass = Class.forName(PacketConstants.NMS_PACKET_REMOVE_ENTITIES);
    packetBlockUpdateClass = Class.forName(PacketConstants.NMS_PACKET_BLOCK_UPDATE);

    // Networking
    serverPlayerClass = Class.forName(PacketConstants.NMS_SERVER_PLAYER);
    serverPacketListenerClass = Class.forName(PacketConstants.NMS_SERVER_PACKET_LISTENER);
    connectionClass = Class.forName(PacketConstants.NMS_CONNECTION);

    // Team
    playerTeamClass = Class.forName(PacketConstants.NMS_PLAYER_TEAM);
    scoreboardClass = Class.forName(PacketConstants.NMS_SCOREBOARD);
    collisionRuleClass = Class.forName(PacketConstants.NMS_COLLISION_RULE);
    visibilityClass = Class.forName(PacketConstants.NMS_VISIBILITY);
    chatFormattingClass = Class.forName(PacketConstants.NMS_CHAT_FORMATTING);
    componentClass = Class.forName("net.minecraft.network.chat.Component");

    // Entity spawning
    entityTypeClass = Class.forName(PacketConstants.NMS_ENTITY_TYPE);
    vec3Class = Class.forName(PacketConstants.NMS_VEC3);

    // Blocks
    blockClass = Class.forName(PacketConstants.NMS_BLOCK);
    blockPosClass = Class.forName(PacketConstants.NMS_BLOCK_POS);
    blockStateClass = Class.forName(PacketConstants.NMS_BLOCK_STATE);
  }

  private static void loadMethods() throws NoSuchMethodException {
    // Entity & Data
    getHandleMethod = getAccessibleMethod(craftEntityClass, "getHandle");
    getEntityDataMethod = getAccessibleMethod(nmsEntityClass, "getEntityData");
    watcherGetMethod = getAccessibleMethod(synchedDataClass, "get", dataAccessorClass);

    // Data values
    dataValueCreateMethod = getAccessibleMethod(dataValueClass, "create", dataAccessorClass, Object.class);
    dataValueIdMethod = getAccessibleMethod(dataValueClass, "id");
    dataValueSerializerMethod = getAccessibleMethod(dataValueClass, "serializer");
    dataValueValueMethod = getAccessibleMethod(dataValueClass, "value");
    serializerCreateAccessorMethod = getAccessibleMethod(dataSerializerClass, "createAccessor", int.class);

    // Networking
    sendPacketMethod = getAccessibleMethod(serverPacketListenerClass, "send", packetClass);

    // Team
    setTeamColorMethod = getAccessibleMethod(playerTeamClass, "setColor", chatFormattingClass);
    setCollisionRuleMethod = getAccessibleMethod(playerTeamClass, "setCollisionRule", collisionRuleClass);
    setNameTagVisibilityMethod = getAccessibleMethod(playerTeamClass, "setNameTagVisibility", visibilityClass);
    setAllowFriendlyFireMethod = getAccessibleMethod(playerTeamClass, "setAllowFriendlyFire", boolean.class);
    setSeeFriendlyInvisiblesMethod = getAccessibleMethod(playerTeamClass, "setSeeFriendlyInvisibles", boolean.class);
    setTeamPrefixMethod = getAccessibleMethod(playerTeamClass, "setPlayerPrefix", componentClass);
    setTeamSuffixMethod = getAccessibleMethod(playerTeamClass, "setPlayerSuffix", componentClass);

    // Chat formatting & Component
    getColorConstantMethod = getAccessibleMethod(chatFormattingClass, "getByCode", char.class);
    componentEmptyMethod = getAccessibleMethod(componentClass, "empty");
    componentLiteralMethod = getAccessibleMethod(componentClass, "literal", String.class);
  }

  private static void loadFields() throws NoSuchFieldException, IllegalAccessException {
    // Entity flags
    dataSharedFlagsField = getAccessibleField(nmsEntityClass, "DATA_SHARED_FLAGS_ID");
    watcherObjectFlags = dataSharedFlagsField.get(null);

    // Packet metadata
    packetMetadataEntityIdField = getAccessibleField(packetMetadataClass, "id");
    packetMetadataItemsField = getAccessibleField(packetMetadataClass, "packedItems");

    // Networking
    playerConnectionField = getAccessibleField(serverPlayerClass, "connection");
    networkManagerField = getAccessibleField(serverPacketListenerClass, "connection");
    channelField = getAccessibleField(connectionClass, "channel");
  }

  private static void loadConstructors() throws NoSuchMethodException {
    // Packets
    packetMetadataConstructor = getAccessibleConstructor(packetMetadataClass, int.class, List.class);
    packetTeamConstructor = getAccessibleConstructor(packetTeamClass,
      String.class, int.class, Optional.class, java.util.Collection.class);
    packetTeamParamsConstructor = getAccessibleConstructor(packetTeamParamsClass, playerTeamClass);

    // Team
    playerTeamConstructor = getAccessibleConstructor(playerTeamClass, scoreboardClass, String.class);

    // Entity spawning
    packetAddEntityConstructor = getAccessibleConstructor(packetAddEntityClass,
      int.class, UUID.class, double.class, double.class, double.class,
      float.class, float.class, entityTypeClass, int.class, vec3Class, double.class);
    packetRemoveEntitiesConstructor = getAccessibleConstructor(packetRemoveEntitiesClass, int[].class);

    // Block packets
    blockPosConstructor = getAccessibleConstructor(blockPosClass, int.class, int.class, int.class);
    packetBlockUpdateConstructor = getAccessibleConstructor(packetBlockUpdateClass, blockPosClass, blockStateClass);
  }

  private static void loadConstants() throws Exception {
    // Scoreboard dummy
    scoreboardDummy = getAccessibleConstructor(scoreboardClass).newInstance();

    // Entity type
    shulkerEntityType = getAccessibleField(entityTypeClass, "SHULKER").get(null);
    armorStandEntityType = getAccessibleField(entityTypeClass, "ARMOR_STAND").get(null);
    zombieEntityType = getAccessibleField(entityTypeClass, "ZOMBIE").get(null);
    villagerEntityType = getAccessibleField(entityTypeClass, "VILLAGER").get(null);
    playerEntityType = getAccessibleField(entityTypeClass, "PLAYER").get(null);
    itemEntityType = getAccessibleField(entityTypeClass, "ITEM").get(null);

    // Vec3 zero
    vec3Zero = getAccessibleConstructor(vec3Class, double.class, double.class, double.class)
      .newInstance(0d, 0d, 0d);
  }

  // ###############################################################
  // ---------------------- PUBLIC ACCESSORS -----------------------
  // ###############################################################

  public static @NotNull Object getNmsEntity(final @NotNull Entity entity) throws ReflectiveOperationException {
    ensureInitialized();
    return getHandleMethod.invoke(entity);
  }

  public static byte getEntityFlags(final @NotNull Object nmsEntity) throws ReflectiveOperationException {
    ensureInitialized();
    final var dataWatcher = getEntityDataMethod.invoke(nmsEntity);
    return (byte) watcherGetMethod.invoke(dataWatcher, watcherObjectFlags);
  }

  public static @NotNull Object createDataValue(final byte flags) throws ReflectiveOperationException {
    ensureInitialized();
    return dataValueCreateMethod.invoke(null, watcherObjectFlags, flags);
  }

  public static @NotNull Object createMetadataPacket(final int entityId, final @NotNull List<Object> dataValues)
    throws ReflectiveOperationException {
    ensureInitialized();
    return packetMetadataConstructor.newInstance(entityId, dataValues);
  }

  public static @NotNull Object createTeamPacket(final @NotNull String teamName, final int mode,
                                                 final @NotNull Optional<Object> params, final @NotNull java.util.Collection<String> entities)
    throws ReflectiveOperationException {
    ensureInitialized();
    return packetTeamConstructor.newInstance(teamName, mode, params, entities);
  }

  public static @NotNull Object createTeam(final @NotNull String teamName) throws ReflectiveOperationException {
    ensureInitialized();
    return playerTeamConstructor.newInstance(scoreboardDummy, teamName);
  }

  public static @NotNull Object createTeamParams(final @NotNull Object team) throws ReflectiveOperationException {
    ensureInitialized();
    return packetTeamParamsConstructor.newInstance(team);
  }

  public static void configureTeam(final @NotNull Object team, final char colorCode, final @NotNull String collisionRule, final @NotNull String visibility, final boolean friendlyFire, final boolean seeInvisibles) throws ReflectiveOperationException {
    ensureInitialized();

    final var colorConstant = getColorConstantMethod.invoke(null, colorCode);
    final var collisionConstant = getEnumConstant(collisionRuleClass, convertTeamOptionsToEnumName(collisionRule));
    final var visibilityConstant = getEnumConstant(visibilityClass, convertTeamOptionsToEnumName(visibility));

    setTeamColorMethod.invoke(team, colorConstant);
    setCollisionRuleMethod.invoke(team, collisionConstant);
    setNameTagVisibilityMethod.invoke(team, visibilityConstant);
    setAllowFriendlyFireMethod.invoke(team, friendlyFire);
    setSeeFriendlyInvisiblesMethod.invoke(team, seeInvisibles);
  }

  /**
   * Set team prefix and suffix (use empty strings to prevent tab list coloring)
   */
  public static void setTeamPrefixSuffix(final @NotNull Object team, final @NotNull String prefix, final @NotNull String suffix) throws ReflectiveOperationException {
    ensureInitialized();

    // Create empty components
    final var prefixComponent = prefix.isEmpty()
      ? componentEmptyMethod.invoke(null)
      : componentLiteralMethod.invoke(null, prefix);

    final var suffixComponent = suffix.isEmpty()
      ? componentEmptyMethod.invoke(null)
      : componentLiteralMethod.invoke(null, suffix);

    setTeamPrefixMethod.invoke(team, prefixComponent);
    setTeamSuffixMethod.invoke(team, suffixComponent);
  }

  public static @NotNull Object createAddShulkerEntityPacket(final int entityId, final @NotNull UUID uuid, final @NotNull Location location) throws ReflectiveOperationException {
    ensureInitialized();
    return packetAddEntityConstructor.newInstance(
      entityId, uuid, location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw(), shulkerEntityType, 0, vec3Zero, 0d
    );
  }

  public static @NotNull Object createSpawnEntityPacket(final int entityId, final @NotNull UUID uuid, final @NotNull EntityType type, final @NotNull Location location) throws ReflectiveOperationException {
    ensureInitialized();

    final var nmsEntityType = getNmsEntityType(type);

    return packetAddEntityConstructor.newInstance(
      entityId,
      uuid,
      location.getX(), location.getY(), location.getZ(),
      location.getPitch(), location.getYaw(),
      nmsEntityType,
      0,
      vec3Zero,
      0d
    );
  }

  public static @NotNull Object createRemoveEntitiesPacket(final int... entityIds) throws ReflectiveOperationException {
    ensureInitialized();
    return packetRemoveEntitiesConstructor.newInstance((Object) entityIds);
  }

  public static @NotNull Object createBlockChangePacket(final @NotNull Location location, final @NotNull Material type) throws ReflectiveOperationException {
    ensureInitialized();

    final var blockPos = blockPosConstructor.newInstance(
      location.getBlockX(),
      location.getBlockY(),
      location.getBlockZ()
    );

    final var craftMagicNumbers = Class.forName(craftPackage + ".util.CraftMagicNumbers");
    final var getBlockMethod = getAccessibleMethod(craftMagicNumbers, "getBlock", Material.class);
    final var nmsBlock = getBlockMethod.invoke(null, type);

    final var defaultBlockStateMethod = getAccessibleMethod(blockClass, "defaultBlockState");
    final var blockState = defaultBlockStateMethod.invoke(nmsBlock);

    return packetBlockUpdateConstructor.newInstance(blockPos, blockState);
  }

  public static void sendPacket(final @NotNull Player player, final @NotNull Object packet) throws ReflectiveOperationException {
    ensureInitialized();
    final var nmsPlayer = getHandleMethod.invoke(player);
    final var connection = playerConnectionField.get(nmsPlayer);
    sendPacketMethod.invoke(connection, packet);
  }

  public static @NotNull Channel getChannel(final @NotNull Player player) throws ReflectiveOperationException {
    ensureInitialized();
    final var nmsPlayer = getHandleMethod.invoke(player);
    final var connection = playerConnectionField.get(nmsPlayer);
    final var networkManager = networkManagerField.get(connection);
    return (Channel) channelField.get(networkManager);
  }

  // ###############################################################
  // ------------------- METADATA PACKET ACCESS --------------------
  // ###############################################################

  public static int getMetadataEntityId(final @NotNull Object packet) throws ReflectiveOperationException {
    ensureInitialized();
    return packetMetadataEntityIdField.getInt(packet);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull List<Object> getMetadataItems(final @NotNull Object packet)
    throws ReflectiveOperationException {
    ensureInitialized();
    return (List<Object>) packetMetadataItemsField.get(packet);
  }

  public static int getDataValueId(final @NotNull Object dataValue) throws ReflectiveOperationException {
    ensureInitialized();
    return (int) dataValueIdMethod.invoke(dataValue);
  }

  public static @NotNull Object getDataValueSerializer(final @NotNull Object dataValue) throws ReflectiveOperationException {
    ensureInitialized();
    return dataValueSerializerMethod.invoke(dataValue);
  }

  public static byte getDataValueValue(final @NotNull Object dataValue) throws ReflectiveOperationException {
    ensureInitialized();
    return (byte) dataValueValueMethod.invoke(dataValue);
  }

  public static @NotNull Object createAccessorFromSerializer(final @NotNull Object serializer, final int id)
    throws ReflectiveOperationException {
    ensureInitialized();
    return serializerCreateAccessorMethod.invoke(serializer, id);
  }

  // ###############################################################
  // -------------------------- GETTERS ----------------------------
  // ###############################################################

  public static @NotNull Object getWatcherObjectFlags() {
    ensureInitialized();
    return watcherObjectFlags;
  }

  public static @NotNull Class<?> getPacketMetadataClass() {
    ensureInitialized();
    return packetMetadataClass;
  }

  // ###############################################################
  // -------------------------- HELPERS ----------------------------
  // ###############################################################

  private static @NotNull Field getAccessibleField(final @NotNull Class<?> clazz, final @NotNull String name)
    throws NoSuchFieldException {
    final var field = clazz.getDeclaredField(name);
    field.setAccessible(true);
    return field;
  }

  private static @NotNull Method getAccessibleMethod(final @NotNull Class<?> clazz, final @NotNull String name, final @NotNull Class<?>... parameterTypes)
    throws NoSuchMethodException {
    final var method = clazz.getDeclaredMethod(name, parameterTypes);
    method.setAccessible(true);
    return method;
  }

  private static @NotNull Constructor<?> getAccessibleConstructor(final @NotNull Class<?> clazz, final @NotNull Class<?>... parameterTypes)
    throws NoSuchMethodException {
    final var constructor = clazz.getDeclaredConstructor(parameterTypes);
    constructor.setAccessible(true);
    return constructor;
  }

  private static @NotNull Object getEnumConstant(final @NotNull Class<?> enumClass, final @NotNull String name)
    throws ReflectiveOperationException {
    final var constants = enumClass.getEnumConstants();
    for (final var constant : constants) {
      if (((Enum<?>) constant).name().equals(name)) {
        return constant;
      }
    }
    throw new IllegalArgumentException("No enum constant " + enumClass.getName() + "." + name);
  }

  private static @NotNull String convertTeamOptionsToEnumName(final @NotNull String nmsName) {
    return switch (nmsName) {
      case "always" -> "ALWAYS";
      case "never" -> "NEVER";
      case "pushOtherTeams" -> "PUSH_OTHER_TEAMS";
      case "pushOwnTeam" -> "PUSH_OWN_TEAM";
      case "hideForOtherTeams" -> "HIDE_FOR_OTHER_TEAMS";
      case "hideForOwnTeam" -> "HIDE_FOR_OWN_TEAM";
      default -> throw new IllegalArgumentException("Unknown NMS name: " + nmsName);
    };
  }

  private static @NotNull Object getNmsEntityType(final @NotNull EntityType type) throws ReflectiveOperationException {
    return switch(type) {
      case SHULKER -> shulkerEntityType;
      case ARMOR_STAND -> armorStandEntityType;
      case ZOMBIE -> zombieEntityType;
      case VILLAGER -> villagerEntityType;
      case PLAYER -> playerEntityType;
      case ITEM -> itemEntityType;
      default -> throw new IllegalArgumentException("Unknown NMS entity type: " + type);
    };
  }

  private static void ensureInitialized() {
    if (!initialized)
      throw new IllegalStateException("PacketReflection not initialized. Call initialize() first.");
  }
}
