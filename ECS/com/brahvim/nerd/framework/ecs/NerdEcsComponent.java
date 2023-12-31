package com.brahvim.nerd.framework.ecs;

import java.io.Serializable;

public abstract class NerdEcsComponent implements Serializable {

	public static final long serialVersionUID = -144634679663L;

	protected NerdEcsComponent() {
	}

	public abstract <ComponentT extends NerdEcsComponent> void copyFieldsFrom(final ComponentT p_other);

}
