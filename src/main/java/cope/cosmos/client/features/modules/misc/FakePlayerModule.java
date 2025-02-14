package cope.cosmos.client.features.modules.misc;

import com.mojang.authlib.GameProfile;
import cope.cosmos.client.events.entity.EntityWorldEvent;
import cope.cosmos.client.events.network.DisconnectEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class FakePlayerModule extends Module {
	public static FakePlayerModule INSTANCE;
	
	public FakePlayerModule() {
        super("FakePlayer", Category.MISC, "Spawns in a indestructible client-side player");
        INSTANCE = this;
        setExempt(true);
    }

    // **************************** general settings ****************************

    public static Setting<Boolean> inventory = new Setting<>("Inventory", true)
            .setDescription("Sync the fake player inventory");

    public static Setting<Boolean> health = new Setting<>("Health", true)
            .setDescription("Sync the fakeplayer health");

    // entity id of fakeplayer
    private int id = -1;

    @Override
    public void onEnable() {
        super.onEnable();

        // create a fake player
        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(mc.player.getGameProfile().getId(), "FakePlayer"));

        // copy rotations from player
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYaw;

        // copy inventory from player
        if (inventory.getValue()) {
            fakePlayer.inventory.copyInventory(mc.player.inventory);
            fakePlayer.inventoryContainer = mc.player.inventoryContainer;
        }

        // copy health from player
        if (health.getValue()) {
            fakePlayer.setHealth(mc.player.getHealth());
            fakePlayer.setAbsorptionAmount(mc.player.getAbsorptionAmount());
        }

        // set player traits
        fakePlayer.setSneaking(mc.player.isSneaking());
        fakePlayer.setPrimaryHand(mc.player.getPrimaryHand());

        // add the fake player to world
        id = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        mc.world.addEntityToWorld(id, fakePlayer);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // remove fake player from world
        mc.world.removeEntityFromWorld(id);
        id = -1;
    }

    @SubscribeEvent
    public void onEntityRemove(EntityWorldEvent.EntityRemoveEvent event) {
        if (event.getEntity().equals(mc.player)) {

            // remove fake player from world
            mc.world.removeEntityFromWorld(id);
            id = -1;

            // disable module
            disable(true);
        }
    }

    @SubscribeEvent
    public void onDisconnect(DisconnectEvent event) {

        // disable module
        disable(true);
    }
}
