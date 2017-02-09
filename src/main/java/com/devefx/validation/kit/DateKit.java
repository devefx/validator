package com.devefx.validation.kit;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * DateKit.
 */
public class DateKit {

    private static final Map<String, SoftReference<DateFormat>> formatReferenceMap
            = new HashMap<String, SoftReference<DateFormat>>();

    private static DateFormat loadFormat(String pattern) {
        SoftReference<DateFormat> reference = formatReferenceMap.get(pattern);
        if (reference == null || reference.get() == null) {
            DateFormat format = new SimpleDateFormat(pattern);
            formatReferenceMap.put(pattern, new SoftReference<DateFormat>(format));
            return format;
        }
        return reference.get();
    }

    public static String format(String pattern, Date date) {
        return loadFormat(pattern).format(date);
    }

    public static Date parse(String pattern, String source) throws ParseException {
        return loadFormat(pattern).parse(source);
    }

}
