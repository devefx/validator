function PatternValidator(field, regExpression, isCaseSensitive, errorCode, errorMessage) {
    FieldValidator.apply(this, [field, errorCode, errorMessage]);
    this.isValid = function (request) {
        var value = request.getParameter(field);
        if (value != null) {
            var reg = isCaseSensitive ? new RegExp(regExpression) : new RegExp(regExpression, "i");
            return reg.test(value);
        }
        return false;
    };
}