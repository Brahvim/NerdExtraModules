package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.scenes;

import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.framework.scene_layer_api.NerdScenesModule;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdGenericGraphics;
import com.brahvim.nerd.window_management.NerdWindowModule;

import processing.core.PGraphics;

public class NerdGenericGraphicsScene extends NerdScene<PGraphics> {

    protected final NerdGenericGraphics<?> GRAPHICS;
    protected final NerdWindowModule<PGraphics> WINDOW;

    protected NerdGenericGraphicsScene(final NerdScenesModule<PGraphics> p_sceneMan) {
        super(p_sceneMan);
        this.WINDOW = super.GENERIC_WINDOW;
        this.GRAPHICS = super.GENERIC_GRAPHICS;
    }

}
