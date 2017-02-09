package com.devefx.validation.web;

import com.devefx.validation.Validator;
import com.devefx.validation.kit.ResKit;
import com.devefx.validation.web.config.Modules;
import com.devefx.validation.web.config.Routes;
import com.devefx.validation.web.config.ValidatorConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ValidatorServlet
 * Created by YYQ on 2016/5/30.
 */
public class ValidatorServlet extends HttpServlet {
    private static final long serialVersionUID = -3634086291767542989L;
    private static final String CONTENT_TYPE = "application/x-javascript; charset=utf-8";
    private static final String VALIDATOR_PATH = "com/devefx/validation/script/js/Validator.js";
    private static final String VALIDATOR_MODULE = "/validator/module/";
    
    private static final String STATUS_SUCCESS = "success";
    private static final String STSTUS_FAILURE = "failure";
    
    private Logger log = LoggerFactory.getLogger(ValidatorServlet.class);
    
    private String name;
    
    private ValidatorConfig validatorConfig;
    private int contextPathLength;
    private String url;

    private Routes routes;
    private Modules modules;

    @Override
    public void init(ServletConfig config) throws ServletException {
        createValidatorConfig(config.getInitParameter("configClass"));

        name = config.getServletName();
        
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

    private static String script;

    private String buildScript() throws IOException {
        if (script == null) {
            StringBuffer buf = new StringBuffer(ResKit.getResourceAsString(VALIDATOR_PATH));
            for (Map.Entry<String, String> entry : modules.getScripts().entrySet()) {
                buf.append("\n");
                buf.append(entry.getValue());
            }
            script = buf.toString();
        }
        return script;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String target = request.getRequestURI();
        if (contextPathLength != 0)
            target = target.substring(contextPathLength);

        if (log.isInfoEnabled()) {
            String method = request.getMethod().toUpperCase();
            log.info("ValidatorServlet with name '" + name + "' processing " + method + " request for [" + target + "]");
        }
        
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        try {
            if (target.equals(url)) {
                out.write(buildScript());
            } else if (target.startsWith(VALIDATOR_MODULE)) {
                moduleValidateService(request, response, target);
            } else {
                Validator validator = routes.get(target);
                if (validator != null) {
                    validator.output(out);
                } else {
                    if (log.isInfoEnabled()) {
                        log.info("ValidatorServlet with name '" + name + "' not found request for [" + target + "]");
                    }
                    response.sendError(404);
                }
            }
        } finally {
            out.flush();
        }
    }
    
    protected void moduleValidateService(HttpServletRequest request,
            HttpServletResponse response, String target) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        try {
            String name = target.substring(VALIDATOR_MODULE.length());
            Matcher matcher = Pattern.compile("^(\\d+)/(\\d+)$").matcher(name);
            if (matcher.find()) {
                int validId = Integer.parseInt(matcher.group(1));
                int moduleId = Integer.parseInt(matcher.group(2));
                Validator validator = routes.get(validId);
                if (validator != null) {
                    if (validator.moduleValidate(moduleId, request, response)) {
                        out.write(STATUS_SUCCESS);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.flush();
        }
        out.write(STSTUS_FAILURE);
    }
    
}
