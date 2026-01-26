package fr.dreamin.dreamapi.api.glowing.packet;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public final class MetadataInterceptor extends ChannelDuplexHandler {

  private final @NotNull Function<Integer, Byte> glowingFlagProvider;

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (!PacketReflection.getPacketMetadataClass().isInstance(msg)) {
      super.write(ctx, msg, promise);
      return;
    }

    try {
      final var entityId = PacketReflection.getMetadataEntityId(msg);
      final var customFlags = this.glowingFlagProvider.apply(entityId);

      if (customFlags == null) {
        super.write(ctx, msg, promise);
        return;
      }

      final var items = PacketReflection.getMetadataItems(msg);
      if (items == null || items.isEmpty()) {
        super.write(ctx, msg, promise);
        return;
      }

      var containsFlags = false;
      var edited = false;
      List<Object> modifiedItems = null;

      for (var i = 0; i < items.size(); i ++) {
        final var item = items.get(i);
        final var serializer = PacketReflection.getDataValueSerializer(item);
        final var watcherObject = PacketReflection.createAccessorFromSerializer(
          serializer,
          PacketReflection.getDataValueId(item)
        );

        if (watcherObject.equals(PacketReflection.getWatcherObjectFlags())) {
          containsFlags = true;
          final var currentFlags = PacketReflection.getDataValueValue(item);
          final var newFlags = (byte) (currentFlags | customFlags);

          if (newFlags != currentFlags) {
            edited = true;
            modifiedItems = new ArrayList<>(items);
            modifiedItems.set(i,
              PacketReflection.createDataValue(newFlags)
            );
          }
        }

        if (!edited && !containsFlags && customFlags != 0) {
          edited = true;
          modifiedItems = new ArrayList<>(items);
          modifiedItems.add(PacketReflection.createDataValue(customFlags));
        }

        if (edited) {
          final var newPacket = PacketReflection.createMetadataPacket(entityId, modifiedItems);
          super.write(ctx, newPacket, promise);
          return;
        }

      }

    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }

    super.write(ctx, msg, promise);
  }
}
