package org.devefx.spring.validator.config;

import org.devefx.spring.validator.SaveValidator;
import org.devefx.validator.config.ValidatorConfig;
import org.devefx.validator.core.Routes;

public class DefaultConfig extends ValidatorConfig {

	@Override
	public void configValidator(Routes routes) {
		routes.setBasePath("/validator");
		routes.add("saveValidator.js", SaveValidator.class);
	}

}
