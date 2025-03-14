package ac.grim.grimac.platform.fabric.player;

import ac.grim.grimac.platform.api.player.PlatformInventory;
import ac.grim.grimac.platform.fabric.utils.convert.FabricConversionUtil;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.container.Generic3x3Container;
import net.minecraft.container.GenericContainer;
import net.minecraft.container.ShulkerBoxContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;


public class FabricPlatformInventory implements PlatformInventory {

    protected final ServerPlayerEntity fabricPlayer;
    protected final PlayerInventory inventory;

    public FabricPlatformInventory(ServerPlayerEntity player) {
        this.fabricPlayer = player;
        // 1.14 - 1.16.5
        this.inventory = player.inventory;
    }

    @Override
    public ItemStack getItemInHand() {
        return FabricConversionUtil.fromFabricItemStack(inventory.getMainHandStack());
    }

    @Override
    public ItemStack getItemInOffHand() {
        return FabricConversionUtil.fromFabricItemStack(inventory.offHand.get(0));
    }

    // 1.14 - 1.15.2
    @Override
    public ItemStack getStack(int bukkitSlot, int vanillaSlot) {
        return FabricConversionUtil.fromFabricItemStack(inventory.getInvStack(bukkitSlot));
    }

    @Override
    public ItemStack getHelmet() {
        return FabricConversionUtil.fromFabricItemStack(inventory.getArmorStack(3));
    }

    @Override
    public ItemStack getChestplate() {
        return FabricConversionUtil.fromFabricItemStack(inventory.getArmorStack(2));
    }

    @Override
    public ItemStack getLeggings() {
        return FabricConversionUtil.fromFabricItemStack(inventory.getArmorStack(1));
    }

    @Override
    public ItemStack getBoots() {
        return FabricConversionUtil.fromFabricItemStack(inventory.getArmorStack(0));
    }

    // 1.14 - 1.15.2
    @Override
    public ItemStack[] getContents() {

        ItemStack[] items = new ItemStack[
                inventory.getInvSize()         // 1.14 - 1.15.2
        ];
        for (int i = 0; i < items.length; i++) {
            items[i] = FabricConversionUtil.fromFabricItemStack(
                    inventory.getInvStack(i)     // 1.14 - 1.15.2
            );
        }
        return items;
    }

    // TODO
    // I don't understand why we do this on Bukkit, so I'm replicating the behaviour without high-level understanding of purpose
    // This method is only used to check if the inventory matches one of the following
    //     private static final Set<String> SUPPORTED_INVENTORIES = new HashSet<>(
    //            Arrays.asList("CHEST", "DISPENSER", "DROPPER", "PLAYER", "ENDER_CHEST", "SHULKER_BOX", "BARREL", "CRAFTING", "CREATIVE")
    //    );
    // And is slated to be replaced by packet based behaviour, this should do for now
    @Override
    public String getOpenInventoryKey() {
        Container container = fabricPlayer.container;

        // Handle null types (player crafting and creative)

        // Not sure if creative mode check here is correct
        if (container == null && fabricPlayer.isCreative()) {
            return "CREATIVE";
            // 4x4 CRAFTING -> CRAFTING
        } else if (container == fabricPlayer.playerContainer) {
            return "CRAFTING";
        }

        // CRAFTING -> CRAFTING
        if (container instanceof CraftingTableContainer) {
            return "CRAFTING";
        // PLAYER -> PLAYER // I give up on figuring out if this is right or not, ifi t isn't we'll find out
        } else if (container instanceof GenericContainer && ((GenericContainer) container).getType() == ContainerType.GENERIC_9X4) {
            return "PLAYER";
        // SHULKER BOX -> SHULKER_BOX
        } else if (container instanceof ShulkerBoxContainer) {
                return "SHULKER_BOX";
        // CHEST, ENDER_CHEST, or BARREL -> CHEST
        } else if (container instanceof GenericContainer && (((GenericContainer) container).getType() == ContainerType.GENERIC_9X3 || ((GenericContainer) container).getType() == ContainerType.GENERIC_9X6)) {
            return "CHEST";
        // DISPENSER, DROPPER -> DISPENSER
        } else if (container instanceof Generic3x3Container) {
            return "DISPENSER";
        } else {
            return container.getClass().getSimpleName(); // Default fallback
        }
    }
}
