package com.brahvim.nerd.framework.scene_layer_api;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.brahvim.nerd.framework.graphics_backends.NerdP3dGraphics;
import com.brahvim.nerd.framework.graphics_backends.NerdP3dGraphics.NerdLightSlot;
import com.brahvim.nerd.io.asset_loader.NerdAssetsModule;
import com.brahvim.nerd.processing_wrapper.NerdAbstractGraphics;
import com.brahvim.nerd.processing_wrapper.NerdModule;
import com.brahvim.nerd.processing_wrapper.NerdSketch;
import com.brahvim.nerd.window_management.NerdWindowModule;

import processing.core.PGraphics;

public class NerdScenesModule<SketchPGraphicsT extends PGraphics> extends NerdModule<SketchPGraphicsT> {

	// region Inner classes.
	// My code style: If it is an inner class, also write the name of the outer
	// class. I do this to aid reading and to prevent namespace pollution.

	@FunctionalInterface
	public interface NerdNewSceneStartedListener {
		public void sceneChanged(
				NerdScenesModule<?> scenesModule,
				Class<? extends NerdScene<?>> previousSceneClass,
				Class<? extends NerdScene<?>> currentSceneClass);
	}

	/**
	 * Stores scene data while a scene is not active. This is done for purposes such
	 * as loading assets for the scene before it even starts!
	 */
	/* `package` */ class NerdSceneCache<SketchPGraphicsForCacheT extends PGraphics> {

		// region Fields.
		/* `package */ int timesLoaded = 0;
		/* `package */ final NerdSceneState STATE;
		/* `package */ final Constructor<? extends NerdScene<SketchPGraphicsForCacheT>> CONSTRUCTOR;

		// protected NerdEcsModule cachedEcs; // Nope! If the user really wants it,
		// they'll should get stuff serialized via `NerdEcsModule` and handle it...

		/* `package */ NerdScene<SketchPGraphicsForCacheT> cachedReference;
		// `NerdSceneModule` deletes this when the scene exits.
		/* `package */ NerdAssetsModule<SketchPGraphicsForCacheT> cachedAssets;
		// endregion

		/* `package */ NerdSceneCache(
				final Constructor<? extends NerdScene<SketchPGraphicsForCacheT>> p_constructor,
				final NerdScene<SketchPGraphicsForCacheT> p_cachedReference) {
			this.CONSTRUCTOR = p_constructor;
			this.cachedReference = p_cachedReference;
			this.STATE = this.cachedReference.STATE;
		}

		// region Cache queries.
		/* `package */ boolean isSceneCacheNull() {
			return this.cachedReference == null;
		}

		/* `package */ void /* deleteCache() { */ nullifyCache() {
			// If this was (hopefully) the only reference to the scene object, it gets GCed!
			this.cachedReference = null;
			System.gc();
		}
		// endregion

	}
	// endregion

	public final NerdScenesModuleSettings<SketchPGraphicsT> SETTINGS;

	// region `protected` fields.
	protected boolean sceneSwitchOccurred;
	protected NerdScene<SketchPGraphicsT> currentScene;

	/**
	 * This {@link Map} contains cached data about each {@link NerdScene} class any
	 * {@link NerdScenesModule} instance has cached or ran.
	 * <p>
	 * Actual "caching" of a {@link NerdScene} is when its corresponding
	 * {@linkplain NerdScenesModule.NerdSceneCache#cachedReference
	 * NerdScenesModule.NerdScenesModuleSceneCache::cachedReference}
	 * is not{@code null}.
	 * <p>
	 * The initial capacity here ({@code 2}) is to aid performance, since, the JIT
	 * does no optimization till the first scene switch. All scene switches after
	 * that the initial should be fast enough!
	 */
	protected final Map<Class<? extends NerdScene<SketchPGraphicsT>>, NerdSceneCache<SketchPGraphicsT>>
	/*   */ SCENE_CLASS_TO_CACHE_MAP = new HashMap<>(2);

