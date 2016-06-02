function SizeValidator(field, min, max, errorCode, errorMessage) {
    FieldValidator.apply(this, [field, errorCode, errorMessage]);
    this.isValid = function (request) {
        var value = request.getParameter(field);
        if (!isEmpty(value)) {
            var val = parseInt(value);
            return !isNaN(val) && val >= min && val <= max;
        }
        return true;
    };
}