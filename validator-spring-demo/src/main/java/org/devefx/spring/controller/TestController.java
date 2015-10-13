package org.devefx.spring.controller;

import org.devefx.spring.validator.SaveValidator;
import org.devefx.validator.annotation.RequestValidator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController {
	@RequestMapping("/doSave")
	@RequestValidator(SaveValidator.class)
	public void doSave() {
		System.out.println("验证成功，处理请求");
	}
}
