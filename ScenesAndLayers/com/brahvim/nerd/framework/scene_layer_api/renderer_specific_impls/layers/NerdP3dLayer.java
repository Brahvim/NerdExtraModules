package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.layers;

import com.brahvim.nerd.framework.scene_layer_api.NerdLayer;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdP3dGraphics;

import processing.opengl.PGraphics3D;

public class NerdP3dLayer extends NerdLayer<PGraphics3D> {

    protected NerdP3dGraphics graphics;

    @Override
    protected void layerRendererInit() {
        this.graphics = (NerdP3dGraphics) super.getGenericGraphics();
    }

}
