package com.android.vibeo.helper;

/**
 * Created by Grigory Azaryan on 12/1/19.
 */

public class Utils {


    public static int meanOfArray(byte[] arr) {
        int sum = 0;
        for (byte i : arr) {
            sum += i;
        }
        return sum / arr.length;
    }
}
