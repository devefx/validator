function PatternValidator(field, regExpression, isCaseSensitive, errorCode, errorMessage) {
    FieldValidator.apply(this, [field, errorCode, errorMessage]);
    this.isValid = function (request) {
        var value = request.getParameter(field);
        if (!isEmpty(value)) {
            var reg = isCaseSensitive ? new RegExp(regExpression) : new RegExp(regExpression, "i");
            return reg.test(value);
        }
        return true;
    };
}