	protected final List<NerdScenesModule.NerdNewSceneStartedListener>
	/*   */ SCENE_CHANGED_LISTENERS = new ArrayList<>(0); // Not gunna have any, will we?

	protected final List<NerdScenesModule.NerdNewSceneStartedListener>
	/*   */ SCENE_CHANGED_LISTENERS_TO_REMOVE = new ArrayList<>(0); // Not gunna have any, will we?

	protected Class<? extends NerdScene<SketchPGraphicsT>> currentSceneClass, previousSceneClass;
	// endregion

	public NerdScenesModule(
			final NerdSketch<SketchPGraphicsT> p_sketch,
			final NerdScenesModuleSettings<SketchPGraphicsT> p_settings) {
		super(p_sketch);
		this.SETTINGS = p_settings;
	}

	// region `NerdSketch` workflow callbacks.
	@Override
	protected void pre() {
		if (this.currentScene != null)
			this.currentScene.runPre();
	}

	@Override
	protected void post() {
		if (this.currentScene != null)
			this.currentScene.runPost();

		this.sceneSwitchOccurred = false;
	}

	@Override
	public void draw() {
		if (super.SKETCH.frameCount == 1 && this.currentScene == null) {
			if (this.SETTINGS.FIRST_SCENE_CLASS == null)
				System.err.println("There is no initial `"
						+ NerdScene.class.getSimpleName()
						+ "` to show!");
			else
				this.startScene(this.SETTINGS.FIRST_SCENE_CLASS);
		}

		if (this.currentScene != null)
			this.currentScene.runDraw();
	}

	@Override
	protected void exit() {
		if (this.currentScene != null)
			this.currentScene.runExit();
	}

	@Override
	protected void dispose() {
		if (this.currentScene != null)
			this.currentScene.runDispose();
	}

	// Too expensive! Need a `push()` and `pop()`.
	/*
	 * protected void runPreDraw() {
	 * if (this.currScene != null)
	 * this.currScene.runPreDraw();
	 * }
	 * protected void runPostDraw() {
	 * if (this.currScene != null)
	 * this.currScene.runPostDraw();
	 * }
	 */
	// endregion

	// region Event callbacks. Passed to the ECS first, THEN here!
	// `NerdLayer` callers:
	protected void callOnCurrSceneActiveLayers(final Consumer<NerdLayer<?>> p_eventCallbackMethod) {
		if (p_eventCallbackMethod != null)
			for (final var l : this.currentScene.getLayers())
				if (l != null)
					if (l.isActive())
						p_eventCallbackMethod.accept(l);
	}

	protected <OtherArgT> void callOnCurrSceneActiveLayers(
			final BiConsumer<NerdLayer<?>, OtherArgT> p_eventCallbackMethod, final OtherArgT p_otherArg) {
		if (p_eventCallbackMethod != null)
			for (final var l : this.currentScene.getLayers())
				if (l != null)
					if (l.isActive())
						p_eventCallbackMethod.accept(l, p_otherArg);
	}

	// region Mouse event callbacks.
	@Override
	public void mousePressed() {
		if (this.currentScene == null)
			return;

		this.currentScene.mousePressed();
		this.callOnCurrSceneActiveLayers(NerdLayer::mousePressed);
	}

	@Override
	public void mouseReleased() {
		if (this.currentScene == null)
			return;

		this.currentScene.mouseReleased();
		this.callOnCurrSceneActiveLayers(NerdLayer::mouseReleased);
	}

	@Override
	public void mouseMoved() {
		if (this.currentScene == null)
			return;

		this.currentScene.mouseMoved();
		this.callOnCurrSceneActiveLayers(NerdLayer::mouseMoved);
	}

	@Override
	public void mouseClicked() {
		if (this.currentScene == null)
			return;

		this.currentScene.mouseClicked();
		this.callOnCurrSceneActiveLayers(NerdLayer::mouseClicked);
	}

	@Override
	public void mouseDragged() {
		if (this.currentScene == null)
			return;

		this.currentScene.mouseDragged();
		this.callOnCurrSceneActiveLayers(NerdLayer::mouseDragged);
	}

