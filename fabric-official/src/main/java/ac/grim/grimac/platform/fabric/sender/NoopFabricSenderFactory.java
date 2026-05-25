package ac.grim.grimac.platform.fabric.sender;

import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.platform.api.sender.SenderFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.rcon.RconConsoleSource;

import java.util.UUID;

// Sender factory for fabric-official. Implements just enough of the SenderFactory
// contract to keep Grim's :common services happy on 26.X without pulling in
// fabric-permissions-api (intermediary-bound). Permission checks fall through
// to vanilla op-level via CommandSourceStack.hasPermission(int).
public class NoopFabricSenderFactory extends SenderFactory<CommandSourceStack> {

    @Override
    protected UUID getUniqueId(CommandSourceStack source) {
        if (source.getEntity() != null) {
            return source.getEntity().getUUID();
        }
        return Sender.CONSOLE_UUID;
    }

    @Override
    protected String getName(CommandSourceStack source) {
        String name = source.getTextName();
        if (source.getEntity() != null && name.equals("Server")) {
            return Sender.CONSOLE_NAME;
        }
        return name;
    }

    @Override
    protected void sendMessage(CommandSourceStack source, String message) {
        // 26.X: always log via LogUtil so alerts appear in latest.log + tmux.
        ac.grim.grimac.utils.anticheat.LogUtil.info(message);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.literal(message), false);
    }

    @Override
    protected void sendMessage(CommandSourceStack source, Component message) {
        // Adventure → MC Component conversion would require adventure-platform-fabric;
        // adventure-text-serializer-plain isn't on the fabric-official classpath either.
        // Flatten via the always-available ComponentFlattener (adventure-api core) — loses
        // formatting but preserves text content. The conversion-util pass in Phase C will
        // replace this with proper formatted text once a 26.X-native adventure path lands.
        StringBuilder out = new StringBuilder();
        ComponentFlattener.basic().flatten(message, out::append);
        sendMessage(source, out.toString());
    }

    @Override
    protected boolean hasPermission(CommandSourceStack source, String node) {
        // 26.X overhauled permissions — hasPermission(int) is gone, replaced by
        // PermissionSet.hasPermission(Permission). Fall back to op level 2 (vanilla
        // "ops only") since fabric-permissions-api isn't ported.
        return source.permissions().hasPermission(
                new Permission.HasCommandLevel(PermissionLevel.byId(2)));
    }

    @Override
    protected boolean hasPermission(CommandSourceStack source, String node, boolean defaultIfUnset) {
        return defaultIfUnset ? true : hasPermission(source, node);
    }

    @Override
    protected void performCommand(CommandSourceStack source, String command) {
        throw new UnsupportedOperationException("performCommand not implemented on 26.X scaffold");
    }

    @Override
    protected boolean isConsole(CommandSourceStack source) {
        CommandSource out = source.source;
        return out == source.getServer()
                || out.getClass() == RconConsoleSource.class
                || (out == CommandSource.NULL && source.getTextName().isEmpty());
    }

    @Override
    protected boolean isPlayer(CommandSourceStack source) {
        return source.getEntity() instanceof ServerPlayer;
    }
}
