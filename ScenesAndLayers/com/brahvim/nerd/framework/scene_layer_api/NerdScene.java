package com.brahvim.nerd.framework.scene_layer_api;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.brahvim.nerd.io.asset_loader.NerdAssetsModule;
import com.brahvim.nerd.io.asset_loader.NerdAssetsModule.NerdAsset;
import com.brahvim.nerd.processing_wrapper.NerdAbstractGraphics;
import com.brahvim.nerd.processing_wrapper.NerdSketch;
import com.brahvim.nerd.window_management.NerdDisplayModule;
import com.brahvim.nerd.window_management.NerdInputModule;
import com.brahvim.nerd.window_management.NerdWindowModule;

import processing.core.PGraphics;

/**
 * <h2>Do not use as an anonymous class!</h2>
 * <i>...or run in another thread.</i>
 * <p>
 * This stuff is initialized by the {@link NerdScenesModule} via reflection.
 * <h5><i>Always extend!</i></h5>
 */
public abstract class NerdScene<SketchPGraphicsT extends PGraphics> {

	@SuppressWarnings("unchecked")
	public final Class<NerdScene<SketchPGraphicsT>> SCENE_CLASS
	/*   */ = (Class<NerdScene<SketchPGraphicsT>>) this.getClass();

	public final String SCENE_NAME = this.SCENE_CLASS.getSimpleName();

	// region `protected` fields.
	protected final NerdSketch<SketchPGraphicsT> SKETCH;
	protected final NerdScenesModule<SketchPGraphicsT> MANAGER;
	protected final NerdWindowModule<SketchPGraphicsT> GENERIC_WINDOW;
	protected final NerdAbstractGraphics<SketchPGraphicsT> GENERIC_GRAPHICS;

	// Non-generic:
	protected final NerdSceneState STATE;
	protected final NerdInputModule<SketchPGraphicsT> INPUT;
	protected final NerdAssetsModule<SketchPGraphicsT> ASSETS;
	protected final NerdDisplayModule<SketchPGraphicsT> DISPLAY;
	// endregion

	// region `private` fields.
	private int startMillis;
	private boolean donePreloading;

	// Start at `0`. "Who needs layers anyway?"
	private final List<NerdLayer<SketchPGraphicsT>> LAYERS = new ArrayList<>(0);

	private final Map<Class<? extends NerdLayer<SketchPGraphicsT>>, Constructor<? extends NerdLayer<SketchPGraphicsT>>>
	/*   */ LAYER_CONSTRUCTORS = new HashMap<>(0);
	// endregion

	@SuppressWarnings("unchecked")
	protected NerdScene(final NerdScenesModule<SketchPGraphicsT> p_sceneMan) {
		this.MANAGER = p_sceneMan;
		this.SKETCH = this.MANAGER.getSketch();
		this.GENERIC_GRAPHICS = this.SKETCH.getNerdGenericGraphics();

		this.STATE = new NerdSceneState();
		this.ASSETS = new NerdAssetsModule<>(this.SKETCH);
		this.INPUT = this.SKETCH.getNerdModule(NerdInputModule.class);
		this.DISPLAY = this.SKETCH.getNerdModule(NerdDisplayModule.class);
		this.GENERIC_WINDOW = this.SKETCH.getNerdModule(NerdWindowModule.class);
	}

	// region Queries.
	@SuppressWarnings("unchecked")
	public int getTimesLoaded() {
		return this.MANAGER.getTimesSceneLoaded((Class<NerdScene<SketchPGraphicsT>>) this.getClass());
	}

	public boolean hasCompletedPreload() {
		return this.donePreloading;
	}

	public NerdSketch<SketchPGraphicsT> getSketch() {
		return this.SKETCH;
	}

	public NerdAbstractGraphics<SketchPGraphicsT> getGenericGraphics() {
		return this.GENERIC_GRAPHICS;
	}

	// region Time queries.
	/**
	 * @return The number of milliseconds that had passed when the
	 *         {@link NerdScene} started. This data is cached in advance.
	 */
	public int getStartMillis() {
		return this.startMillis;
	}

