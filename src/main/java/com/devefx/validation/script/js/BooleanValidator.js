function BooleanValidator(field, errorCode, errorMessage) {
    FieldValidator.apply(this, [field, errorCode, errorMessage]);
    this.isValid = function (request) {
        var value = request.getParameter(field).toLowerCase();
        if (!isEmpty(value)) {
            return "1" == value || "0" == value ||
                "true" == value || "false" == value;
        }
        return true;
    };
}