	@Override
	public void mouseWheel(
			final processing.event.MouseEvent p_mouseEvent) {
		if (this.currentScene == null)
			return;

		this.currentScene.mouseWheel(p_mouseEvent);
		this.callOnCurrSceneActiveLayers(NerdLayer::mouseWheel, p_mouseEvent);
	}
	// endregion

	// region Touch event callbacks.
	@Override
	protected void touchStarted() {
		if (this.currentScene == null)
			return;

		this.currentScene.touchStarted();
		this.callOnCurrSceneActiveLayers(NerdLayer::touchStarted);
	}

	@Override
	protected void touchMoved() {
		if (this.currentScene == null)
			return;

		this.currentScene.touchMoved();
		this.callOnCurrSceneActiveLayers(NerdLayer::touchMoved);
	}

	@Override
	protected void touchEnded() {
		if (this.currentScene == null)
			return;

		this.currentScene.touchEnded();
		this.callOnCurrSceneActiveLayers(NerdLayer::touchEnded);
	}
	// endregion

	// region Window event callbacks.
	@Override
	protected void resized() {
		if (this.currentScene == null)
			return;

		this.currentScene.resized();
		this.callOnCurrSceneActiveLayers(NerdLayer::resized);
	}

	@Override
	protected void focusLost() {
		if (this.currentScene == null)
			return;

		this.currentScene.focusLost();
		this.callOnCurrSceneActiveLayers(NerdLayer::focusLost);
	}

	@Override
	protected void focusGained() {
		if (this.currentScene == null)
			return;

		this.currentScene.focusGained();
		this.callOnCurrSceneActiveLayers(NerdLayer::focusGained);
	}

	@Override
	protected void monitorChanged() {
		if (this.currentScene == null)
			return;

		this.currentScene.monitorChanged();
		this.callOnCurrSceneActiveLayers(NerdLayer::monitorChanged);
	}

	@Override
	protected void fullscreenChanged(final boolean p_state) {
		if (this.currentScene == null)
			return;

		this.currentScene.fullscreenChanged(p_state);
		this.callOnCurrSceneActiveLayers(NerdLayer::fullscreenChanged, p_state);
	}
	// endregion

	// region Keyboard event callbacks.
	@Override
	public void keyTyped() {
		if (this.currentScene == null)
			return;

		this.currentScene.keyTyped();
		this.callOnCurrSceneActiveLayers(NerdLayer::keyTyped);
	}

	@Override
	public void keyPressed() {
		if (this.currentScene == null)
			return;

		this.currentScene.keyPressed();
		this.callOnCurrSceneActiveLayers(NerdLayer::keyPressed);
	}

	@Override
	public void keyReleased() {
		if (this.currentScene == null)
			return;

		this.currentScene.keyReleased();
		this.callOnCurrSceneActiveLayers(NerdLayer::keyReleased);
	}
	// endregion
	// endregion

	// region [`public`] Getters.
	public NerdScene<SketchPGraphicsT> getCurrentScene() {
		return this.currentScene;
	}

	public boolean didSceneSwitchOccur() {
		return this.sceneSwitchOccurred;
	}

	public Class<? extends NerdScene<SketchPGraphicsT>> getCurrentSceneClass() {
		return this.currentSceneClass;
	}

	public Class<? extends NerdScene<SketchPGraphicsT>> getPreviousSceneClass() {
		return this.previousSceneClass;
	}
	// endregion

	// region [`public`] Queries.
	/**
	 * Adds a {@link NerdScenesModule.NerdNewSceneStartedListener} that
	 * allows you to track when a scene starts!
	 *
	 * @param p_listener is the listener you want to add.
	 */
	public final void addNewSceneStartedListener(
			final NerdScenesModule.NerdNewSceneStartedListener p_listener) {
		if (p_listener != null)
			this.SCENE_CHANGED_LISTENERS.add(p_listener);
	}

