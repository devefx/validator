/**
 * Validator 2.0
 * Created by YYQ on 2016/5/30.
 */
function Error(code, message) {
    this.code = code;
    this.message = message;
}

function ConstraintValidator(errorCode, errorMessage) {
    this.error = new Error(errorCode, errorMessage);
    this.isValid = function (request) {
        return false;
    };
}
function FieldValidator(field, errorCode, errorMessage) {
    ConstraintValidator.apply(this, [errorCode, errorMessage]);
    this.field = field;
}

function ValidatorManager() {
    this.validatorMap = {};
    this.get = function (name) {
        if (name == undefined) {
            var count = 0;
            for (var key in this.validatorMap) {
                count++;
                if (count > 1) {
                    throw new Error("// TODO");
                }
                name = key;
            }
        }
        return this.validatorMap[name];
    };
    this.register = function (name, validator) {
        this.validatorMap[name] = validator;
    };
}

function Validator() {
    // Property
    this.validators = [];
    this.invalid = false;
    this.shortCircuit = false;
    this.errors = {};
    // Method
    this.setShortCircuit = function (shortCircuit) {
        this.shortCircuit = shortCircuit;
    };
    this.add = function (constraintValidator) {
        this.validators.push(constraintValidator);
    };
    this.reset = function () {
        this.invalid = false;
        this.errors = {};
    };
    this.process = function (request) {
        for (var i = 0; i < this.validators.length; i++) {
            var validator = this.validators[i];
            if (!validator.isValid(request)) {
                this.invalid = true;
                var error = validator.error;
                if (!this.errors.hasOwnProperty(error.code)) {
                    this.errors[error.code] = error.message;
                }
                if (this.shortCircuit) {
                    break;
                }
            }
        }
        return !this.invalid;
    };
    this.checkField = function (request, field) {
        for (var i = 0; i < this.validators.length; i++) {
            var validator = this.validators[i];
            if (validator.field == field && !validator.isValid(request)) {
                return validator.error;
            }
        }
        return null;
    };

}
/**
 * SimpleDateFormat
 * Created by YYQ on 2016/5/30.
 */
function SimpleDateFormat(pattern) {
    this.pattern = pattern;
    this.regex = new RegExp("^" + pattern.replace("yyyy", "\\d{4}").replace("MM", "(0\\d|1[12])").replace("dd", "([0-2]\\d|3[0-1])")
            .replace("HH", "([0-1]\\d|2[0-3])").replace("hh", "(0\\d|1[0-2])").replace("mm", "[0-5]\\d").replace("ss", "[0-5]\\d") + "$");
    this.position = {
        year: pattern.indexOf("yyyy"), month: pattern.indexOf("MM"), day: pattern.indexOf("dd"),
        hour: pattern.toLowerCase().indexOf("hh"), minute: pattern.indexOf("mm"), second: pattern.indexOf("ss")
    };
    this.parse = function (source) {
        if (!this.regex.test(source))
            throw new Error("Unparseable date: \"" + source + "\"");
        var time = {
            year: source.substr(this.position.year, 4),
            month: source.substr(this.position.month, 2),
            day: source.substr(this.position.day, 2)
        };
        if (this.position.hour != -1)
            time.hour = source.substr(this.position.hour, 2);
        if (this.position.minute != -1)
            time.minute = source.substr(this.position.minute, 2);
        if (this.position.second != -1)
            time.second = source.substr(this.position.second, 2);
        var day31 = "01,03,05,07,08,10,12";
        if (time.day == 31 && day31.indexOf(time.month) == -1)
            throw new Error("Unparseable date: \"" + source + "\"");
        if (time.month == 2 && time.day == 29 && !(time.year % 4 == 0 && time.year % 100 != 0)
            && !(time.year % 100 == 0 && time.year % 400 == 0)) {
            throw new Error("Unparseable date: \"" + source + "\"");
        }
        var date = new Date();
        date.setFullYear(time.year, time.month - 1, time.day);
        if (time.hour != undefined) date.setHours(time.hour);
        if (time.minute != undefined) date.setMinutes(time.minute);
        if (time.second != undefined) date.setSeconds(time.second);
        return date;
    };
    this.format = function (date) {
        function fmt(v, n) {
            for (var i = n - (v + "").length; i > 0; i--) {
                v = "0" + v;
            }
            return v;
        }
        var h24 = date.getHours();
        return this.pattern.replace("yyyy", fmt(date.getFullYear(), 4)).replace("MM", fmt(date.getMonth() + 1, 2))
            .replace("dd", fmt(date.getDate(), 2)).replace("HH", fmt(h24, 2)).replace("hh", fmt((h24 - 1) % 12 + 1, 2))
            .replace("mm", fmt(date.getMinutes(), 2)).replace("ss", fmt(date.getSeconds(), 2));
    };
}
/**
 * use jQuery
 */
function Request(form) {
    this.form = form;
    this.getParameter = function (name) {
        try {
            return this.form.findInputByName(name).val();
        } catch (e) {
            return null;
        }
    }
}
/**
 * Bind to jQuery
 */
(function ($) {
    $.fn.extend({
        // support 4 parameters
        validator: function (params) {
            var validator = new (validatorManager.get(params.name));
            validator.setup();

            var selector = this;
            var request = new Request(selector);
            selector.submit(function () {
                validator.reset();
                if (validator.process(request)) {
                    return true == params.success();
                }
                params.error(validator.errors, null);
                return false;
            });
            // auto validator
            selector.find(":input[name]").each(function (i, input) {
                var bindEvent = null;
                switch (input.localName) {
                    case "select":
                        bindEvent = "change";
                        break;
                    case "textarea":
                        bindEvent = "keyup blur";
                        break;
                    case "input":
                        switch (input.type) {
                            case "text":
                            case "password":
                                bindEvent = "keyup blur";
                                break;
                            case "checkbox":
                            case "radio":
                            case "file":
                                bindEvent = "click";
                                break;
                        }
                }
                $(input).bind(bindEvent, function() {
                    params.error(validator.checkField(request, this.name), this.name);
                });
            });
        },
        commit: function(callback) {
            $.ajax({
                url: this.attr("action"),
                type: this.attr("method"),
                data: this.serialize(),
                dataType: "json",
                success: callback
            });
        },
        findInputByName: function (name) {
            return this.find("input[name=" + name + "]");
        }
    });
    window.validatorManager = new ValidatorManager();
})(jQuery);

/**
 * Modules
 */
