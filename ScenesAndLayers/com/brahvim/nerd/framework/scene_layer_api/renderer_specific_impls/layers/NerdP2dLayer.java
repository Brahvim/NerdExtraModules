package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.layers;

import com.brahvim.nerd.framework.scene_layer_api.NerdLayer;
import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdP2dGraphics;
import com.brahvim.nerd.window_management.NerdWindowModule;
import com.brahvim.nerd.window_management.window_module_impls.NerdGlWindowModule;

import processing.opengl.PGraphics2D;

public class NerdP2dLayer extends NerdLayer<PGraphics2D> {

    public final NerdP2dGraphics GRAPHICS;
    public final NerdGlWindowModule WINDOW;

    protected NerdP2dLayer(final NerdScene<PGraphics2D> p_scene) {
        super(p_scene);
        this.GRAPHICS = (NerdP2dGraphics) super.GENERIC_GRAPHICS;
        this.WINDOW = (NerdGlWindowModule) (NerdWindowModule<?>) super.GENERIC_WINDOW;
    }

}
