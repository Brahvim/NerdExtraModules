package com.brahvim.nerd.framework.scene_layer_api;

import com.brahvim.nerd.io.asset_loader.NerdAssetsModule;
import com.brahvim.nerd.processing_wrapper.NerdSketch;
import com.brahvim.nerd.processing_wrapper.graphics_backends.NerdGenericGraphics;
import com.brahvim.nerd.window_management.NerdDisplayModule;
import com.brahvim.nerd.window_management.NerdInputModule;
import com.brahvim.nerd.window_management.NerdWindowModule;

import processing.core.PGraphics;

/**
 * Just like {@link NerdScene}s, {@link NerdLayer}s
 * are used via inheritance, and not anonymous classes.
 *
 * <p>
 * To add a {@link NerdLayer} to a {@link NerdScene}, use the
 * {@linkplain NerdScene#addLayer(Class) NerdScene::addLayer(Class)}, passing in
 * your {@link NerdLayer} subclass.
 */
public abstract class NerdLayer<SketchPGraphicsT extends PGraphics> {

	// region `protected` fields.
	// Seriously, why did I set these to be `protected`?
	public final NerdLayer<SketchPGraphicsT> LAYER = this;

	protected final NerdSketch<SketchPGraphicsT> SKETCH;
	protected final NerdWindowModule<SketchPGraphicsT> GENERIC_WINDOW;
	protected final NerdScenesModule<SketchPGraphicsT> MANAGER;

	// Non-generic ones:
	protected final NerdSceneState STATE;
	protected final NerdInputModule<SketchPGraphicsT> INPUT;
	protected final NerdAssetsModule<SketchPGraphicsT> ASSETS;
	protected final NerdDisplayModule<SketchPGraphicsT> DISPLAY;

	protected final NerdScene<SketchPGraphicsT> SCENE;
	// endregion

	protected final NerdGenericGraphics<SketchPGraphicsT> GENERIC_GRAPHICS;

	// region `private` fields.
	private int timesActivated;
	private boolean active;
	// endregion

	protected NerdLayer(final NerdScene<SketchPGraphicsT> p_scene) {
		this.SCENE = p_scene;
		this.STATE = this.SCENE.STATE;
		this.INPUT = this.SCENE.INPUT;
		this.SKETCH = this.SCENE.SKETCH;
		this.ASSETS = this.SCENE.ASSETS;
		this.DISPLAY = this.SCENE.DISPLAY;
		this.MANAGER = this.SCENE.MANAGER;
		this.GENERIC_WINDOW = this.SCENE.GENERIC_WINDOW;
		this.GENERIC_GRAPHICS = this.SCENE.GENERIC_GRAPHICS;
	}

	// region Activity status.
	public boolean isActive() {
		return this.active;
	}

	public int getTimesActivated() {
		return this.timesActivated;
	}

	public void setActive(final boolean p_toggleState) {
		final boolean previouslyActive = this.active; // RECORD!!!!
		this.active = p_toggleState;

		if (this.active && !previouslyActive) {
			if (this.timesActivated == 0)
				this.layerActivated();

			this.setup();
			this.timesActivated++;
		} else
			this.layerDeactivated();
	}
	// endregion

	// region Events.
	// region Mouse events.
	protected void mousePressed() {
	}

	protected void mouseReleased() {
	}

	protected void mouseMoved() {
	}

	protected void mouseClicked() {
	}

	protected void mouseDragged() {
	}

	protected void mouseWheel(final processing.event.MouseEvent p_mouseEvent) {
	}
	// endregion

	// region Keyboard events.
	protected void keyTyped() {
	}

	protected void keyPressed() {
	}

	protected void keyReleased() {
	}
	// endregion

	// region Touch events.
	protected void touchStarted() {
	}

	protected void touchMoved() {
	}

	protected void touchEnded() {
	}
	// endregion

	// region Window focus events.
	protected void focusLost() {
	}

	protected void exit() {
	}

	protected void resized() {
	}

	protected void focusGained() {
	}

	protected void monitorChanged() {
	}

	protected void fullscreenChanged(final boolean p_state) {
	}
	// endregion

	protected void layerActivated() {
	}

	protected void layerDeactivated() {
	}
	// endregion

	// region App workflow callbacks.
	protected void setup() {
	}

	protected void pre() {
	}

	protected void draw() {
	}

	protected void post() {
	}
	// endregion

}