	/**
	 * @return The number of milliseconds that have passed since the
	 *         {@link NerdScene} started, as the result of an immediate calculation.
	 */
	public int getMillisSinceStart() {
		return this.SKETCH.millis() - this.startMillis;
	}
	// endregion
	// endregion

	// region `Layer`-operations.
	// region `onFirstLayerOfClass()` overloads.
	/**
	 * Given a {@link NerdLayer} class, performs a task on the instance of that
	 * class, which was added <i>first</i> to this {@link NerdScene}.
	 */
	public <T extends NerdLayer<SketchPGraphicsT>> void onFirstLayerOfClass(
			final Class<T> p_layerClass,
			final Consumer<T> p_task) {
		this.onFirstLayerOfClass(p_layerClass, p_task, null);
	}

	/**
	 * Given a {@link NerdLayer} class, performs a task on the instance of that
	 * class, which was added <i>first</i> to this {@link NerdScene}. If there is no
	 * instance of the given class, performs the other given task.
	 */
	// Actual implementation!:
	public <T extends NerdLayer<SketchPGraphicsT>> void onFirstLayerOfClass(
			final Class<T> p_layerClass, final Consumer<T> p_onFoundTask, final Runnable p_notFoundTask) {
		final T instance = this.getFirstLayerOfClass(p_layerClass);

		// Check if we have any such layers:
		if (instance != null) {
			// On finding one, perform the given task!
			if (p_onFoundTask != null)
				p_onFoundTask.accept(instance);
		} else {
			// On finding none, perform the other task!
			if (p_notFoundTask != null)
				p_notFoundTask.run();
		}
	}
	// endregion

	// region `onLayersOfClass()` and similar.
	// region `onInactiveLayersOfClass()` overloads.
	public <T extends NerdLayer<SketchPGraphicsT>> void onInactiveLayersOfClass(
			final Class<T> p_layerClass, final Consumer<T> p_task) {
		this.onInactiveLayersOfClass(p_layerClass, p_task, null);
	}

	// Actual implementation!:
	@SuppressWarnings("unchecked")
	public <T extends NerdLayer<SketchPGraphicsT>> void onInactiveLayersOfClass(
			final Class<T> p_layerClass, final Consumer<T> p_task, final Runnable p_notFoundTask) {
		if (p_layerClass == null) {
			p_notFoundTask.run();
			return;
		}

		int i = 0;
		final int LAYERS_SIZE = this.LAYERS.size();

		// For every `NerdLayer`,
		for (; i != LAYERS_SIZE; ++i) {
			final NerdLayer<SketchPGraphicsT> l = this.LAYERS.get(i);

			if (l != null) // If it ain't `null`,
				if (l.getClass().equals(p_layerClass)) // And it be from ma' class,
					if (!l.isActive()) // ...if it ain't active,
						p_task.accept((T) l); // ...perform the given task!
		}

		// If no `NerdLayer`s were found, perform the other task!:
		if (i == 0 && p_notFoundTask != null)
			p_notFoundTask.run();
	}
	// endregion

	// region `onActiveLayersOfClass()` overloads.
	public <T extends NerdLayer<SketchPGraphicsT>> void onActiveLayersOfClass(
			final Class<T> p_layerClass, final Consumer<T> p_task) {
		this.onActiveLayersOfClass(p_layerClass, p_task, null);
	}

	@SuppressWarnings("unchecked")
	public <T extends NerdLayer<SketchPGraphicsT>> void onActiveLayersOfClass(
			final Class<T> p_layerClass, final Consumer<T> p_task, final Runnable p_noLayersFoundTask) {
		if (p_layerClass == null) {
			p_noLayersFoundTask.run();
			return;
		}

		int i = 0;
		final int LAYERS_SIZE = this.LAYERS.size();

		// For every `NerdLayer`,
		for (; i != LAYERS_SIZE; ++i) {
			final NerdLayer<SketchPGraphicsT> l = this.LAYERS.get(i);

			if (l != null) // If it ain't `null`,
				if (l.getClass().equals(p_layerClass)) // And it be from ma' class,
					if (l.isActive()) // ...if it is active,
						p_task.accept((T) l); // ...perform the given task!
		}

		// If no `NerdLayer`s were found, perform the other task!:
		if (i == 0 && p_noLayersFoundTask != null)
			p_noLayersFoundTask.run();
	}
	// endregion

