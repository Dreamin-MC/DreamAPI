package fr.dreamin.dreamapi.api.worldborder.model;

import fr.dreamin.dreamapi.api.nms.packet.PacketReflection;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static fr.dreamin.dreamapi.api.worldborder.model.ConsumerSupplierTupel.of;

/**
 * The nms adapter impl for the world border
 */
public class WorldBorder extends AbstractWorldBorder {

    private final Object handle;

    /**
     * Ctor
     *
     * @param player the bukkit player
     */
    public WorldBorder(Player player) {
        this(PacketReflection.createWorldBorderHandle());
    }

    /**
     * Ctor
     *
     * @param world the bukkit world
     */
    public WorldBorder(World world) {
        this(PacketReflection.getWorldBorderHandle(world));
    }

    /**
     * Ctor
     *
     * @param worldBorder the nms world border
     */
    private WorldBorder(Object worldBorder) {
        super(
                of(
                        position -> PacketReflection.setWorldBorderCenter(worldBorder, position.x(), position.z()),
                        () -> new Position(PacketReflection.getWorldBorderCenterX(worldBorder), PacketReflection.getWorldBorderCenterZ(worldBorder))
                ),
                () -> new Position(PacketReflection.getWorldBorderMinX(worldBorder), PacketReflection.getWorldBorderMinZ(worldBorder)),
                () -> new Position(PacketReflection.getWorldBorderMaxX(worldBorder), PacketReflection.getWorldBorderMaxZ(worldBorder)),
                of(size -> PacketReflection.setWorldBorderSize(worldBorder, size), () -> PacketReflection.getWorldBorderSize(worldBorder)),
                of(buffer -> PacketReflection.setWorldBorderSafeZone(worldBorder, buffer), () -> PacketReflection.getWorldBorderSafeZone(worldBorder)),
                of(seconds -> PacketReflection.setWorldBorderWarningTime(worldBorder, seconds), () -> PacketReflection.getWorldBorderWarningTime(worldBorder)),
                of(blocks -> PacketReflection.setWorldBorderWarningBlocks(worldBorder, blocks), () -> PacketReflection.getWorldBorderWarningBlocks(worldBorder)),
                (oldSize, newSize, time, startTime) -> PacketReflection.lerpWorldBorderSize(worldBorder, oldSize, newSize, time)
        );
        this.handle = worldBorder;
    }

    @Override
    public void send(@NotNull Player player, @NotNull WorldBorderAction worldBorderAction) {
        try {
            final var packet = PacketReflection.createWorldBorderPacket(handle, worldBorderAction);
            PacketReflection.sendPacket(player, packet);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to send world border packet", e);
        }
    }
}