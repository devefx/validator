function EqualFieldValidator(field1, field2, errorCode, errorMessage) {
    FieldValidator.apply(this, [field1, errorCode, errorMessage]);
    this.isValid = function (request) {
        var value1 = request.getParameter(field1);
        var value2 = request.getParameter(field2);
        return value1 == value2;
    };
}