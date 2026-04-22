package com.eight.talesmod.item;

import com.eight.talesmod.client.renderer.LaserSaberRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FXHelper;

import java.util.function.Consumer;

public class LaserSaberItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LaserSaberItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No complex animations yet; add AnimationControllers here when needed.
    }

    /**
     * Called whenever an entity swings this item. We use this hook to fire the
     * Photon {@code eighttales:saber_trail_yellow} VFX on the local client so
     * that the glowing trail appears during every standard weapon swing.
     *
     * <p>The effect is only spawned on the <em>client</em> side — Photon VFX
     * are purely visual and must never run on the dedicated server.</p>
     */
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player && entity.level().isClientSide()) {
            spawnSaberTrailEffect(player);
        }
        // Return false so vanilla's arm-swing animation still plays normally.
        return false;
    }

    /**
     * Loads (or retrieves from cache) the Photon FX and attaches it to the
     * given player entity.  This method is client-only.
     */
    @OnlyIn(Dist.CLIENT)
    private static void spawnSaberTrailEffect(Player player) {
        try {
            var fx = FXHelper.getFX(
                    ResourceLocation.fromNamespaceAndPath("eighttales", "saber_trail_yellow"));
            if (fx == null) return;

            var executor = new EntityEffectExecutor(
                    fx,
                    player.level(),
                    player,
                    EntityEffectExecutor.AutoRotate.FORWARD);
            // Allow multiple simultaneous instances so rapid swings
            // each show their own trail.
            executor.setAllowMulti(true);
            executor.start();
        } catch (Exception e) {
            // Graceful no-op if the FX file is missing or malformed.
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private LaserSaberRenderer renderer;

            @Override
            public LaserSaberRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new LaserSaberRenderer();
                return this.renderer;
            }
        });
    }
}
