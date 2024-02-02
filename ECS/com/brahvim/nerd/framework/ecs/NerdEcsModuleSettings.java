package com.brahvim.nerd.framework.ecs;

import com.brahvim.nerd.processing_wrapper.NerdModuleSettings;

import processing.core.PGraphics;

public class NerdEcsModuleSettings<SketchPGraphicsT extends PGraphics>
		extends NerdModuleSettings<SketchPGraphicsT, NerdEcsModule<SketchPGraphicsT>> {

	public Class<? extends NerdEcsSystem<?>>[] ecsSystemsOrder;

	@Override
	@SuppressWarnings("unchecked")
	public <RetModuleClassT extends NerdEcsModule<SketchPGraphicsT>> Class<RetModuleClassT> getNerdModuleClass() {
		return (Class<RetModuleClassT>) NerdEcsModule.class;
	}

}
