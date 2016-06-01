function SizeValidator(field, min, max, errorCode, errorMessage) {
    FieldValidator.apply(this, [field, errorCode, errorMessage]);
    this.isValid = function (request) {
        try {
            var value = request.getParameter(field);
            var val = parseInt(value);
            return val >= min && val <= max;
        } catch (e) {
        }
        return false;
    };
}