package org.zephyrsoft.util.gui;

import java.lang.annotation.*;

@Target(value={ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoPopulate {
	// marker
}