	/**
	 * Removes a {@link NerdScenesModule.NerdNewSceneStartedListener}
	 * that would otherwise allow you to track when a scene starts.
	 *
	 * @param p_listener is the listener you want to remove.
	 */
	public final void removeNewSceneStartedListener(
			final NerdScenesModule.NerdNewSceneStartedListener p_listener) {
		this.SCENE_CHANGED_LISTENERS_TO_REMOVE.add(p_listener);
	}

	/**
	 * Returns a {@link HashSet} of {@link NerdScene} classes including only classes
	 * instances of which this {@link NerdScenesModule} has ran.
	 */
	public final Set<Class<? extends NerdScene<SketchPGraphicsT>>> getKnownSceneClassesSet() {
		return new HashSet<>(this.SCENE_CLASS_TO_CACHE_MAP.keySet());
	}
	// endregion

	// region `Scene`-operations.
	public int getTimesSceneLoaded(
			final Class<? extends NerdScene<SketchPGraphicsT>> p_sceneClass) {
		return this.SCENE_CLASS_TO_CACHE_MAP.get(p_sceneClass).timesLoaded;
	}

	// region Invoking the asset loader.
	// To those demanding var-arg versions of these `loadSceneAssets*()` methods:
	// "...no"! (I mean, should I just make a bean of some kind?)

	public void loadSceneAssetsAsync(
			final Class<? extends NerdScene<SketchPGraphicsT>> p_sceneClass) {
		this.loadSceneAssetsAsync(p_sceneClass, false);
	}

	public void loadSceneAssetsAsync(
			final Class<? extends NerdScene<SketchPGraphicsT>> p_sceneClass,
			final boolean p_forcibly) {
		if (!this.hasCached(p_sceneClass))
			this.cacheScene(p_sceneClass);

		if (this.givenSceneRanPreload(p_sceneClass))
			return;

		new Thread(() -> this.loadSceneAssets(p_sceneClass, p_forcibly),
				"NerdAsyncAssetLoader_" + this.getClass().getSimpleName()).start();
	}

	// Non-async versions:
	public void loadSceneAssets(
			final Class<? extends NerdScene<SketchPGraphicsT>> p_sceneClass) {
		this.loadSceneAssets(p_sceneClass, false);
	}

	public void loadSceneAssets(
			final Class<? extends NerdScene<SketchPGraphicsT>> p_sceneClass,
			final boolean p_forcibly) {
		if (!this.hasCached(p_sceneClass))
			this.cacheScene(p_sceneClass);

		if (this.givenSceneRanPreload(p_sceneClass))
			return;

		final NerdSceneCache<SketchPGraphicsT> sceneCache = this.SCENE_CLASS_TO_CACHE_MAP
				.get(p_sceneClass);

		if (sceneCache != null) {
			if (sceneCache.cachedReference.hasCompletedPreload())
				return;

			this.loadSceneAssets(sceneCache.cachedReference, p_forcibly);
		}
	}
	// endregion

	// region Starting, or switching to a scene.
	public void restartScene() {
		this.restartScene(null);
	}

	public void restartScene(final NerdSceneState p_setupState) {
		if (this.currentSceneClass == null)
			return;

		this.startSceneImpl(this.currentSceneClass, p_setupState);
	}

	public void startPreviousScene() {
		this.startPreviousScene(null);
	}

	public void startPreviousScene(
			final NerdSceneState p_setupState) {
		if (this.previousSceneClass == null)
			return;

		final NerdScene<SketchPGraphicsT> toUse = this
				.constructScene(this.SCENE_CLASS_TO_CACHE_MAP.get(this.previousSceneClass).CONSTRUCTOR);
		this.setScene(toUse, p_setupState);
	}

	/**
	 * Starts a {@link NerdScene}, and tells using the return value, whether it was
	 * restored from cache or started again.
	 */
	public boolean startScene(
			final Class<? extends NerdScene<SketchPGraphicsT>> p_sceneClass) {
		return this.startScene(p_sceneClass, null);
	}