	// region `onLayersOfClass()` overloads.
	/**
	 * Given a {@link NerdLayer} class, performs a task on all instances of that
	 * class being used by this {@link NerdScene}.
	 */
	public <T extends NerdLayer<SketchPGraphicsT>> void onLayersOfClass(
			final Class<T> p_layerClass, final Consumer<T> p_task) {
		this.onLayersOfClass(p_layerClass, p_task, null);
	}

	/**
	 * Given a {@link NerdLayer} class, performs a task on all instances of that
	 * class being used by this {@link NerdScene}. If no instance is found, performs
	 * the other given task.
	 */
	// Actual implementation!:
	@SuppressWarnings("unchecked")
	public <T extends NerdLayer<SketchPGraphicsT>> void onLayersOfClass(
			final Class<T> p_layerClass, final Consumer<T> p_task, final Runnable p_noLayersFoundTask) {
		if (p_layerClass == null) {
			p_noLayersFoundTask.run();
			return;
		}

		int i = 0;
		final int LAYERS_SIZE = this.LAYERS.size();

		// For every `NerdLayer`,
		for (; i != LAYERS_SIZE; ++i) {
			final NerdLayer<SketchPGraphicsT> l = this.LAYERS.get(i);
			if (l != null) // If it ain't `null`,
				if (l.getClass().equals(p_layerClass)) // And it be from ma' class,
					p_task.accept((T) l); // ...perform the given task!
		}

		// If no `NerdLayer`s were found, perform the other task!:
		if (i == 0 && p_noLayersFoundTask != null)
			p_noLayersFoundTask.run();
	}
	// endregion
	// endregion

	// region `getLayer()` and similar.
	// They get a running `Layer`'s reference from its (given) class.
	public <RetT extends NerdLayer<SketchPGraphicsT>> RetT getFirstLayerOfClass(
			final Class<RetT> p_layerClass) {
		for (final var l : this.LAYERS)
			if (l.getClass().equals(p_layerClass))
				return p_layerClass.cast(l);
		return null;
	}

	/**
	 * Gives an {@link List} of {@link NerdLayer} instances of the given
	 * subclass this {@link NerdScene} contains, which are <b>not</b> active.
	 */
	public List<NerdLayer<SketchPGraphicsT>> getInactiveLayers(
			final Class<? extends NerdLayer<SketchPGraphicsT>> p_layerClass) {
		if (p_layerClass == null)
			return new ArrayList<>();

		final List<NerdLayer<SketchPGraphicsT>> toRet = new ArrayList<>();

		for (final var l : this.LAYERS)
			if (l != null && l.getClass().equals(p_layerClass) && !l.isActive())
				toRet.add(l);

		return toRet;
	}

	/**
	 * Gives an {@link List} of {@link NerdLayer} instances of the given
	 * subclass this {@link NerdScene} contains, which are also active.
	 */
	public List<NerdLayer<SketchPGraphicsT>> getActiveLayers(
			final Class<? extends NerdLayer<SketchPGraphicsT>> p_layerClass) {
		if (p_layerClass == null)
			return new ArrayList<>();

		final List<NerdLayer<SketchPGraphicsT>> toRet = new ArrayList<>();

		for (final var l : this.LAYERS)
			if (l != null && l.getClass().equals(p_layerClass) && l.isActive())
				toRet.add(l);

		return toRet;
	}

	/**
	 * Gives an {@link List} of {@link NerdLayer} instances of the given
	 * subclass this {@link NerdScene} contains.
	 */
	public List<NerdLayer<SketchPGraphicsT>> getLayers(
			final Class<? extends NerdLayer<SketchPGraphicsT>> p_layerClass) {
		final List<NerdLayer<SketchPGraphicsT>> toRet = new ArrayList<>();

		for (final var l : this.LAYERS)
			if (l != null && l.getClass().equals(p_layerClass))
				toRet.add(l);

		return toRet;
	}

