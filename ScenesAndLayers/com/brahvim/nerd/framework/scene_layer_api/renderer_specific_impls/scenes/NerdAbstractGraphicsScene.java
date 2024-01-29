package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.scenes;

import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.framework.scene_layer_api.NerdScenesModule;
import com.brahvim.nerd.processing_wrapper.NerdAbstractGraphics;
import com.brahvim.nerd.window_management.NerdWindowModule;

import processing.core.PGraphics;

public class NerdAbstractGraphicsScene extends NerdScene<PGraphics> {

    public final NerdAbstractGraphics<?> GRAPHICS;
    public final NerdWindowModule<PGraphics> WINDOW;

    protected NerdAbstractGraphicsScene(final NerdScenesModule<PGraphics> p_sceneMan) {
        super(p_sceneMan);
        this.WINDOW = super.GENERIC_WINDOW;
        this.GRAPHICS = super.GENERIC_GRAPHICS;
    }

}