	public boolean startScene(
			final Class<? extends NerdScene<SketchPGraphicsT>> p_sceneClass,
			final NerdSceneState p_setupState) {
		if (p_sceneClass == null)
			throw new NullPointerException(
					String.format("`%s`::startScene()` received `null`.",
							NerdScenesModule.class.getSimpleName()));

		if (this.hasCached(p_sceneClass)) {
			this.setScene(this.SCENE_CLASS_TO_CACHE_MAP.get(p_sceneClass).cachedReference, p_setupState);
			return true;
		} else {
			this.startSceneImpl(p_sceneClass, p_setupState);
			return false;
		}
	}
	// endregion

	// region `NerdScene<SketchPGraphicsT>` operations.
	protected boolean givenSceneRanPreload(
			final Class<? extends NerdScene<SketchPGraphicsT>> p_sceneClass) {
		final NerdScene<SketchPGraphicsT> sceneCache = this.SCENE_CLASS_TO_CACHE_MAP.get(p_sceneClass).cachedReference;

		// `SonarLint` did this optimization, yay!:
		return sceneCache != null && sceneCache.hasCompletedPreload();
		// return sceneCache == null ? false : sceneCache.hasCompletedPreload();
	}

	@SuppressWarnings("unchecked")
	protected void loadSceneAssets(
			final NerdScene<SketchPGraphicsT> p_scene,
			final boolean p_forcibly) {
		if (p_scene == null)
			return;

		// If forced to, do it:
		if (p_forcibly) {
			p_scene.runPreload();
			return;
		}

		final Class<? extends NerdScene<SketchPGraphicsT>>
		/*   */ sceneClass = (Class<NerdScene<SketchPGraphicsT>>) p_scene.getClass();

		// If this scene has never been loaded up before, preload the data!
		if (this.getTimesSceneLoaded(sceneClass) == 0) {
			// p_scene.ASSETS.clear(); // Not needed - this code will never have bugs. Hah!
			p_scene.runPreload();
			this.SCENE_CLASS_TO_CACHE_MAP.get(sceneClass).cachedAssets = p_scene.ASSETS;
			return;
		}

		// region Preloads other than the first one.
		// We're allowed to preload only once?
		// Don't re-load, just use the cache!:
		if (this.SETTINGS.ON_PRELOAD.preloadOnlyOnce) {
			final NerdAssetsModule<SketchPGraphicsT> assets = this.SCENE_CLASS_TO_CACHE_MAP
					.get(sceneClass).cachedAssets;
			super.getSketchModulesMap()
					.put((Class<? extends NerdModule<SketchPGraphicsT>>) NerdAssetsModule.class, assets);
			p_scene.ASSETS.clear(); // Since the next operation is an addition, clear; else it won't be 'copying'!
			p_scene.ASSETS.addAllAssetsFrom(assets);
		} else { // Else, since we're supposed to run `NerdScene::preload()` each time, do that!:
			p_scene.ASSETS.clear();
			p_scene.runPreload();
			this.SCENE_CLASS_TO_CACHE_MAP.get(sceneClass).cachedAssets = p_scene.ASSETS;
			// ^^^ `NerdAssetsModule` has literally only ONE field, but alright!
		}
		// endregion

	}

	// region Caching operations.
	protected boolean hasCached(
			final Class<? extends NerdScene<SketchPGraphicsT>> p_sceneClass) {
		// If you haven't been asked to run the scene even once, you didn't cache it!
		// Simply report so:
		// if (!this.SCENE_CACHE.containsKey(p_sceneClass))
		// return false;

		// ...so you ran the scene? Great! ...BUT DO YOU HAVE THE SCENE OBJECT?!
		// return !this.SCENE_CACHE.get(p_sceneClass).isSceneCacheNull();

		// Faster!:
		final NerdSceneCache<SketchPGraphicsT> cachedScene = this.SCENE_CLASS_TO_CACHE_MAP
				.get(p_sceneClass);
		// return cachedScene == null ? false : !cachedScene.isSceneCacheNull();
		return cachedScene != null && !cachedScene.isSceneCacheNull(); // SonarLint's 'simplification'!
		// (For the JVM, not us 😅)
	}