	/**
	 * This method gives the user access to all {@link NerdLayer} instances being
	 * used by this {@link NerdScene} along with rights such as changing layer
	 * rendering order.
	 */
	public List<NerdLayer<SketchPGraphicsT>> getLayers() {
		return this.LAYERS;
	}
	// endregion

	// region `NerdLayer` state-management.

	/*
	 * @SafeVarargs // I'm not willing to limit your freedom, but this method HAS to
	 * be `final`...
	 * public final NerdLayer[] addLayers(final Class<? extends NerdLayer>...
	 * p_layerClasses) {
	 * final NerdLayer[] toRet = new NerdLayer[p_layerClasses.length];
	 * for (int i = 0; i < p_layerClasses.length; ++i) {
	 * final Class<? extends NerdLayer> c = p_layerClasses[i];
	 * toRet[i] = this.addLayers(c);
	 * }
	 * return toRet;
	 * }
	 */

	public <RetT extends NerdLayer<SketchPGraphicsT>> RetT addLayer(final Class<RetT> p_layerClass) {
		if (p_layerClass == null)
			throw new NullPointerException("You weren't supposed to pass `null` into `NerdScene::startLayer()`.");

		// We allow multiple layer instances, by the way!
		final Constructor<? extends NerdLayer<SketchPGraphicsT>>
		/*   */ layerConstructor = this.getLayerConstructor(p_layerClass);
		// ^^^ This is a separate declaration to allow better debugging.
		final NerdLayer<SketchPGraphicsT> toRet = this.constructLayer(layerConstructor);

		if (toRet == null)
			throw new NullPointerException("Could not construct `NerdLayer`!");

		toRet.setActive(true);
		this.LAYERS.add(toRet);
		return p_layerClass.cast(toRet);
	}

	@SafeVarargs // I'm not willing to limit your freedom, but this method HAS to be `final`...
	public final void restartLayers(
			final Class<? extends NerdLayer<SketchPGraphicsT>>... p_layerClasses) {
		for (final var c : p_layerClasses)
			this.restartLayers(c);
	}

	@SuppressWarnings("unchecked")
	public void restartLayer(final NerdLayer<SketchPGraphicsT> p_layer) {
		if (p_layer == null)
			return;

		final Class<? extends NerdLayer<SketchPGraphicsT>> layerClass = (Class<NerdLayer<SketchPGraphicsT>>) p_layer
				.getClass();

		// If an instance of this layer does not already exist,
		if (!this.LAYERS.contains(p_layer)) {
			System.out.printf("No instance of `%s` exists. Making one...%n", layerClass.getSimpleName());
			this.addLayer(layerClass);
			return;
		}

		final NerdLayer<SketchPGraphicsT> toStart = this
				.constructLayer(this.getLayerConstructor(layerClass));

		if (toStart == null)
			throw new NullPointerException("You passed a `null` `NerdLayer` into `NerdScene::restartLayer()` :|");

		this.LAYERS.set(this.LAYERS.indexOf(p_layer), toStart);
		p_layer.setActive(false);
		toStart.setActive(true);
	}
	// endregion

	// region `NerdLayer` construction.
	private Constructor<? extends NerdLayer<SketchPGraphicsT>> getLayerConstructor(
			final Class<? extends NerdLayer<SketchPGraphicsT>> p_layerClass) {
		Constructor<? extends NerdLayer<SketchPGraphicsT>>
		/*   */ toRet = this.LAYER_CONSTRUCTORS.get(p_layerClass);
		if (toRet != null)
			return toRet;

		try {
			toRet = p_layerClass.getDeclaredConstructor(NerdScene.class);
		} catch (final NoSuchMethodException e) {
			System.err.println("""
					Every subclass of `NerdLayer` must be `public` with a `public` "null-constructor"
					(constructor with no arguments), or no overridden constructors at all.""");
		} catch (final SecurityException e) {
			e.printStackTrace();
		}

		this.LAYER_CONSTRUCTORS.put(p_layerClass, toRet);
		return toRet;
	}

