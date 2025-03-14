package ac.grim.grimac.platform.fabric.command;


import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.platform.api.command.PlayerSelector;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.platform.fabric.sender.FabricSenderFactory;


public class FabricPlayerSelectorAdapter implements PlayerSelector {
    protected final org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector fabricSelector;

    public FabricPlayerSelectorAdapter(org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector fabricSelector) {
        this.fabricSelector = fabricSelector;
    }

    @Override
    public Sender getSinglePlayer() {
        return ((FabricSenderFactory) GrimAPI.INSTANCE.getSenderFactory()).map(fabricSelector.single().getCommandSource());
    }


    @Override
    public String inputString() {
        return fabricSelector.inputString();
    }
}
