package com.brahvim.nerd.framework.ecs;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import processing.core.PGraphics;

public final class NerdEcsEntity<SketchPGraphicsT extends PGraphics> implements Serializable {

	// region Fields.
	public static final long serialVersionUID = -84636463676L;

	// Nope, no use in keeping the name here!
	// If you want speed, let there be a `HashMap` in `NerdEcsModule`!

	protected final transient NerdEcsModule<SketchPGraphicsT> MANAGER;
	protected final transient NerdEcsEntity<SketchPGraphicsT> ENTITY = this;

	// What is an ECS? A kind of data-oriented design?
	// This is totally not data-oriented design haha. Use an `ArrayList`!!!
	// TODO: CHECK for duplicates! Perhaps even write a subclass for `ArrayList`.
	private final List<NerdEcsComponent> COMPONENTS = new ArrayList<>();
	// endregion

	protected NerdEcsEntity(final NerdEcsModule<SketchPGraphicsT> p_module) {
		this.MANAGER = p_module;
	}

	// region Dynamic component list queries. (PLEASE! No variadic overloads...)
	/**
	 * Checks if a given {@link NerdEcsComponent} exists. As simple as that!
	 *
	 * @return ..if this exact component was held by this entity.
	 */
	public boolean hasComponent(final NerdEcsComponent p_component) {
		if (p_component != null)
			for (final var c : this.COMPONENTS)
				if (c == p_component)
					return true;
		return false;
	}

	/**
	 * Checks if a given {@link NerdEcsComponent} exists, and calls the given
	 * callback, if so.
	 *
	 * @param p_component   is the component to find. Duh.
	 * @param p_taskIfFound is the task performed if the component is found.
	 * @return ..if this exact component was held by this entity.
	 */
	public <ComponentT extends NerdEcsComponent> boolean hasComponent(
			final ComponentT p_component,
			final Consumer<ComponentT> p_taskIfFound) {
		if (p_component != null)
			for (final var c : this.COMPONENTS)
				if (c == p_component) {
					p_taskIfFound.accept(p_component);
					return true;
				}
		return false;
	}

	/**
	 * Checks if a given {@link NerdEcsComponent} exists. No matter it does or not,
	 * your callbacks are always called with the one you submitted!
	 *
	 * @param p_component      is the component to find. Duh.
	 * @param p_taskIfFound    is the task performed if the component is found.
	 * @param p_taskIfNotFound is the task performed if the component is not found.
	 * @return ..if this exact component was held by this entity.
	 */
	public <ComponentT extends NerdEcsComponent> boolean hasComponent(
			final ComponentT p_component,
			final Runnable p_taskIfNotFound,
			final Consumer<ComponentT> p_taskIfFound) {
		if (p_component != null)
			for (final var c : this.COMPONENTS)
				if (c == p_component) {
					p_taskIfFound.accept(p_component);
					return true;
				}

		p_taskIfNotFound.run();
		return false;
	}

	/**
	 * Tells if <i>absolutely <b>any</b></i> instance of the provided subclass of
	 * {@link NerdEcsComponent} is held by this entity.
	 *
	 * @param p_componentClass is the {@link Class} you need to pass!
	 *                         (Usually {@code ClassName.class}
	 *                         or {@code someObject.getClass()})
	 */
	public boolean hasComponentOfClass(final Class<? extends NerdEcsComponent> p_componentClass) {
		if (p_componentClass != null)
			for (final var c : this.COMPONENTS)
				if (c.getClass() == p_componentClass)
					return true;

		return false;
	}

	/**
	 * Tells if <i>absolutely <b>any</b></i> instance of the provided subclass of
	 * {@link NerdEcsComponent} is held by this entity.
	 *
	 * @param p_componentClass is the {@link Class} you need to pass!
	 *                         (Usually {@code ClassName.class}
	 *                         or {@code someObject.getClass()})
	 * @param p_taskIfFound    is performed if a component of the given type exists.
	 */
	@SuppressWarnings("unchecked")
	public <ComponentT extends NerdEcsComponent> boolean hasComponent(
			final Class<ComponentT> p_componentClass,
			final Consumer<ComponentT> p_taskIfFound) {
		if (p_componentClass != null)
			for (final var c : this.COMPONENTS)
				if (c.getClass() == p_componentClass) {
					p_taskIfFound.accept((ComponentT) c);
					return true;
				}

		return false;
	}

