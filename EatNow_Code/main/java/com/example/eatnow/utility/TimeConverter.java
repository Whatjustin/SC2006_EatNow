package com.example.eatnow.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Class to ensure time is uniformed to SGT since system time is in GMT +0
// SGT is GMT +8 (28800 seconds)
public final class TimeConverter {

    static final int SGT_OFFSET = 28800;

    public static String convertUnixTStoString(long timestamp) {
        DateFormat dateFormat = DateFormat.getDateInstance();
        Date date = new Date((timestamp - SGT_OFFSET) * 1000); // Offset to System Time (GMT +0);
        String dateString = dateFormat.format(date);
        return dateString;
    }

    public static String convertUnixTStoStringFull(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());
        Date date = new Date((timestamp - SGT_OFFSET) * 1000); // Offset to System Time (GMT +0);
        String dateString = dateFormat.format(date);
        return dateString;
    }

    public static Double getSGTUnixTS() {
        return (double) (System.currentTimeMillis() / 1000 + SGT_OFFSET); // Offset to SGT;
    }

    public static long convertOffsetUnixTStoSGT(long timestamp) {
        return (timestamp / 1000 + SGT_OFFSET); // Offset to SGT;
    }

}
