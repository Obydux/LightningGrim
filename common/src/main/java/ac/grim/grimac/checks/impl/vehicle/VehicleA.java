package ac.grim.grimac.checks.impl.vehicle;

import ac.grim.grimac.api.packet.types.client.play.ClientSteerVehiclePacket;
import ac.grim.grimac.api.packet.types.event.PacketReceiveEvent;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.api.packet.types.PacketTypes;

@CheckData(name = "VehicleA", description = "Impossible input values")
public class VehicleA extends Check implements PacketCheck {
    public VehicleA(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketTypes.Play.Client.STEER_VEHICLE) {
            final ClientSteerVehiclePacket packet = ClientSteerVehiclePacket.from(event);

            if (Math.abs(packet.getForward()) > 0.98f || Math.abs(packet.getSideways()) > 0.98f) {
                if (flagAndAlert("forwards=" + packet.getForward() + ", sideways=" + packet.getSideways()) && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}
