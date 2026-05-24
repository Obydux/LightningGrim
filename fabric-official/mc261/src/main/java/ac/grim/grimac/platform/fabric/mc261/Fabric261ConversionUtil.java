package ac.grim.grimac.platform.fabric.mc261;

import ac.grim.grimac.platform.fabric.utils.convert.IFabricConversionUtil;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.minecraft.network.chat.Component;

public class Fabric261ConversionUtil implements IFabricConversionUtil {
    @Override
    public ItemStack fromFabricItemStack(net.minecraft.world.item.ItemStack fabricStack) {
        // TODO Phase C: wire proper conversion through PE's encoder. For now empty
        // stack so the engine doesn't crash on inventory reads — the bukkit/spigot
        // path uses PE's BukkitConverter; fabric needs an equivalent.
        return ItemStack.EMPTY;
    }

    @Override
    public net.minecraft.network.chat.Component toNativeText(net.kyori.adventure.text.Component component) {
        // adventure-platform-fabric isn't on the 26.X classpath. Plain-text flatten
        // until that path lands (Phase C); preserves text content, loses formatting.
        StringBuilder out = new StringBuilder();
        ComponentFlattener.basic().flatten(component, out::append);
        return Component.literal(out.toString());
    }
}
