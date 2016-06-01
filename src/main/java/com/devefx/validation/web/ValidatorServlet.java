package com.devefx.validation.web;

import com.devefx.validation.ConstraintValidator;
import com.devefx.validation.Validator;
import com.devefx.validation.annotation.BindScript;
import com.devefx.validation.constraints.impl.*;
import com.devefx.validation.kit.ResKit;
import com.devefx.validation.web.config.Modules;
import com.devefx.validation.web.config.Routes;
import com.devefx.validation.web.config.ValidatorConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ValidatorServlet
 * Created by YYQ on 2016/5/30.
 */
public class ValidatorServlet extends HttpServlet {
    private static final long serialVersionUID = -3634086291767542989L;
    private static final String CONTENT_TYPE = "application/x-javascript; charset=utf-8";
    private static final String VALIDATOR_PATH = "com/devefx/validation/script/js/Validator.js";

    private ValidatorConfig validatorConfig;
    private int contextPathLength;
    private String url;

    private Routes routes;
    private Modules modules;

    @Override
    public void init(ServletConfig config) throws ServletException {
        createValidatorConfig(config.getInitParameter("configClass"));

        String path = config.getInitParameter("url");
        if (path == null)
            throw new RuntimeException("Please set url parameter of ValidatorServlet in web.xml");
        url = path;

        if (!Config.configValidator(validatorConfig))
            throw new RuntimeException("Config init error!");

        routes = Config.getRoutes();
        modules = Config.getModules();

        String contextPath = config.getServletContext().getContextPath();
        contextPathLength = (contextPath == null || "/".equals(contextPath) ? 0 : contextPath.length());
    }

    private void createValidatorConfig(String configClass) {
        if (configClass == null)
            throw new RuntimeException("Please set configClass parameter of ValidatorServlet in web.xml");
        Object temp = null;
        try {
            temp = Class.forName(configClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can not create instance of class: " + configClass, e);
        }
        if (temp instanceof ValidatorConfig)
            validatorConfig = (ValidatorConfig) temp;
        else
            throw new RuntimeException("Can not create instance of class: " + configClass + ". Please check the config in web.xml");
    }

    private static SoftReference<String> script;

    private String buildScript() throws IOException {
        if (script == null) {
            boolean first = true;
            StringBuffer buf = new StringBuffer(ResKit.getResourceAsString(VALIDATOR_PATH));
            for (Map.Entry<String, String> entry : modules.getScripts().entrySet()) {
                if (first == false) {
                    buf.append("\n");
                }
                first = false;
                buf.append(entry.getValue());
            }
            script = new SoftReference<String>(buf.toString());
        }
        return script.get();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String target = request.getRequestURI();
        if (contextPathLength != 0)
            target = target.substring(contextPathLength);

        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        try {
            if (target.equals(url)) {
                out.write(buildScript());
            } else {
                Validator validator = routes.get(target);
                if (validator != null) {
                    validator.output(out);
                }
            }
        } finally {
            out.flush();
        }
    }
}
