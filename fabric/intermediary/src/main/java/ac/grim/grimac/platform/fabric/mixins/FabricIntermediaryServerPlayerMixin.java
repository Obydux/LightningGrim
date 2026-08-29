package ac.grim.grimac.platform.fabric.mixins;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.platform.fabric.inject.FabricServerPlayerHandle;
import ac.grim.grimac.platform.fabric.player.FabricPlatformPlayerFactory;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayer.class)
@Implements(@Interface(iface = ac.grim.grimac.platform.fabric.inject.FabricServerPlayerHandle.class, prefix = "grim$"))
abstract class FabricIntermediaryServerPlayerMixin extends Player {

    public FabricIntermediaryServerPlayerMixin(Level level, BlockPos blockPos, GameProfile gameProfile) {
        super(level, blockPos, gameProfile);
    }

    public void grim$resyncSharedFlags() {
        this.getEntityData().getItem(DATA_SHARED_FLAGS_ID).setDirty(true);
        this.getEntityData().isDirty = true;
    }

    public boolean grim$isDead() {
        return this.isDeadOrDying();
    }

    public void grim$sendSystemText(Object nativeComponent) {
        this.displayClientMessage((Component) nativeComponent, false);
    }

    public boolean grim$isDisconnected() {
        return ((ServerPlayer) (Object) this).hasDisconnected();
    }

    public String grim$usernameString() {
        return this.getName().getString();
    }

    public void grim$broadcastInventoryChanges() {
        this.containerMenu.broadcastChanges();
    }

    public void grim$stopUsingItem() {
        this.stopUsingItem();
    }

    public boolean grim$isUsingItem() {
        return this.isUsingItem();
    }

    public double grim$posX() {
        return this.getX();
    }

    public double grim$posY() {
        return this.getY();
    }

    public double grim$posZ() {
        return this.getZ();
    }

    public UUID grim$uuid() {
        return this.getUUID();
    }

    public Object grim$vehicleEntity() {
        return this.getVehicle();
    }

    public Object grim$gameMode() {
        return ((ServerPlayer) (Object) this).gameMode.getGameModeForPlayer();
    }

    public Object grim$heldItemStack() {
        return this.inventory.getSelected();
    }

    public Object grim$inventoryItemAt(int slot) {
        return this.inventory.getItem(slot);
    }

    public Object grim$usedItemHand() {
        return this.getUsedItemHand();
    }

    public int grim$inventorySlotCount() {
        return this.inventory.getContainerSize();
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void onRestoreFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        ((FabricPlatformPlayerFactory) GrimAPI.INSTANCE.getPlatformPlayerFactory())
                .replaceNativePlayer(oldPlayer.getUUID(), (FabricServerPlayerHandle) (Object) this);
    }
}
