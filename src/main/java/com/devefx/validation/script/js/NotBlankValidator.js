function NotBlankValidator(field, errorCode, errorMessage) {
    FieldValidator.apply(this, [field, errorCode, errorMessage]);
    this.isValid = function (request) {
        var value = request.getParameter(field);
        return value != null && value.length != 0;
    };
}