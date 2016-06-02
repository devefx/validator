function DateValidator(field, minDate, maxDate, pattern, errorCode, errorMessage) {
    FieldValidator.apply(this, [field, errorCode, errorMessage]);
    this.isValid = function(request) {
        var value = request.getParameter(field);
        if (!isEmpty(value)) {
            var sdf = new SimpleDateFormat(pattern);
            var time = sdf.parse(value).getTime();
            return time >= sdf.parse(minDate).getTime() &&
                time <= sdf.parse(maxDate).getTime();
        }
        return true;
    };
}