	/**
	 * {@linkplain NerdScenesModule
	 * NerdScenesModule:}<wbr>{@linkplain NerdScenesModule#cacheScene(Class)
	 * :cacheScene(final Class<&quest; extends NerdScene<SketchPGraphicsT>>
	 * sceneClass)}
	 */
	protected void cacheScene(
			final Class<? extends NerdScene<SketchPGraphicsT>> p_sceneClass
	/**//* , final boolean p_isDeletable */) {
		if (this.SCENE_CLASS_TO_CACHE_MAP.containsKey(p_sceneClass))
			return;

		final Constructor<? extends NerdScene<SketchPGraphicsT>> sceneConstructor = this
				.getSceneConstructor(p_sceneClass); // Yes, this IS used later!
		final NerdScene<SketchPGraphicsT> constructedScene = this.constructScene(sceneConstructor);

		if (constructedScene == null)
			throw new IllegalStateException(
					"`NerdScenesModule::constructScene()` returned `null` on an attempt to cache!");

		this.SCENE_CLASS_TO_CACHE_MAP.put(p_sceneClass,
				new NerdSceneCache<>(sceneConstructor, constructedScene));
	}
	// endregion

	// region Construction-and-setup operations!
	protected Constructor<? extends NerdScene<SketchPGraphicsT>> getSceneConstructor(
			final Class<? extends NerdScene<SketchPGraphicsT>> p_sceneClass) {
		if (this.hasCached(p_sceneClass))
			return this.SCENE_CLASS_TO_CACHE_MAP.get(p_sceneClass).CONSTRUCTOR;

		try {
			return p_sceneClass.getDeclaredConstructor(NerdScenesModule.class);
		} catch (final NoSuchMethodException e) {
			throw new UnsupportedOperationException("""
					Every subclass of `NerdScene` must be declared `public` with a
					`public` constructor with only a `NerdScenesModule` argument.
					It also must not be an anonymous, or inner class.""");
		}
	}

	protected NerdScene<SketchPGraphicsT> constructScene(
			final Constructor<? extends NerdScene<SketchPGraphicsT>> p_sceneConstructor) {

		if (p_sceneConstructor == null) // Shouldn't ever be!
			throw new IllegalStateException("""
					An unknown error ocurred during the construction of a `NerdScene`.
					Try declaring this subclass of `NerdScene` as public, with a
					public constructor and only a `NerdScenesModule` argument.
					Said class must not be an anonymous or inner class.""");

		NerdScene<SketchPGraphicsT> toRet = null;

		// region Get an instance if possible!
		try {
			p_sceneConstructor.setAccessible(true); // NOSONAR!
			toRet = p_sceneConstructor.newInstance(this);
			p_sceneConstructor.setAccessible(false);
		} catch (final InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			// throw new UnsupportedOperationException(
			// "No subclass of `NerdScene<SketchPGraphicsT>` must be an anonymous or inner
			// class.");
		}
		// endregion

		// Shouldn't be `null`, since we handling practically every case causing this.
		if (toRet == null)
			throw new IllegalStateException("""
					`NerdScenesModule::constructScene()` returned `null`!
					Please check if your sketch and scene's rendering backends match.""");

		final Class<? extends NerdScene<SketchPGraphicsT>> sceneClass = p_sceneConstructor.getDeclaringClass();
		final NerdSceneCache<SketchPGraphicsT> sceneCache = this.SCENE_CLASS_TO_CACHE_MAP.get(sceneClass);

		if (sceneCache != null) {
			toRet.STATE.DATA.putAll(sceneCache.STATE.DATA);
			return toRet;
		}

		// If this is the first time we're constructing this scene,
		// ensure it has a cache and a saved state!
		this.SCENE_CLASS_TO_CACHE_MAP.put(
				sceneClass,
				new NerdSceneCache<>(p_sceneConstructor, toRet));
		toRet.STATE.clear();

		return toRet;
	}

