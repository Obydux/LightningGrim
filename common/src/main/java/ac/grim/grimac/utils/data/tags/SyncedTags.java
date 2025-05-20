package ac.grim.grimac.utils.data.tags;

import ac.grim.grimac.api.packet.ResourceLocationI;
import ac.grim.grimac.api.packet.item.PacketStateType;
import ac.grim.grimac.api.packet.protocol.PacketClientVersion;
import ac.grim.grimac.api.packet.protocol.PacketClientVersions;
import ac.grim.grimac.api.packet.protocol.version.server.ServerVersion;
import ac.grim.grimac.api.packet.protocol.version.server.ServerVersions;
import ac.grim.grimac.api.packet.types.server.play.ServerTagsPacket;
import ac.grim.grimac.api.packet.world.PacketStateTypes;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.api.packet.world.blocktags.BlockTags;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTags;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This class stores tags that the client is aware of.
 */
public final class SyncedTags {

    public static final ResourceLocationI CLIMBABLE = ResourceLocationI.minecraft("climbable");
    public static final ResourceLocationI MINEABLE_AXE = ResourceLocationI.minecraft("mineable/axe");
    public static final ResourceLocationI MINEABLE_PICKAXE = ResourceLocationI.minecraft("mineable/pickaxe");
    public static final ResourceLocationI MINEABLE_SHOVEL = ResourceLocationI.minecraft("mineable/shovel");
    public static final ResourceLocationI MINEABLE_HOE = ResourceLocationI.minecraft("mineable/hoe");
    public static final ResourceLocationI NEEDS_DIAMOND_TOOL = ResourceLocationI.minecraft("needs_diamond_tool");
    public static final ResourceLocationI NEEDS_IRON_TOOL = ResourceLocationI.minecraft("needs_iron_tool");
    public static final ResourceLocationI NEEDS_STONE_TOOL = ResourceLocationI.minecraft("needs_stone_tool");
    public static final ResourceLocationI SWORD_EFFICIENT = ResourceLocationI.minecraft("sword_efficient");
    private static final ServerVersion VERSION = ServerVersions.getServerVersion();
    private static final ResourceLocationI BLOCK = VERSION.isNewerThanOrEquals(ServerVersions.V_1_21) ? ResourceLocationI.minecraft("block") : ResourceLocationI.minecraft("blocks");
    private final GrimPlayer player;
    private final Map<ResourceLocationI, Map<ResourceLocationI, SyncedTag<?>>> synced;

    public SyncedTags(GrimPlayer player) {
        this.player = player;
        this.synced = new HashMap<>();
        PacketClientVersion version = player.getClientVersion();
        trackTags(BLOCK, id -> PacketStateTypes.getById(VERSION.toClientVersion(), id),
                // // TODO (Packet Rewrite) fix hacky cast
                SyncedTag.<PacketStateType>builder(CLIMBABLE).defaults(BlockTags.CLIMBABLE.getStates()).supported(version.isNewerThanOrEquals(PacketClientVersions.V_1_16)),
                SyncedTag.<PacketStateType>builder(MINEABLE_AXE).defaults(BlockTags.MINEABLE_AXE.getStates()).supported(version.isNewerThanOrEquals(PacketClientVersions.V_1_17)),
                SyncedTag.<PacketStateType>builder(MINEABLE_PICKAXE).defaults(BlockTags.MINEABLE_PICKAXE.getStates()).supported(version.isNewerThanOrEquals(PacketClientVersions.V_1_17)),
                SyncedTag.<PacketStateType>builder(MINEABLE_SHOVEL).defaults(BlockTags.MINEABLE_SHOVEL.getStates()).supported(version.isNewerThanOrEquals(PacketClientVersions.V_1_17)),
                SyncedTag.<PacketStateType>builder(MINEABLE_HOE).defaults(BlockTags.MINEABLE_HOE.getStates()).supported(version.isNewerThanOrEquals(PacketClientVersions.V_1_17)),
                SyncedTag.<PacketStateType>builder(NEEDS_DIAMOND_TOOL).defaults(BlockTags.NEEDS_DIAMOND_TOOL.getStates()).supported(version.isNewerThanOrEquals(PacketClientVersions.V_1_17)),
                SyncedTag.<PacketStateType>builder(NEEDS_IRON_TOOL).defaults(BlockTags.NEEDS_IRON_TOOL.getStates()).supported(version.isNewerThanOrEquals(PacketClientVersions.V_1_17)),
                SyncedTag.<PacketStateType>builder(NEEDS_STONE_TOOL).defaults(BlockTags.NEEDS_STONE_TOOL.getStates()).supported(version.isNewerThanOrEquals(PacketClientVersions.V_1_17)),
                SyncedTag.<PacketStateType>builder(SWORD_EFFICIENT).defaults(BlockTags.SWORD_EFFICIENT.getStates()).supported(version.isNewerThanOrEquals(PacketClientVersions.V_1_20))
        );
    }

    @SafeVarargs
    private <T> void trackTags(ResourceLocationI location, Function<Integer, T> remapper, SyncedTag.Builder<T>... syncedTags) {
        final Map<ResourceLocationI, SyncedTag<?>> tags = new HashMap<>(syncedTags.length);
        for (SyncedTag.Builder<T> syncedTag : syncedTags) {
            syncedTag.remapper(remapper);
            final SyncedTag<T> built = syncedTag.build();
            tags.put(built.location(), built);
        }
        synced.put(location, tags);
    }

    public SyncedTag<PacketStateType> block(ResourceLocationI tag) {
        final Map<ResourceLocationI, SyncedTag<?>> blockTags = synced.get(BLOCK);
        return (SyncedTag<PacketStateType>) blockTags.get(tag);
    }

    // TODO (Packet Rewrite) (Platform independent tags)
    public void handleTagSync(ServerTagsPacket tags) {
        if (player.getClientVersion().isOlderThan(PacketClientVersions.V_1_13)) return;
        tags.getTagMap().forEach((location, tagList) -> {
            if (!synced.containsKey(location)) return;
            final Map<ResourceLocationI, SyncedTag<?>> syncedTags = synced.get(location);
            tagList.forEach(tag -> {
                if (!syncedTags.containsKey(tag.getKey())) return;
                syncedTags.get(tag.getKey()).readTagValues((WrapperPlayServerTags.Tag) tag);
            });
        });
    }
}
