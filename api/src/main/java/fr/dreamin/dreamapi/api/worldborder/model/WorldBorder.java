package fr.dreamin.dreamapi.api.worldborder.model;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The nms adapter impl for the world border
 */
public class WorldBorder extends AbstractWorldBorder {

    private final net.minecraft.world.level.border.WorldBorder handle;

    /**
     * Ctor
     *
     * @param player the bukkit player
     */
    public WorldBorder(Player player) {
        this(new net.minecraft.world.level.border.WorldBorder());
        this.handle.world = ((CraftWorld) player.getWorld()).getHandle();
    }

    /**
     * Ctor
     *
     * @param world the bukkit world
     */
    public WorldBorder(World world) {
        this(((CraftWorld) world).getHandle().getWorldBorder());
    }

    /**
     * Ctor
     *
     * @param worldBorder the nms world border
     */
    public WorldBorder(net.minecraft.world.level.border.WorldBorder worldBorder) {
        super(
                of(
                        position -> worldBorder.setCenter(position.x(), position.z()),
                        () -> new Position(worldBorder.getCenterX(), worldBorder.getCenterZ())
                ),
                () -> new Position(worldBorder.getMinX(), worldBorder.getMinZ()),
                () -> new Position(worldBorder.getMaxX(), worldBorder.getMaxZ()),
                of(worldBorder::setSize, worldBorder::getSize),
                of(worldBorder::setSafeZone, worldBorder::getSafeZone),
                of(worldBorder::setWarningTime, worldBorder::getWarningTime),
                of(worldBorder::setWarningBlocks, worldBorder::getWarningBlocks),
                worldBorder::lerpSizeBetween
        );
        this.handle = worldBorder;
    }

    @Override
    public void send(@NotNull Player player, @NotNull WorldBorderAction worldBorderAction) {
        var packet = switch (worldBorderAction) {
            case INITIALIZE -> new ClientboundInitializeBorderPacket(handle);
            case LERP_SIZE -> new ClientboundSetBorderLerpSizePacket(handle);
            case SET_CENTER -> new ClientboundSetBorderCenterPacket(handle);
            case SET_SIZE -> new ClientboundSetBorderSizePacket(handle);
            case SET_WARNING_BLOCKS -> new ClientboundSetBorderWarningDistancePacket(handle);
            case SET_WARNING_TIME -> new ClientboundSetBorderWarningDelayPacket(handle);
        };

        ((CraftPlayer) player).getHandle().connection.send(packet);
    }
}