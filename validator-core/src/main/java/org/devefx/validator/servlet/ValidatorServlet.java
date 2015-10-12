package org.devefx.validator.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.devefx.validator.config.ValidatorConfig;
import org.devefx.validator.core.ValidatorManager;
import org.devefx.validator.core.Validator;

public class ValidatorServlet extends HttpServlet {
	private static final long serialVersionUID = -3634086291767542989L;

	private ValidatorConfig validatorConfig;
	
	private static final ValidatorManager validatorManager = new ValidatorManager();

	private int contextPathLength;
	private String validatorJs;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		createValidatorConfig(servletConfig.getInitParameter("configClass"));
		
		String path = servletConfig.getInitParameter("validatorJs");
		if (path == null)
			throw new RuntimeException("Please set validatorJs parameter of ValidatorServlet in web.xml");
		this.validatorJs = path;
		
		if (!validatorManager.init(validatorConfig)) {
			throw new RuntimeException("ValidatorManager init error!");
		}
		
		String contextPath = servletConfig.getServletContext().getContextPath();
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
			validatorConfig = (ValidatorConfig)temp;
		else
			throw new RuntimeException("Can not create instance of class: " + configClass + ". Please check the config in web.xml");
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/plain;charset=UTF-8");
		
		String target = request.getRequestURI();
		if (contextPathLength != 0)
			target = target.substring(contextPathLength);
		
		if (target.equals(validatorJs)) {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("validator.js");
			if (inputStream == null) {
				throw new FileNotFoundException("validator.js file not found!");
			}
			write(inputStream, response.getOutputStream());
			return;
		}
		
		try {
			Validator validator = validatorManager.getValidator(target);
			if (validator != null) {
				validator.output(response.getWriter());
			}
		} catch (Exception e) {
		}
	}
	
	private void write(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buff = new byte[1024];
		int length = 0;
		while((length = inputStream.read(buff)) != -1){
			outputStream.write(buff, 0, length);
		}
		outputStream.flush();
		outputStream.close();
		inputStream.close();
	}
}
