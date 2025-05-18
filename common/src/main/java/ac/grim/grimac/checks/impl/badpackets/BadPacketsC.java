package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.api.packet.types.PacketTypes;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.api.packet.types.event.PacketReceiveEvent;
import ac.grim.grimac.api.packet.player.enums.GameMode;

@CheckData(name = "BadPacketsC", description = "Interacted with self")
public class BadPacketsC extends Check implements PacketCheck {
    public BadPacketsC(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketTypes.Play.Client.INTERACT_ENTITY) {
            if (player.gamemode == GameMode.SPECTATOR) return;
            if (packetFactory.clientInteractEntity(event).getEntityId() == player.entityID) {
                // Instant ban
                if (flagAndAlert() && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}
