package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.scenes;

import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdP2dGraphics;

import processing.opengl.PGraphics2D;

public abstract class NerdP2dScene extends NerdScene<PGraphics2D> {

    protected NerdP2dGraphics graphics;

    @Override
    protected void sceneRendererInit() {
        this.graphics = (NerdP2dGraphics) super.getGenericGraphics();
    }

    /* package */ void runSceneInit() {
    }

    protected void sceneInit() {
    }

}
