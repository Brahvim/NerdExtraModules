package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.scenes;

import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.framework.scene_layer_api.NerdScenesModule;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdP3dGraphics;

import processing.opengl.PGraphics3D;

public abstract class NerdP3dScene extends NerdScene<PGraphics3D> {

    // TODO Get renderer-specific windows module instances!

    protected final NerdP3dGraphics GRAPHICS;

    protected NerdP3dScene(final NerdScenesModule<PGraphics3D> p_sceneMan) {
        super(p_sceneMan);
        this.GRAPHICS = (NerdP3dGraphics) super.getGenericGraphics();
    }

}
