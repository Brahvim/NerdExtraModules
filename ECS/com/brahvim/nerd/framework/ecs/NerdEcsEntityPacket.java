package com.brahvim.nerd.framework.ecs;

import java.io.Serializable;

import processing.core.PGraphics;

/* `package` */ class NerdEcsEntityPacket<SketchPGraphicsT extends PGraphics> implements Serializable {

	@SuppressWarnings("unused")
	private final String NAME;

	@SuppressWarnings("unused")
	private final NerdEcsEntity<SketchPGraphicsT> ENTITY;

	public NerdEcsEntityPacket(final String p_name, final NerdEcsEntity<SketchPGraphicsT> p_entity) {
		this.NAME = p_name;
		this.ENTITY = p_entity;
	}

}
