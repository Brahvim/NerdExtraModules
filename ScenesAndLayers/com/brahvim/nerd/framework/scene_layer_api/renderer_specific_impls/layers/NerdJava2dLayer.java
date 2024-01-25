package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.layers;

import com.brahvim.nerd.framework.scene_layer_api.NerdLayer;
import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdJava2dGraphics;
import com.brahvim.nerd.window_management.NerdWindowModule;
import com.brahvim.nerd.window_management.window_module_impls.NerdJava2dWindowModule;

import processing.awt.PGraphicsJava2D;

public class NerdJava2dLayer extends NerdLayer<PGraphicsJava2D> {

    public final NerdJava2dGraphics GRAPHICS;
    public final NerdJava2dWindowModule WINDOW;

    protected NerdJava2dLayer(final NerdScene<PGraphicsJava2D> p_scene) {
        super(p_scene);
        this.GRAPHICS = (NerdJava2dGraphics) super.GENERIC_GRAPHICS;
        this.WINDOW = (NerdJava2dWindowModule) (NerdWindowModule<?>) super.GENERIC_WINDOW;
    }

}
