package org.devefx.spring.validator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.devefx.validator.core.Validator;

public class SaveValidator extends Validator {

	@Override
	protected void validate(HttpServletRequest request, HttpServletResponse response) {
		validateRequiredString("nickname", "nickname", "昵称不能为空");
		validateRequiredString("password", "password", "密码不能为空");
		validateString("password", 6, 20, "password", "密码长度在6-20位");
		validateEqualField("password", "pass_again", "pass_again", "两次密码不一致");
		validateRequiredString("email", "email", "邮箱不能为空");
		validateEmail("email", "email", "邮箱格式有误");
		validateRequiredString("mobile", "mobile", "手机号不能为空");
		validateMobile("mobile", "mobile", "手机格式有误");
	}

	@Override
	protected void handleError(HttpServletRequest request, HttpServletResponse response) {
		renderJSONError();
	}

}
