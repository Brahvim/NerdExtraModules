package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.scenes;

import com.brahvim.nerd.framework.graphics_backends.NerdJava2dGraphics;
import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.framework.scene_layer_api.NerdScenesModule;
import com.brahvim.nerd.window_management.NerdWindowModule;
import com.brahvim.nerd.window_management.window_module_impls.NerdJava2dWindowModule;

import processing.awt.PGraphicsJava2D;

public abstract class NerdJava2dScene extends NerdScene<PGraphicsJava2D> {

    public final NerdJava2dGraphics GRAPHICS;
    public final NerdJava2dWindowModule WINDOW;

    protected NerdJava2dScene(final NerdScenesModule<PGraphicsJava2D> p_sceneMan) {
        super(p_sceneMan);
        this.GRAPHICS = (NerdJava2dGraphics) super.GENERIC_GRAPHICS;
        this.WINDOW = (NerdJava2dWindowModule) (NerdWindowModule<?>) super.GENERIC_WINDOW;
    }

}