	// Yes, this checks for errors.
	protected void startSceneImpl(
			final Class<? extends NerdScene<SketchPGraphicsT>> p_sceneClass,
			final NerdSceneState p_state) {
		this.setScene(
				this.constructScene(
						this.getSceneConstructor(p_sceneClass)),
				p_state);
	}

	// The scene-deleter! Also receives EVERY request to start a scene, by the way.
	@SuppressWarnings("unchecked")
	protected void setScene(final NerdScene<SketchPGraphicsT> p_currentScene, final NerdSceneState p_state) {
		final NerdWindowModule<SketchPGraphicsT> window = super.SKETCH.getNerdModule(NerdWindowModule.class);
		window.cursorVisible = true;
		window.cursorConfined = false;

		// region `this.SETTINGS.ON_SWITCH` tasks.
		if (this.SETTINGS.ON_SWITCH.doClear) {
			if (this.SETTINGS.ON_SWITCH.clearColor == -1)
				super.SKETCH.clear();
			else
				super.SKETCH.background(this.SETTINGS.ON_SWITCH.clearColor);
		}

		if (this.SETTINGS.ON_SWITCH.resetSceneLayerCallbackOrder)
			this.SETTINGS.setSceneLayerCallbackOrderToDefault();
		// endregion

		this.previousSceneClass = this.currentSceneClass;
		this.SCENE_CHANGED_LISTENERS.removeAll(this.SCENE_CHANGED_LISTENERS_TO_REMOVE);
		this.SCENE_CHANGED_LISTENERS.forEach(l -> l
				.sceneChanged(this, this.previousSceneClass, this.currentSceneClass));

		final NerdAbstractGraphics<SketchPGraphicsT> nerdGraphics = super.SKETCH.getNerdGenericGraphics();
		nerdGraphics.setClearImage(null);
		nerdGraphics.setAutoClearFlag(false);
		nerdGraphics.setClearImageFlag(false);

		if (this.previousSceneClass != null) {
			// Exit the scene, and nullify the cache.
			this.currentScene.runSceneChanged();

			final NerdSceneCache<SketchPGraphicsT>
			/*   */ sceneCache = this.SCENE_CLASS_TO_CACHE_MAP.get(this.currentSceneClass);
			if (sceneCache != null)
				sceneCache.nullifyCache();// Sets the `NerdScene` instance to `null`, then calls `System.gc()`!
		}

		this.currentSceneClass = (Class<NerdScene<SketchPGraphicsT>>) p_currentScene.getClass();
		this.currentScene = p_currentScene;
		this.setupCurrentScene(p_state);
	}

	// Set the time, *then* call `NerdScenesModule::runSetup()`.
	// Called only by `NerdScenesModule::setScene()`:
	protected void setupCurrentScene(final NerdSceneState p_state) {
		this.sceneSwitchOccurred = true;
		this.loadSceneAssets(this.currentScene, false);
		this.SCENE_CLASS_TO_CACHE_MAP.get(this.currentSceneClass).timesLoaded++;

		// Helps in resetting style and transformation info across scenes! YAY!:
		if (this.previousSceneClass != null)
			super.SKETCH.pop();

		// We push and pop the style and transforms to auto-reset. Hah!:
		super.SKETCH.push();
		super.SKETCH.textFont(super.SKETCH.getDefaultFont()); // ...Also reset the font. Thanks!

		if (super.SKETCH.getNerdGenericGraphics() instanceof final NerdP3dGraphics g)
			for (final var s : NerdLightSlot.values())
				g.clearLightSlot(s);

		this.SCENE_CHANGED_LISTENERS.removeAll(this.SCENE_CHANGED_LISTENERS_TO_REMOVE);

		// This is nullable in SO MANY PLACES!:
		if (p_state == null) // Deal with it now!
			this.currentScene.runSetup(new NerdSceneState());
		else
			this.currentScene.runSetup(p_state);
	}
	// endregion
	// endregion
	// endregion

}
