function EmailValidator(field, multi, errorCode, errorMessage) {
    FieldValidator.apply(this, [field, errorCode, errorMessage]);
    this.isValid = function(request) {
        var value = request.getParameter(field);
        if (!isEmpty(value)) {
            if (multi) {
                if (value.charAt(value.length - 1) != ";") {
                    value = value + ";";
                }
                return /^([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+;)+$/.test(value);
            }
            return /^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/.test(value);
        }
        return true;
    };
}