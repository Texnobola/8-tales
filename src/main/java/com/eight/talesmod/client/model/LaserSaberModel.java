package com.eight.talesmod.client.model;

import com.eight.talesmod.item.LaserSaberItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LaserSaberModel extends GeoModel<LaserSaberItem> {

    @Override
    public ResourceLocation getModelResource(LaserSaberItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("eighttales", "geo/item/laser_saber.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LaserSaberItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("eighttales", "textures/item/laser_saber.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LaserSaberItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("eighttales", "animations/item/laser_saber.animation.json");
    }
}
