package com.eight.talesmod.client.renderer;

import com.eight.talesmod.client.model.LaserSaberModel;
import com.eight.talesmod.item.LaserSaberItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class LaserSaberRenderer extends GeoItemRenderer<LaserSaberItem> {

    public LaserSaberRenderer() {
        super(new LaserSaberModel());
    }
}