	private NerdLayer<SketchPGraphicsT> constructLayer(
			final Constructor<? extends NerdLayer<SketchPGraphicsT>> p_layerConstructor) {
		if (p_layerConstructor == null)
			return null;

		NerdLayer<SketchPGraphicsT> toRet = null;

		// region Construct `toRet`.
		try {
			p_layerConstructor.setAccessible(true); // NOSONAR!
			toRet = p_layerConstructor.newInstance(this);
			p_layerConstructor.setAccessible(false);
		} catch (final InstantiationException
				| IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		// endregion

		return toRet;
	}
	// endregion
	// endregion

	// region Anything callback-related, LOL.
	/* `package` */ void runSetup(final NerdSceneState p_state) {
		this.startMillis = this.SKETCH.millis();
		this.setup(p_state);
	}

	/* `package` */ synchronized void runPreload() {
		this.preload();
		this.ASSETS.forceLoading();

		if (this.MANAGER.SETTINGS.ON_PRELOAD.useExecutors) {
			final ThreadPoolExecutor executor = new ThreadPoolExecutor(
					0, this.MANAGER.SETTINGS.ON_PRELOAD.maxExecutorThreads,
					10L, TimeUnit.SECONDS, new SynchronousQueue<>(),
					new ThreadFactory() {
						private static int threadCount = 1;

						@Override
						public Thread newThread(final Runnable p_threadTask) {
							// Using `NerdScene.this` to get the name of exact class the API user wrote:
							return new Thread(p_threadTask,
									NerdScene.this.getClass().getSimpleName()
											+ "ParallelAssetLoaderThread"
											+ threadCount++);
						}
					});

			final Set<Future<?>> futures = new HashSet<>(this.ASSETS.size());
			this.ASSETS.forEach(a -> futures.add(executor.submit(a::startLoading)));
			executor.shutdown(); // This tells the executor to stop accepting new tasks.

			// If you must complete within this function, do that:
			if (this.MANAGER.SETTINGS.ON_PRELOAD.completeAssetLoadingWithinPreload)
				try {
					executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS); // Keep going, keep going...
					// Can't simply cheat the implementation to make it wait forever!
				} catch (final InterruptedException e) {
					// e.printStackTrace();
					Thread.currentThread().interrupt();
				}
		} else
			this.ASSETS.forEach(NerdAsset::startLoading);

		this.donePreloading = true;
		// System.gc(); // Do we need this?
	}

	/* `package` */ void runSceneChanged() {
		this.sceneChanged();
	}

	/* `package` */ void runDispose() {
		this.dispose();
	}

	/* `package` */ void runDraw() {
		if (this.MANAGER.SETTINGS.drawFirstCaller == null)
			this.MANAGER.SETTINGS.drawFirstCaller = NerdScenesModuleSettings.NerdSceneLayerCallbackOrder.LAYER;

		// To avoid asynchronous changes from causing repetition, we put both parts in
		// `if` and `else` block.

		switch (this.MANAGER.SETTINGS.drawFirstCaller) {
			case SCENE -> {
				this.GENERIC_GRAPHICS.push();
				this.draw();
				this.GENERIC_GRAPHICS.pop();

				for (final var l : this.LAYERS)
					this.runLayerDraw(l);
			}

			case LAYER -> {
				for (final var l : this.LAYERS)
					this.runLayerDraw(l);

				this.GENERIC_GRAPHICS.push();
				this.draw();
				this.GENERIC_GRAPHICS.pop();
			}
		}
	}

	private void runLayerDraw(final NerdLayer<SketchPGraphicsT> p_layer) {
		if (p_layer == null)
			return;

		if (p_layer.isActive()) {
			this.GENERIC_GRAPHICS.push();
			p_layer.draw();
			this.GENERIC_GRAPHICS.pop();
		}
	}

	/* `package` */ void runPost() {
		if (this.MANAGER.SETTINGS.postFirstCaller == null)
			this.MANAGER.SETTINGS.postFirstCaller = NerdScenesModuleSettings.NerdSceneLayerCallbackOrder.LAYER;

		// To avoid asynchronous changes from causing repetition, we put both parts in
		// `if` and `else` block.

		switch (this.MANAGER.SETTINGS.preFirstCaller) {
			case SCENE -> {
				this.post();
				this.runLayerPost();
			}
			case LAYER -> {
				this.runLayerPost();
				this.post();
			}
		}
	}

