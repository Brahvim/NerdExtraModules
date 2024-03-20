package com.brahvim.nerd.framework.scene_layer_api.renderer_specific_impls.scenes;

import com.brahvim.nerd.framework.graphics_backends.NerdP2dGraphics;
import com.brahvim.nerd.framework.scene_layer_api.NerdScene;
import com.brahvim.nerd.framework.scene_layer_api.NerdScenesModule;
import com.brahvim.nerd.window_management.NerdWindowModule;
import com.brahvim.nerd.window_management.window_module_impls.NerdGlWindowModule;

import processing.opengl.PGraphics2D;

public abstract class NerdP2dScene extends NerdScene<PGraphics2D> {

	public final NerdP2dGraphics GRAPHICS;
	public final NerdGlWindowModule WINDOW;

	protected NerdP2dScene(final NerdScenesModule<PGraphics2D> p_sceneMan) {
		super(p_sceneMan);
		this.GRAPHICS = (NerdP2dGraphics) super.GENERIC_GRAPHICS;
		this.WINDOW = (NerdGlWindowModule) (NerdWindowModule<?>) super.GENERIC_WINDOW;
	}

}
