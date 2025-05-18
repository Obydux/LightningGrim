package ac.grim.grimac.events.packets.worldreader;

import ac.grim.grimac.api.packet.types.Packet;
import ac.grim.grimac.api.packet.types.SendablePacket;
import ac.grim.grimac.api.packet.types.event.PacketSendEvent;
import ac.grim.grimac.api.packet.world.chunk.HeightmapType;
import ac.grim.grimac.api.packet.world.chunk.PacketChunk;
import ac.grim.grimac.api.packet.world.chunk.v1_18.ChunkReaderV1_18;
import ac.grim.grimac.api.packet.world.chunk.v1_18.ChunkV1_18;
import ac.grim.grimac.api.packet.world.dimension.DimensionTypes;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public class PacketWorldReaderEighteen extends BasePacketWorldReader {

    private static final ChunkReaderV1_18 CHUNK_READER_V_1_18 = ChunkReaderV1_18.from();
    private static final boolean PRE_1_21_5 = PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_21_5);

    // Mojang decided to include lighting in this packet.  It's inefficient to read it, so we replace PacketEvents logic.
    @Override
    public void handleMapChunk(GrimPlayer player, PacketSendEvent event) {
        SendablePacket wrapper = SendablePacket.from(event);

        int x = wrapper.readInt();
        int z = wrapper.readInt();

        // Skip past heightmaps
        if (PRE_1_21_5)
            wrapper.readNBT();
        else
            wrapper.readMap(HeightmapType::read, Packet::readLongArray);

        // Use the new ChunkReader method that works with PacketWrapper directly
        PacketChunk[] chunks = CHUNK_READER_V_1_18.read(
                DimensionTypes.OVERWORLD, null, null, true, false, false,
                event.getUser().getTotalWorldHeight() >> 4,
                wrapper.readVarInt(), // Length of chunk data length (arrayLength) to pass to the new ChunkReader method
                wrapper
        );

        // Remove biomes to save memory
        for (int i = 0; i < chunks.length; i++) {
            ChunkV1_18 chunk = (ChunkV1_18) chunks[i];
            if (chunk != null) {
                // I know I'm passing null into @NotNull, but it shouldn't affect anything.
                chunks[i] = ChunkV1_18.from(chunk.getBlockCount(), chunk.getChunkData(), null);
            }
        }

        addChunkToCache(event, player, chunks, true, x, z);

        event.setLastUsedWrapper(null); // Prevent PacketEvents from using this incomplete wrapper later
    }
}
