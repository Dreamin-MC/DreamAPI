package fr.dreamin.dreamapi.api.glowing.packet;

public final class PacketConstants {

  // ###############################################################
  // ------------------------ ENTITY FLAGS -------------------------
  // ###############################################################

  public static final byte GLOWING_FLAG = 1 << 6;
  public static final byte INVISIBILITY_FLAG = 1 << 5;

// ###############################################################
  // ---------------------- CRAFTBUKKIT CLASSES --------------------
  // ###############################################################

  public static final String CRAFT_ENTITY = "entity.CraftEntity";

  // ###############################################################
  // ------------------------- NMS CLASSES -------------------------
  // ###############################################################

  // Entity & Data
  public static final String NMS_ENTITY = "net.minecraft.world.entity.Entity";
  public static final String NMS_SYNCHED_DATA = "net.minecraft.network.syncher.SynchedEntityData";
  public static final String NMS_DATA_ACCESSOR = "net.minecraft.network.syncher.EntityDataAccessor";
  public static final String NMS_DATA_VALUE = "net.minecraft.network.syncher.SynchedEntityData$DataValue";
  public static final String NMS_DATA_SERIALIZER = "net.minecraft.network.syncher.EntityDataSerializer";

  // Packets
  public static final String NMS_PACKET = "net.minecraft.network.protocol.Packet";
  public static final String NMS_PACKET_METADATA = "net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket";
  public static final String NMS_PACKET_TEAM = "net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket";
  public static final String NMS_PACKET_TEAM_PARAMS = "net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket$Parameters";
  public static final String NMS_PACKET_ADD_ENTITY = "net.minecraft.network.protocol.game.ClientboundAddEntityPacket";
  public static final String NMS_PACKET_REMOVE_ENTITIES = "net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket";

  // Networking
  public static final String NMS_SERVER_PLAYER = "net.minecraft.server.level.ServerPlayer";
  public static final String NMS_SERVER_PACKET_LISTENER = "net.minecraft.server.network.ServerCommonPacketListenerImpl";
  public static final String NMS_CONNECTION = "net.minecraft.network.Connection";

  // Team
  public static final String NMS_PLAYER_TEAM = "net.minecraft.world.scores.PlayerTeam";
  public static final String NMS_SCOREBOARD = "net.minecraft.world.scores.Scoreboard";
  public static final String NMS_COLLISION_RULE = "net.minecraft.world.scores.Team$CollisionRule";
  public static final String NMS_VISIBILITY = "net.minecraft.world.scores.Team$Visibility";
  public static final String NMS_CHAT_FORMATTING = "net.minecraft.ChatFormatting";

  // Entity spawning
  public static final String NMS_ENTITY_TYPE = "net.minecraft.world.entity.EntityType";
  public static final String NMS_VEC3 = "net.minecraft.world.phys.Vec3";
  public static final String NMS_LEVEL = "net.minecraft.world.level.Level";

}
