package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.scenes;

import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.framework.scene_layer_api.NerdScenesModule;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdGenericGraphics;

import processing.core.PGraphics;

public class NerdGenericScene extends NerdScene<PGraphics> {

    protected final NerdGenericGraphics<?> GRAPHICS;

    protected NerdGenericScene(final NerdScenesModule<PGraphics> p_sceneMan) {
        super(p_sceneMan);
        this.GRAPHICS = super.getGenericGraphics();
    }

}