	/**
	 * Tells if <i>absolutely <b>any</b></i> instance of the provided subclass of
	 * {@link NerdEcsComponent} is held by this entity.
	 *
	 * @param p_componentClass is the {@link Class} you need to pass!
	 *                         (Usually {@code ClassName.class}
	 *                         or {@code someObject.getClass()})
	 * @param p_taskIfFound    is performed if a component of the given type exists.
	 * @param p_taskIfNotFound is performed if a component of the given type does
	 *                         not exist.
	 */
	@SuppressWarnings("unchecked")
	public <ComponentT extends NerdEcsComponent> boolean hasComponent(
			final Class<ComponentT> p_componentClass,
			final Runnable p_taskIfNotFound,
			final Consumer<ComponentT> p_taskIfFound) {
		if (p_componentClass != null)
			for (final var c : this.COMPONENTS)
				if (c.getClass() == p_componentClass) {
					p_taskIfFound.accept((ComponentT) c);
					return true;
				}

		p_taskIfNotFound.run();
		return false;
	}

	@SuppressWarnings("unchecked")
	public <ComponentT extends NerdEcsComponent> ComponentT getComponent(final Class<ComponentT> p_componentClass) {
		for (final var c : this.COMPONENTS)
			if (c.getClass().equals(p_componentClass))
				return (ComponentT) c;

		return null;
	}

	public <ComponentT extends NerdEcsComponent> ComponentT attachComponent(final Class<ComponentT> p_componentClass) {
		ComponentT toRet = null;

		// region Construction!
		try {
			toRet = p_componentClass.getDeclaredConstructor().newInstance();
		} catch (final InstantiationException | SecurityException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace();
		}
		// endregion

		if (toRet != null) {
			this.COMPONENTS.add(toRet);
			this.MANAGER.addComponent(toRet);
		}

		return toRet;
	}

	public void removeAllComponentsOfCondition(final Predicate<NerdEcsComponent> p_ifStatement) {
		final List<NerdEcsComponent> toRemove = new ArrayList<>();

		for (final var c : this.COMPONENTS)
			if (p_ifStatement.test(c))
				toRemove.add(c);

		this.COMPONENTS.removeAll(toRemove);
	}

	public void removeAllComponentsTyped(final Class<? extends NerdEcsComponent> p_componentClass) {
		final List<NerdEcsComponent> toRemove = new ArrayList<>();

		for (final var c : this.COMPONENTS)
			if (c.getClass().equals(p_componentClass))
				toRemove.add(c);

		this.COMPONENTS.removeAll(toRemove);
	}

	@SuppressWarnings("unchecked")
	public <ComponentT extends NerdEcsComponent> ComponentT removeComponent(final Class<ComponentT> p_componentClass) {
		ComponentT toRet = null;

		for (final var c : this.COMPONENTS)
			if (c.getClass().equals(p_componentClass))
				toRet = (ComponentT) c;

		if (toRet != null) {
			this.COMPONENTS.remove(toRet);
			this.MANAGER.removeComponent(toRet);
		}

		return toRet;
	}

	/**
	 * @param <ComponentT>     The type of the component - any
	 *                         {@link NerdEcsComponent}.
	 * @param p_componentClass is the component's respective {@link Class}.
	 * @return {@code null} if the component already exists.
	 */
	public <ComponentT extends NerdEcsComponent> Optional<ComponentT> attachComponentIfAbsent(
			final Class<ComponentT> p_componentClass) {
		if (!(p_componentClass == null || this.hasComponentOfClass(p_componentClass)))
			return Optional.of(this.attachComponent(p_componentClass));
		else
			return Optional.empty();
	}
	// endregion

}