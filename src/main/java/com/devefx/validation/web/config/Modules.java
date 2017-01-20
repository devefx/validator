package com.devefx.validation.web.config;

import com.devefx.validation.ConstraintValidator;
import com.devefx.validation.annotation.BindScript;
import com.devefx.validation.kit.ResKit;

import java.io.FileNotFoundException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modules
 * Created by YYQ on 2016/5/30.
 */
public class Modules {

    private static final String SCRIPT_PATH = "com/devefx/validation/script/js/";

    private Logger log = LoggerFactory.getLogger(Routes.class);
    
    private final List<Class<? extends ConstraintValidator>> modules = new ArrayList<Class<? extends ConstraintValidator>>(14);

    private final Map<String, String> scripts = new TreeMap<String, String>(new Comparator<String>() {
        @Override
        public int compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    });

    public Modules add(Class<? extends ConstraintValidator> module) {
        if (module != null) {
            if (modules.contains(module)) {
                throw new IllegalArgumentException("The module already exists: " + module);
            }
            modules.add(module);
            // bind script
            if (module.isAnnotationPresent(BindScript.class)) {
                BindScript bindScript = module.getAnnotation(BindScript.class);
                String path = bindScript.value();
                if (!path.contains("/")) {
                    path = SCRIPT_PATH + path;
                }
                try {
                    String script = ResKit.getResourceAsString(path);
                    scripts.put(module.getSimpleName(), script);
                    if (log.isInfoEnabled()) {
                        log.info("Register module [" + module + "] path [" + path + "]");
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("The file not found: " + path);
                } catch (Exception e) {
                    throw new IllegalArgumentException("The file read error: " + path);
                }
            }
        }
        return this;
    }

    public List<Class<? extends ConstraintValidator>> getModules() {
        return modules;
    }

    public Map<String, String> getScripts() {
        return scripts;
    }

}
