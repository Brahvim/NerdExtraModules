package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.layers;

import com.brahvim.nerd.framework.scene_layer_api.NerdLayer;
import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdFx2dGraphics;
import com.brahvim.nerd.window_management.NerdWindowModule;
import com.brahvim.nerd.window_management.window_module_impls.NerdFx2dWindowModule;

import processing.javafx.PGraphicsFX2D;

public class NerdFx2dLayer extends NerdLayer<PGraphicsFX2D> {

    public final NerdFx2dGraphics GRAPHICS;
    public final NerdFx2dWindowModule WINDOW;

    protected NerdFx2dLayer(final NerdScene<PGraphicsFX2D> p_scene) {
        super(p_scene);
        this.GRAPHICS = (NerdFx2dGraphics) super.SCENE.getGenericGraphics();
        this.WINDOW = (NerdFx2dWindowModule) (NerdWindowModule<?>) super.GENERIC_WINDOW;
    }

}
