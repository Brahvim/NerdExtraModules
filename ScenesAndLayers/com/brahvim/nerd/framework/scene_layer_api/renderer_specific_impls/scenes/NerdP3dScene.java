package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.scenes;

import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.framework.scene_layer_api.NerdScenesModule;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdP3dGraphics;
import com.brahvim.nerd.window_management.NerdWindowModule;
import com.brahvim.nerd.window_management.window_module_impls.NerdGlWindowModule;

import processing.opengl.PGraphics3D;

public abstract class NerdP3dScene extends NerdScene<PGraphics3D> {

    public final NerdP3dGraphics GRAPHICS;
    public final NerdGlWindowModule WINDOW;

    protected NerdP3dScene(final NerdScenesModule<PGraphics3D> p_sceneMan) {
        super(p_sceneMan);
        this.GRAPHICS = (NerdP3dGraphics) super.GENERIC_GRAPHICS;
        this.WINDOW = (NerdGlWindowModule) (NerdWindowModule<?>) super.GENERIC_WINDOW;
    }

}
