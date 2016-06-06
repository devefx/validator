package com.devefx.validation.web;

import com.devefx.validation.constraints.impl.*;
import com.devefx.validation.web.config.Modules;
import com.devefx.validation.web.config.Routes;
import com.devefx.validation.web.config.ValidatorConfig;

/**
 * Config
 * Created by YYQ on 2016/5/31.
 */
class Config {

    private static final Modules modules = new Modules();
    private static final Routes routes = new Routes();

    static {
        modules.add(BooleanValidator.class);
        modules.add(DateValidator.class);
        modules.add(EmailValidator.class);
        modules.add(EqualFieldValidator.class);
        modules.add(FloatValidator.class);
        modules.add(IntegerValidator.class);
        modules.add(LengthValidator.class);
        modules.add(MobileValidator.class);
        modules.add(NotBlankValidator.class);
        modules.add(NotEmptyValidator.class);
        modules.add(NotNullValidator.class);
        modules.add(NullValidator.class);
        modules.add(PatternValidator.class);
        modules.add(SizeValidator.class);
        modules.add(URLValidator.class);
    }

    static boolean configValidator(ValidatorConfig validatorConfig) {
        try {
            validatorConfig.configModules(modules);
            validatorConfig.configRoute(routes);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    static Modules getModules() {
        return modules;
    }

    static Routes getRoutes() {
        return routes;
    }
}
