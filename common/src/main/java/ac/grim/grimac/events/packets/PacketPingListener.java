package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.packet.types.client.play.ClientPongPacket;
import ac.grim.grimac.api.packet.types.client.play.ClientWindowConfirmationPacket;
import ac.grim.grimac.api.packet.types.event.PacketSendEvent;
import ac.grim.grimac.api.packet.types.server.play.ServerPingPacket;
import ac.grim.grimac.api.packet.types.server.play.ServerWindowConfirmation;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsS;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.data.Pair;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.api.packet.types.event.PacketReceiveEvent;
import ac.grim.grimac.api.packet.types.PacketTypes;

public class PacketPingListener extends PacketListenerAbstract {

    // Must listen on LOWEST (or maybe low) to stop Tuinity packet limiter from kicking players for transaction/pong spam
    public PacketPingListener() {
        super(PacketListenerPriority.LOWEST);
    }


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketTypes.Play.Client.WINDOW_CONFIRMATION) {
            ClientWindowConfirmationPacket transaction = ClientWindowConfirmationPacket.from(event);
            short id = transaction.getActionId();

            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;
            player.packetStateData.lastTransactionPacketWasValid = false;

            // Vanilla always uses an ID starting from 1
            if (id <= 0) {
                // check if accepted
                if (!transaction.isAccepted()) {
                    player.checkManager.getCheck(BadPacketsS.class).flagAndAlert();
                    event.setCancelled(true);
                    return;
                }
                // Check if we sent this packet before cancelling it
                if (player.addTransactionResponse(id)) {
                    player.packetStateData.lastTransactionPacketWasValid = true;
                    event.setCancelled(true);
                }
            }
        }

        if (event.getPacketType() == PacketTypes.Play.Client.PONG) {
            ClientPongPacket pong = ClientPongPacket.from(event);
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;
            player.packetStateData.lastTransactionPacketWasValid = false;

            int id = pong.getId();
            // If it wasn't below 0, it wasn't us
            // If it wasn't in short range, it wasn't us either
            if (id == (short) id) {
                short shortID = ((short) id);
                if (player.addTransactionResponse(shortID)) {
                    player.packetStateData.lastTransactionPacketWasValid = true;
                    // Not needed for vanilla as vanilla ignores this packet, needed for packet limiters
                    event.setCancelled(true);
                }
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketTypes.Play.Server.WINDOW_CONFIRMATION) {
            ServerWindowConfirmation confirmation = ServerWindowConfirmation.from(event);
            short id = confirmation.getActionId();
            //
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;
            player.packetStateData.lastServerTransWasValid = false;
            // Vanilla always uses an ID starting from 1
            if (id <= 0) {
                if (player.didWeSendThatTrans.remove(id)) {
                    player.packetStateData.lastServerTransWasValid = true;
                    player.transactionsSent.add(new Pair<>(id, System.nanoTime()));
                    player.lastTransactionSent.getAndIncrement();
                }
            }
        }

        if (event.getPacketType() == PacketTypes.Play.Server.PING) {
            ServerPingPacket pong = ServerPingPacket.from(event);
            int id = pong.getId();
            //
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;
            player.packetStateData.lastServerTransWasValid = false;
            // Check if in the short range, we only use short range
            if (id == (short) id) {
                // Cast ID twice so we can use the list
                Short shortID = ((short) id);
                if (player.didWeSendThatTrans.remove(shortID)) {
                    player.packetStateData.lastServerTransWasValid = true;
                    player.transactionsSent.add(new Pair<>(shortID, System.nanoTime()));
                    player.lastTransactionSent.getAndIncrement();
                }
            }
        }
    }


}
