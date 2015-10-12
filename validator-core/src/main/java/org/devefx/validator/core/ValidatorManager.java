package org.devefx.validator.core;

import org.devefx.validator.config.ValidatorConfig;

public class ValidatorManager {
	
	private final Routes routes = new Routes();
	
	public boolean init(ValidatorConfig validatorConfig) {
		validatorConfig.configValidator(routes);
		
		return true;
	}
	
	public Validator getValidator(String url) {
		Validator validator = routes.get(url);
		if (validator != null) {
			return validator;
		}
		return null;
	}
	
}
