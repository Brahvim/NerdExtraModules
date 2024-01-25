package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.layers;

import com.brahvim.nerd.framework.scene_layer_api.NerdLayer;
import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdP3dGraphics;
import com.brahvim.nerd.window_management.NerdWindowModule;
import com.brahvim.nerd.window_management.window_module_impls.NerdGlWindowModule;

import processing.opengl.PGraphics3D;

public class NerdP3dLayer extends NerdLayer<PGraphics3D> {

    protected final NerdP3dGraphics GRAPHICS;
    protected final NerdGlWindowModule WINDOW;

    protected NerdP3dLayer(final NerdScene<PGraphics3D> p_scene) {
        super(p_scene);
        this.GRAPHICS = (NerdP3dGraphics) super.GENERIC_GRAPHICS;
        this.WINDOW = (NerdGlWindowModule) (NerdWindowModule<?>) super.GENERIC_WINDOW;
    }

}
