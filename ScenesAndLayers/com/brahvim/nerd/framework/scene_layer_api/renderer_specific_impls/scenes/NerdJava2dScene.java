package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.scenes;

import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.framework.scene_layer_api.NerdScenesModule;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdJava2dGraphics;

import processing.awt.PGraphicsJava2D;

public abstract class NerdJava2dScene extends NerdScene<PGraphicsJava2D> {

    protected final NerdJava2dGraphics GRAPHICS;

    protected NerdJava2dScene(final NerdScenesModule<PGraphicsJava2D> p_sceneMan) {
        super(p_sceneMan);
        this.GRAPHICS = (NerdJava2dGraphics) super.getGenericGraphics();
    }

}
