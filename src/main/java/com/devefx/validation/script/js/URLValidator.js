function URLValidator(field, min, max, errorCode, errorMessage) {
    FieldValidator.apply(this, [field, errorCode, errorMessage]);
    this.isValid = function (request) {
        var value = request.getParameter(field);
        if (!isEmpty(value)) {
            var regex = "^((https|http|ftp|rtsp|mms)?://)"
                + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?"
                + "(([0-9]{1,3}\.){3}[0-9]{1,3}"
                + "|"
                + "([0-9a-z_!~*'()-]+\.)*"
                + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\."
                + "[a-z]{2,6})"
                + "(:[0-9]{1,4})?"
                + "((/?)|"
                + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
            return new RegExp(regex).test(value);
        }
        return true;
    };
}