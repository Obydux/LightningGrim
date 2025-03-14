package ac.grim.grimac.platform.fabric;

import ac.grim.grimac.platform.api.PlatformServer;
import ac.grim.grimac.platform.api.sender.Sender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;


public class FabricPlatformServer implements PlatformServer {

    @Override
    public String getPlatformImplementationString() {
        // Return the Fabric server version
        return "Fabric " + FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion().getFriendlyString() + " (MC: " + GrimACFabricLoaderPlugin.FABRIC_SERVER.getVersion() + ")";
    }

    // < 1.14 to 1.19
    @Override
    public void dispatchCommand(Sender sender, String command) {
        ServerCommandSource commandSource = GrimACFabricLoaderPlugin.PLUGIN.getFabricSenderFactory().reverse(sender);
        GrimACFabricLoaderPlugin.FABRIC_SERVER.getCommandManager().execute(commandSource, command);
    }

    // < 1.14 to 1.21.4
    @Override
    public Sender getConsoleSender() {
        ServerCommandSource consoleSource = GrimACFabricLoaderPlugin.FABRIC_SERVER.getCommandSource();
        return GrimACFabricLoaderPlugin.PLUGIN.getFabricSenderFactory().map(consoleSource);
    }

    @Override
    public void registerOutgoingPluginChannel(String bungeeCord) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getTPS() {
        // TODO chain-load
        return GrimACFabricLoaderPlugin.FABRIC_SERVER.getTickTime(); // GrimACFabricLoaderPlugin.FABRIC_SERVER.getAverageTickTime();
    }
}
