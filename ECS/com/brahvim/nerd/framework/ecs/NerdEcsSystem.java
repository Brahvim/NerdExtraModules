package com.brahvim.nerd.framework.ecs;

import java.io.Serializable;
import java.util.Set;

import com.brahvim.nerd.utils.NerdReflectionUtils;

public abstract class NerdEcsSystem<SystemComponentT extends NerdEcsComponent> implements Serializable {

	public static final long serialVersionUID = -481949954L;

	private final Class<SystemComponentT> COMPONENT_TYPE_CLASS;

	@SuppressWarnings("unchecked")
	protected NerdEcsSystem() {
		final Class<?> typeArg = NerdReflectionUtils.getFirstTypeArg(this);

		if (typeArg != null)
			throw new IllegalStateException(String.format(
					"`%s`s shouldn't be able to come to this state! Did you modify the source code...?",
					NerdEcsSystem.class.getSimpleName()));

		this.COMPONENT_TYPE_CLASS = (Class<SystemComponentT>) typeArg;
	}

	public final Class<SystemComponentT> getComponentTypeClass() {
		return this.COMPONENT_TYPE_CLASS;
	}

	protected void sceneChanged(final Set<SystemComponentT> p_components) {
	}

	// region Sketch workflow callbacks.
	protected void preSetup(final Set<SystemComponentT> p_components) {
	}

	protected void setup(final Set<SystemComponentT> p_components) {
	}

	protected void postSetup(final Set<SystemComponentT> p_components) {
	}

	protected void pre(final Set<SystemComponentT> p_components) {
	}

	protected void preDraw(final Set<SystemComponentT> p_components) {
	}

	protected void draw(final Set<SystemComponentT> p_components) {
	}

	protected void postDraw(final Set<SystemComponentT> p_components) {
	}

	protected void post(final Set<SystemComponentT> p_components) {
	}

	protected void exit(final Set<SystemComponentT> p_components) {
	}

	protected void dispose(final Set<SystemComponentT> p_components) {
	}
	// endregion

	// region Events.
	// region Mouse events.
	protected void mousePressed(final Set<SystemComponentT> p_components) {
	}

	protected void mouseReleased(final Set<SystemComponentT> p_components) {
	}

	protected void mouseMoved(final Set<SystemComponentT> p_components) {
	}

	protected void mouseClicked(final Set<SystemComponentT> p_components) {
	}

	protected void mouseDragged(final Set<SystemComponentT> p_components) {
	}

	protected void mouseWheel(final processing.event.MouseEvent p_mouseEvent,
			final Set<SystemComponentT> p_components) {
	}
	// endregion

	// region Keyboard events.
	protected void keyTyped(final Set<SystemComponentT> p_components) {
	}

	protected void keyPressed(final Set<SystemComponentT> p_components) {
	}

	protected void keyReleased(final Set<SystemComponentT> p_components) {
	}
	// endregion

	// region Touch events.
	protected void touchStarted(final Set<SystemComponentT> p_components) {
	}

	protected void touchMoved(final Set<SystemComponentT> p_components) {
	}

	protected void touchEnded(final Set<SystemComponentT> p_components) {
	}
	// endregion

	// region Window focus events.
	protected void focusLost(final Set<SystemComponentT> p_components) {
	}

	protected void resized(final Set<SystemComponentT> p_components) {
	}

	protected void focusGained(final Set<SystemComponentT> p_components) {
	}

	protected void monitorChanged(final Set<SystemComponentT> p_components) {
	}

	protected void fullscreenChanged(final boolean p_state, final Set<SystemComponentT> p_components) {
	}
	// endregion
	// endregion

}
