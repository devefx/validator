function Validator(selector, handlerError) {
	var valid = new Object();
	valid.form = $(selector);
	valid.invalid = false;
	valid.shortCircuit = false;
	valid.datePattern = null;
	valid.error = {};
	valid.handlerError = handlerError;
	var DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
	var emailAddressPattern = "\\b(^['_A-Za-z0-9-]+(\\.['_A-Za-z0-9-]+)*@([A-Za-z0-9-])+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z0-9]{2,})|(\\.[A-Za-z0-9]{2,}\\.[A-Za-z0-9]{2,}))$)\\b";
	var mobileNumberPattern = "^(0|86|17951)?(13[0-9]|15[0-9]|17[678]|18[0-9]|14[57])[0-9]{8}$";
	var urlAddressPattern = "";
	
	valid.setShortCircuit = function(shortCircuit) {
		valid.shortCircuit = shortCircuit;
	};
	valid.setDatePattern = function(datePattern) {
		valid.datePattern = datePattern;
	};
	valid.getDatePattern = function() {
		return (valid.datePattern != null ? valid.datePattern : DEFAULT_DATE_PATTERN);
	};
	valid.validate = function () {};
	valid.addError = function(errorKey, errorMessage) {
		valid.invalid = true;
		if(valid.error[errorKey] == null)
			valid.error[errorKey] = errorMessage;
		if (valid.shortCircuit) {
			throw new Error();
		}
	};
	valid.getParameter = function(name) {
		return valid.form.find("[name="+name+"]").val();
	};
	valid.validateRequired = function(field, errorKey, errorMessage) {
		var value = this.getParameter(name);
		if (value == null || "" == value)
			this.addError(errorKey, errorMessage);
	};
	valid.validateRequiredString = function(field, errorKey, errorMessage) {
		var value = this.getParameter(field);
		if (value == null || "" == value.replace(/\s/ig, ""))
			this.addError(errorKey, errorMessage);
	};
	valid.validateInteger = function(field, min, max, errorKey, errorMessage) {
		var value = this.getParameter(field);
		if (value == null || "" == value.replace(/\s/ig, "")) {
			this.addError(errorKey, errorMessage);
			return ;
		}
		var temp = parseInt(value.replace(/\s/ig, ""));
		if (isNaN(temp) || temp < min || temp > max)
			this.addError(errorKey, errorMessage);
	};
	valid.validateInteger = function(field, errorKey, errorMessage) {
		var value = this.getParameter(field);
		if (value == null || "" == value.replace(/\s/ig, "")) {
			this.addError(errorKey, errorMessage);
			return ;
		}
		if(isNaN(parseInt(value.replace(/\s/ig, "")))) {
			this.addError(errorKey, errorMessage);
			return ;
		}
	};
	valid.validateLong = function(field, min, max, errorKey, errorMessage) {
		this.validateInteger(field, min, max, errorKey, errorMessage);
	};
	valid.validateLong = function(field, errorKey, errorMessage) {
		this.validateInteger(field, errorKey, errorMessage);
	};
	valid.validateDouble = function(field, min, max, errorKey, errorMessage) {
		var value = this.getParameter(field);
		if (value == null || "" == value.replace(/\s/ig, "")) {
			this.addError(errorKey, errorMessage);
			return ;
		}
		var temp = parseFloat(value.replace(/\s/ig, ""));
		if (isNaN(temp) || temp < min || temp > max)
			this.addError(errorKey, errorMessage);
	};
	valid.validateDouble = function(field, errorKey, errorMessage) {
		var value = this.getParameter(field);
		if (value == null || "" == value.replace(/\s/ig, "")) {
			this.addError(errorKey, errorMessage);
			return ;
		}
		if (isNaN(parseFloat(value.replace(/\s/ig, ""))))
			this.addError(errorKey, errorMessage);
	};
	valid.validateDate = function(field, errorKey, errorMessage) {
		var value = this.getParameter(field);
		if (value == null || "" == value.replace(/\s/ig, "")) {
			this.addError(errorKey, errorMessage);
			return ;
		}
		
	};
	valid.validateDate = function(field, min, max, errorKey, errorMessage) {
		
	};
	valid.validateEqualField = function(field_1, field_2, errorKey, errorMessage) {
		var value_1 = this.getParameter(field_1);
		var value_2 = this.getParameter(field_2);
		if (value_1 == null || value_2 == null || value_1 != value_2)
			this.addError(errorKey, errorMessage);
	};
	valid.validateEqualString = function(s1, s2, errorKey, errorMessage) {
		if (s1 == null || s2 == null || s1 != s2)
			this.addError(errorKey, errorMessage);
	};
	valid.validateEqualInteger = function(i1, i2, errorKey, errorMessage) {
		this.validateEqualString(i1, i2, errorKey, errorMessage);
	};
	valid.validateEmail = function(field, errorKey, errorMessage) {
		this.validateRegex(field, emailAddressPattern, false, errorKey, errorMessage);
	};
	valid.validateMobile = function(field, errorKey, errorMessage) {
		this.validateRegex(field, mobileNumberPattern, false, errorKey, errorMessage);
	};
	valid.validateUrl = function(field, errorKey, errorMessage) {
		this.validateRegex(field, urlAddressPattern, false, errorKey, errorMessage);
	};
	valid.validateRegex = function(field, regExpression, isCaseSensitive, errorKey, errorMessage) {
		var value = this.getParameter(field);
		if (value == null || "" == value.replace(/\s/ig, "")) {
			this.addError(errorKey, errorMessage);
			return ;
		}
		if (typeof(isCaseSensitive) != "boolean" && errorMessage == undefined) {
			errorMessage = errorKey;
			errorKey = isCaseSensitive;
			isCaseSensitive = true;
		}
		var reg = isCaseSensitive ? new RegExp(regExpression) : new RegExp(regExpression, "i");
		if (!reg.test(value)) {
			this.addError(errorKey, errorMessage);
		}
	};
	valid.validateString = function(field, minLen, maxLen, errorKey, errorMessage) {
		var value = this.getParameter(field);
		if (value == null || "" == value.replace(/\s/ig, "")) {
			this.addError(errorKey, errorMessage);
			return ;
		}
		if (value.length < minLen || value.length > maxLen)
			this.addError(errorKey, errorMessage);
	};
	valid.validateBoolean = function(field, errorKey, errorMessage) {
		var value = this.getParameter(field);
		if (value == null || "" == value.replace(/\s/ig, "")) {
			this.addError(errorKey, errorMessage);
			return ;
		}
		value = value.replace(/\s/ig, "").toLowerCase();
		if ("1" == value || "true" == value)
			return ;
		else if ("0" == value || "false" == value)
			return ;
		this.addError(errorKey, errorMessage);
	};
	return valid;
}