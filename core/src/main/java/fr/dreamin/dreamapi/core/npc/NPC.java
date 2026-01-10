package fr.dreamin.dreamapi.core.npc;

import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.*;
import net.citizensnpcs.trait.waypoint.Waypoints;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class NPC {

  public enum NavigationType { DEFAULT, STRAIGHT_LINEAR }

  @Getter
  private static final NPCRegistry registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());

  private final @NotNull net.citizensnpcs.api.npc.NPC npc;

  public static NPC builder(final @NotNull EntityType type, final @NotNull String name, final @NotNull Location location) {
    return new NPC(type, name, location);
  }

  public static NPC builder(final @NotNull EntityType type, final @NotNull String name) {
    return new NPC(type, name);
  }

  public static NPC builder(final @NotNull String name) {
    return new NPC(EntityType.PLAYER, name);
  }

  private NPC(final @NotNull EntityType type, final @NotNull String name, final @NotNull Location location) {
    this.npc = registry.createNPC(type, name, location);
  }

  private NPC(final @NotNull EntityType type, final @NotNull String name) {
    this.npc = registry.createNPC(type, name);
  }

  private NPC(final @NotNull String name) {
    this.npc = registry.createNPC(EntityType.PLAYER, name);
  }

  private NPC(final @NotNull net.citizensnpcs.api.npc.NPC npc) {
    this.npc = npc;
  }

  // ###############################################################
  // -------------------------- LOCATION ---------------------------
  // ###############################################################

  /**
   * Spawns the NPC at the given location.
   *
   * @param location Location to spawn the NPC.
   * @return this (NpcBuilder) for chaining.
   */
  public NPC spawn(final @NotNull Location location) {
    this.npc.spawn(location);
    return this;
  }

  /**
   * Teleports the NPC to the given location if it is spawned.
   *
   * @param location Location to teleport to.
   * @return this (NpcBuilder) for chaining.
   */
  public NPC teleport(final @NotNull Location location) {
    if (this.npc.isSpawned()) this.npc.getEntity().teleport(location);
    else this.npc.spawn(location);
    return this;
  }

  // ###############################################################
  // ---------------------------- DATA -----------------------------
  // ###############################################################

  public NPC setName(final @NotNull String name) {
    this.npc.setName(name);
    return this;
  }

  public NPC setEntityType(final @NotNull EntityType type) {
    this.npc.setBukkitEntityType(type);
    return this;
  }

  public NPC setNameplateVisible(final boolean visible) {
    this.npc.data().setPersistent(net.citizensnpcs.api.npc.NPC.Metadata.NAMEPLATE_VISIBLE, visible);
    return this;
  }

  public NPC setFlyable(final boolean flyable) {
    this.npc.setFlyable(flyable);
    return this;
  }

  public NPC setMinecraftIA(final boolean useAI) {
    this.npc.setUseMinecraftAI(useAI);
    return this;
  }

  public NPC setCollidable(final boolean collidable) {
    this.npc.data().setPersistent(net.citizensnpcs.api.npc.NPC.Metadata.COLLIDABLE, collidable);
    return this;
  }

  public NPC setGravity(final boolean hasGravity) {
    this.npc.getOrAddTrait(Gravity.class).setHasGravity(hasGravity);
    return this;
  }

  public NPC setInvulnerable(final boolean invulnerable) {
    if (this.npc.isSpawned()) {
      this.npc.getEntity().setInvulnerable(invulnerable);
    }
    return this;
  }

  public NPC setProtected(final boolean isProtected) {
    this.npc.setProtected(isProtected);
    return this;
  }

  public NPC setSneaking(boolean sneaking) {
    this.npc.setSneaking(sneaking);
    return this;
  }

  public NPC setWanderer(final boolean wanderer) {
    final var waypoints = this.npc.getOrAddTrait(Waypoints.class);
    waypoints.setWaypointProvider(wanderer ? "wander" : "linear");
    return this;
  }

  // ###############################################################
  // ------------------------- NAVIGATION --------------------------
  // ###############################################################

  public NPC navigateTo(final @NotNull Location location, final @NotNull NavigationType navigationType) {
    if (this.npc.getNavigator().canNavigateTo(location)) {
      if (navigationType == NavigationType.DEFAULT) this.npc.getNavigator().setTarget(location);
      else this.npc.getNavigator().setStraightLineTarget(location);
    }
    return this;
  }

  public NPC navigateTo(final @NotNull Entity entity, final boolean aggressive, final @NotNull NavigationType navigationType) {
    if (this.npc.getNavigator().canNavigateTo(entity.getLocation())) {
      if (navigationType == NavigationType.DEFAULT) this.npc.getNavigator().setTarget(entity, aggressive);
      else this.npc.getNavigator().setStraightLineTarget(entity, aggressive);
    }
    return this;
  }

  public NPC stopNavigation() {
    if (this.npc.getNavigator().isNavigating()) this.npc.getNavigator().cancelNavigation();
    return this;
  }

  public NPC setFollow(final @NotNull Entity entity) {
    final var followTrait = this.npc.getOrAddTrait(FollowTrait.class);
    followTrait.follow(entity);
    return this;
  }

  public NPC stopFollow() {
    if (this.npc.hasTrait(FollowTrait.class)) this.npc.removeTrait(FollowTrait.class);
    return this;
  }

  // ###############################################################
  // -------------------------- PASSENGER --------------------------
  // ###############################################################

  public NPC setPassenger(final @NotNull Entity entity) {
    if (this.npc.isSpawned()) this.npc.getEntity().setPassenger(entity);
    return this;
  }

  public NPC addPassenger(final @NotNull Entity entity) {
    if (this.npc.isSpawned()) this.npc.getEntity().addPassenger(entity);
    return this;
  }

  public NPC clearPassengers() {
    if (this.npc.isSpawned()) this.npc.getEntity().getPassengers().clear();
    return this;
  }

  // ###############################################################
  // -------------------------- EQUIPMENT --------------------------
  // ###############################################################

  public NPC setEquipment(final @NotNull Equipment.EquipmentSlot slot, final @NotNull ItemStack item) {
    final var equipment = this.npc.getOrAddTrait(Equipment.class);
    equipment.set(slot, item);
    return this;
  }

  // ###############################################################
  // ---------------------------- SKIN -----------------------------
  // ###############################################################

  public NPC setSkin(String playerName) {
    this.npc.getOrAddTrait(SkinTrait.class).setSkinName(playerName);
    return this;
  }

  public NPC setSkin(String value, String signature) {
    this.npc.getOrAddTrait(SkinTrait.class).setTexture(value, signature);
    return this;
  }

  // ###############################################################
  // ---------------------------- UTILS ----------------------------
  // ###############################################################

  public NPC setRideable(final boolean rideable) {
    final var controllable = this.npc.getOrAddTrait(Controllable.class);
    controllable.setEnabled(rideable);
    return this;
  }

  public NPC setRideable(final boolean rideable, final @NotNull Controllable.BuiltInControls controls) {
    final var controllable = this.npc.getOrAddTrait(Controllable.class);
    controllable.setEnabled(rideable);
    controllable.setControls(controls);
    return this;
  }

  public NPC setSitting(final @NotNull Location location) {
    final var sitTrait = this.npc.getOrAddTrait(SitTrait.class);
    sitTrait.setSitting(location);
    return this;
  }

  public NPC stopSitting() {
    if (this.npc.hasTrait(SitTrait.class)) this.npc.removeTrait(SitTrait.class);
    return this;
  }

  public NPC setSleeping(final @NotNull Location location) {
    final var sleepTrait = this.npc.getOrAddTrait(SleepTrait.class);
    sleepTrait.setSleeping(location);
    return this;
  }

  public NPC stopSleeping() {
    if (this.npc.hasTrait(SleepTrait.class)) this.npc.removeTrait(SleepTrait.class);
    return this;
  }

  public NPC setSpeed(float speed) {
    this.npc.getNavigator().getDefaultParameters().speedModifier(speed);
    return this;
  }

  /**
   * Sets the rotation of the NPC if it is spawned.
   *
   * @param yaw   Yaw value for the NPC's rotation.
   * @param pitch Pitch value for the NPC's rotation.
   * @return this (NpcBuilder) for chaining.
   */
  public NPC setRotation(final float yaw, final float pitch) {
    if (this.npc.isSpawned()) this.npc.getEntity().setRotation(yaw, pitch);
    return this;
  }

  // ###############################################################
  // --------------------------- BUILD -----------------------------
  // ###############################################################

  /**
   * Builds and returns the NPC.
   *
   * @return The NPC instance.
   */
  public net.citizensnpcs.api.npc.NPC build() {
    return this.npc;
  }

}