	private void runLayerPost() {
		for (final var l : this.LAYERS)
			if (l != null)
				if (l.isActive())
					l.post();
	}

	/* `package` */ void runExit() {
		for (final var l : this.LAYERS)
			if (l != null)
				if (l.isActive())
					l.exit();

		this.exit();
	}

	/* `package` */ void runPre() {
		if (this.MANAGER.SETTINGS.preFirstCaller == null)
			this.MANAGER.SETTINGS.preFirstCaller = NerdScenesModuleSettings.NerdSceneLayerCallbackOrder.SCENE;

		switch (this.MANAGER.SETTINGS.preFirstCaller) {
			case SCENE -> {
				this.pre();

				for (final var l : this.LAYERS)
					if (l != null)
						if (l.isActive())
							l.pre();
			}
			case LAYER -> {
				for (final var l : this.LAYERS)
					if (l != null)
						if (l.isActive())
							l.pre();

				this.pre();
			}
		}
	}

	// region Scene workflow callbacks.
	/**
	 * Used by a {@link NerdScene} to load {@link NerdAsset}s
	 * into their, or their {@link NerdScenesModule}'s {@link NerdAssetsModule}.
	 *
	 * <p>
	 * Use this method for all asset-loading purposes that you would like to do in
	 * the background. If {@linkplain NerdScenesModule#loadSceneAssets()
	 * NerdScenesModule::loadSceneAssets()} or
	 * {@linkplain NerdScenesModule#loadSceneAssetsAsync()
	 * NerdScenesModule::loadSceneAssetsAsync()} is called, this method is run
	 * async, loading-in all {@link NerdAsset}s!
	 * <p>
	 * Since {@link NerdScene}s could be a part of the same {@link NerdSketch}, it
	 * is important to ensure that this method is {@code synchronized}.
	 */
	protected synchronized void preload() {
	}

	/**
	 * Callback for when the scene changes. Calling certain methods from
	 * {@linkplain NerdScene#MANAGER NerdScene::MANAGER} <i><b>will</b></i> cause
	 * crashes here!
	 */
	protected void sceneChanged() {
	}

	/**
	 * {@linkplain NerdScene#setup() NerdScene::setup()} is called when one of
	 * {@linkplain NerdScenesModule#startScene(Class)
	 * NerdScenesModule::startScene(Class)},
	 * {@linkplain NerdScenesModule#restartScene(Class)
	 * NerdScenesModule::restartScene(Class)}, or
	 * {@linkplain NerdScenesModule#startPreviousScene(Class)
	 * NerdScenesModule::startPreviousScene(Class)}
	 * is called, after the {@link NerdScene} finishes executing
	 * {@linkplain NerdScene#preload() NerdScene::preload()},
	 * <p>
	 * {@linkplain NerdLayer#setup() NerdLayer::setup()} is called <i>when a
	 * {@link NerdLayer} is set active</i> using
	 * {@linkplain NerdLayer#setActive(boolean) NerdLayer::setActive(boolean)}.
	 */
	protected void setup(final NerdSceneState p_state) {
	}

	protected void pre() {
	}

	protected void draw() {
	}

	protected void post() {
	}

	protected void exit() {
	}

	// protected void preDraw() { }

	protected void dispose() {
	}

	// protected void postDraw() { }
	// endregion
	// endregion

	// region Events.
	// region Mouse events.
	public void mousePressed() {
	}

	public void mouseReleased() {
	}

	public void mouseMoved() {
	}

	public void mouseClicked() {
	}

	public void mouseDragged() {
	}

	public void mouseWheel(final processing.event.MouseEvent p_mouseEvent) {
	}
	// endregion

	// region Keyboard events.
	public void keyTyped() {
	}

	public void keyPressed() {
	}

	public void keyReleased() {
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
	protected void resized() {
	}

	protected void focusLost() {
	}

	protected void focusGained() {
	}

	protected void monitorChanged() {
	}

	protected void fullscreenChanged(final boolean p_state) {
	}
	// endregion
	// endregion

}
