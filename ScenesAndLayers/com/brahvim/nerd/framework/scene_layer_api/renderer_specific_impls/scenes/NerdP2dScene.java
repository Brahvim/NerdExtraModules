package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.scenes;

import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.framework.scene_layer_api.NerdScenesModule;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdP2dGraphics;

import processing.opengl.PGraphics2D;

public abstract class NerdP2dScene extends NerdScene<PGraphics2D> {

    protected final NerdP2dGraphics GRAPHICS;

    protected NerdP2dScene(final NerdScenesModule<PGraphics2D> p_sceneMan) {
        super(p_sceneMan);
        this.GRAPHICS = (NerdP2dGraphics) super.getGenericGraphics();
    }

}
