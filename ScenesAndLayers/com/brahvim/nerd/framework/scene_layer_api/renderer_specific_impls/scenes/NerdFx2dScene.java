package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.scenes;

import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.framework.scene_layer_api.NerdScenesModule;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdFx2dGraphics;
import com.brahvim.nerd.window_management.NerdWindowModule;
import com.brahvim.nerd.window_management.window_module_impls.NerdFx2dWindowModule;

import processing.javafx.PGraphicsFX2D;

public class NerdFx2dScene extends NerdScene<PGraphicsFX2D> {

    protected final NerdFx2dGraphics GRAPHICS;
    protected final NerdFx2dWindowModule WINDOW;

    protected NerdFx2dScene(final NerdScenesModule<PGraphicsFX2D> p_sceneMan) {
        super(p_sceneMan);
        this.GRAPHICS = (NerdFx2dGraphics) super.GENERIC_GRAPHICS;
        this.WINDOW = (NerdFx2dWindowModule) (NerdWindowModule<?>) super.GENERIC_WINDOW;
    }

}
