package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.layers;

import com.brahvim.nerd.framework.scene_layer_api.NerdLayer;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdJava2dGraphics;

import processing.awt.PGraphicsJava2D;

public class NerdJava2dLayer extends NerdLayer<PGraphicsJava2D> {

    protected NerdJava2dGraphics graphics;

    @Override
    protected void layerRendererInit() {
        this.graphics = (NerdJava2dGraphics) super.getGenericGraphics();
    }

}
