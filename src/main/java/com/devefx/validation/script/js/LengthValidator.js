function LengthValidator(field, minLen, maxLen, errorKey, errorMessage) {
    FieldValidator.apply(this, [field, errorKey, errorMessage]);
    this.isValid = function(request) {
        var value = request.getParameter(field);
        if (value != null) {
            var len = value.length;
            return len >= minLen && len <= maxLen;
        }
        return false;
    };
}