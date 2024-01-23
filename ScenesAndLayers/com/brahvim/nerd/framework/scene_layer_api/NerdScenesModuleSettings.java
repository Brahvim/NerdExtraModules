package com.brahvim.nerd.framework.scene_layer_api;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.brahvim.nerd.processing_wrapper.NerdModuleSettings;
import com.brahvim.nerd.processing_wrapper.NerdSketch;

@SuppressWarnings("rawtypes")
public class NerdScenesModuleSettings extends NerdModuleSettings<NerdScenesModule> {

	public final Class<? extends NerdScene<?>> FIRST_SCENE_CLASS;

	/**
	 * Dictates to every {@link NerdScenesModule} instance, the order in which a
	 * {@link NerdScene} or {@link NerdLayer} is allowed to call certain "workflow
	 * events" ({@linkplain NerdScene#pre() NerdScene::pre()},
	 * {@linkplain NerdScene#draw() NerdScene::draw()} and
	 * {@linkplain NerdScene#post() NerdScene::post()}) from Processing.
	 *
	 * @see {@linkplain NerdScenesModuleSettings#preFirstCaller
	 *      NerdScenesModuleSettings::preFirstCaller}, which is
	 *      {@linkplain NerdSceneLayerCallbackOrder#SCENE
	 *      NerdSceneLayerCallbackOrder::SCENE} by
	 *      default.
	 * @see {@linkplain NerdScenesModuleSettings#drawFirstCaller
	 *      NerdScenesModuleSettings::drawFirstCaller}, which is
	 *      {@linkplain NerdSceneLayerCallbackOrder#LAYER
	 *      NerdSceneLayerCallbackOrder::LAYER} by
	 *      default.
	 * @see {@linkplain NerdScenesModuleSettings#postFirstCaller
	 *      NerdScenesModuleSettings::postFirstCaller}, which is
	 *      {@linkplain NerdSceneLayerCallbackOrder#LAYER
	 *      NerdSceneLayerCallbackOrder::LAYER} by
	 *      default.
	 */
	public /* `static` */ enum NerdSceneLayerCallbackOrder {
		SCENE(), LAYER();
	}

	public class OnScenePreload {

		private OnScenePreload() {
		}

		/**
		 * When {@code true}, {@linkplain NerdScene#preload() NerdScene::preload()} is
		 * run only the first time a {@link NerdScene} class is used in the engine.
		 * Setting this to {@code false} loads scene assets each time, so that assets
		 * are updated.
		 *
		 * @apiNote {@code true} by default!
		 */
		public boolean preloadOnlyOnce = true;

		/**
		 * When {@code true}, {@linkplain NerdScene#preload() NerdScene::preload()} runs
		 * the loading process in multiple threads using a {@link ExecutorService}. If
		 * {@linkplain NerdScenesModuleSettings.OnScenePreload#completeAssetLoadingWithinPreload
		 * NerdScenesModuleSettings.OnScenePreload::completeAssetLoadingWithinPreload}
		 * is {@code true}, the asset loading is <i>guaranteed</i> to finish within
		 * {@linkplain NerdScene#preload() NerdScene::preload()}.
		 *
		 * @apiNote {@code true} by default!
		 */
		public boolean useExecutors = true;

		/**
		 * The maximum number of threads multithreaded asset loading started in
		 * {@linkplain NerdScene#preload() NerdScene::preload()} can use. You may change
		 * whether that's the case, by setting
		 * {@linkplain NerdScenesModuleSettings.OnScenePreload#useExecutors
		 * NerdScenesModuleSettings.OnScenePreload::useExecutors}
		 * to {@code true} or {@code false}!
		 *
		 * @apiNote We use <b>{@code 6}</b> threads by default! Enough? Happy?
		 *          I hope you are ":D!~
		 */
		public int maxExecutorThreads = 6;

		/**
		 * If {@linkplain NerdScenesModuleSettings.OnScenePreload#useExecutors
		 * NerdScenesModuleSettings.OnScenePreload::useExecutors} is {@code true}, the
		 * asset loading process in {@linkplain NerdScene#preload()
		 * NerdScene::preload()} is run using multiple threads. Setting <i>this</i> to
		 * {@code true} <i>guarantees</i> that the asset loading will be finished within
		 * {@linkplain NerdScene#preload() NerdScene::preload()}.
		 *
		 * @apiNote {@code true} by default!
		 */
		public boolean completeAssetLoadingWithinPreload = true;

	}

