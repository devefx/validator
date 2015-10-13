package org.devefx.struts.action;

import org.devefx.struts.validator.SaveValidator;
import org.devefx.validator.annotation.RequestValidator;

import com.opensymphony.xwork2.ActionSupport;

public class TestAction extends ActionSupport {
	
	private static final long serialVersionUID = -6739593701085960358L;
	
	@RequestValidator(SaveValidator.class)
	public void doSave() {
		System.out.println("验证成功，处理请求");
	}
}