	public class OnSceneSwitch {

		private OnSceneSwitch() {
		}

		/**
		 * If set to {@code -1}, will call {@linkplain NerdSketch#clear()
		 * NerdSketch::clear()} and not
		 * {@linkplain NerdSketch#background() NerdSketch::background()}. <br>
		 * </br>
		 * <b>This is the default behavior!</b>
		 */
		public int clearColor = -1;

		/**
		 * Clears the screen according to
		 * {@linkplain NerdScenesModuleSettings.OnSceneSwitch#clearColor
		 * NerdScenesModuleSettings.OnSceneSwitch::clearColor}.
		 *
		 * @apiNote {@code false} by default.
		 */
		public boolean doClear = false;

		/**
		 * Resets {@linkplain NerdScenesModuleSettings#preFirstCaller
		 * NerdScenesModuleSettings::preFirstCaller},
		 * {@linkplain NerdScenesModuleSettings#drawFirstCaller
		 * NerdScenesModuleSettings::drawFirstCaller}, and
		 * {@linkplain NerdScenesModuleSettings#postFirstCaller
		 * NerdScenesModuleSettings::postFirstCaller} to their
		 * default values!
		 */
		public boolean resetSceneLayerCallbackOrder = true;

	}

	public final NerdScenesModuleSettings.OnSceneSwitch ON_SWITCH = new OnSceneSwitch();

	public final NerdScenesModuleSettings.OnScenePreload ON_PRELOAD = new OnScenePreload();

	// region Callback order specifiers.
	/**
	 * Controls whether {@linkplain NerdScene#pre() NerdScene::pre()} or
	 * {@linkplain NerdLayer#pre() NerdLayer::pre()} is called first by the
	 * {@link NerdScenesModule}. If the value of this field is ever {@code null}, it
	 * is set to its default, {@linkplain NerdSceneLayerCallbackOrder#SCENE
	 * NerdSceneLayerCallbackOrder::SCENE}.
	 */
	public NerdScenesModuleSettings.NerdSceneLayerCallbackOrder preFirstCaller = NerdScenesModuleSettings.NerdSceneLayerCallbackOrder.SCENE;

	/**
	 * Controls whether {@linkplain NerdScene#draw() NerdScene::draw()} or
	 * {@linkplain NerdLayer#draw() NerdLayer::draw()} is called first by the
	 * {@link NerdScenesModule}. If the value of this field is ever{@code null}, it
	 * is set to its default, {@linkplain NerdSceneLayerCallbackOrder#LAYER
	 * NerdSceneLayerCallbackOrder::LAYER}.
	 */
	public NerdScenesModuleSettings.NerdSceneLayerCallbackOrder drawFirstCaller = NerdScenesModuleSettings.NerdSceneLayerCallbackOrder.LAYER;

	/**
	 * Controls whether {@linkplain NerdScene#post() NerdScene::post()} or
	 * {@linkplain NerdLayer#post() NerdLayer::post()} is called first by the
	 * {@link NerdScenesModule}. If the value of this field is ever {@code null}, it
	 * is set to its default, {@linkplain NerdSceneLayerCallbackOrder#LAYER
	 * NerdSceneLayerCallbackOrder::LAYER}.
	 */
	public NerdScenesModuleSettings.NerdSceneLayerCallbackOrder postFirstCaller = NerdScenesModuleSettings.NerdSceneLayerCallbackOrder.LAYER;
	// endregion

	public Set<Class<? extends NerdScene<?>>> classesOfScenesToPreload = new HashSet<>(0);

	public NerdScenesModuleSettings(final Class<? extends NerdScene<?>> p_firstSceneClass) {
		this.FIRST_SCENE_CLASS = p_firstSceneClass;
	}

	@Override
	public Class<NerdScenesModule> getModuleClass() {
		return NerdScenesModule.class;
	}

}
