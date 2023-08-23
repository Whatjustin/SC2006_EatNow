package com.example.eatnow.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Class to handle arraylist to convert arraylist to string, vice versa
public final class ListConverter {

    public static ArrayList<Integer> convertStringToInts(String target) {
        ArrayList<Integer> integers = new ArrayList<>();
        int integer;
        List<String> splitted;
        splitted = new ArrayList<>(Arrays.asList(target.split(",")));
        for (String split : splitted) {
            if (!split.isEmpty()) {
                integer = Integer.parseInt(split);
                integers.add(integer);
            }
        }
        return integers;
    }

    public static String convertIntsToString(ArrayList<Integer> integers) {
        return android.text.TextUtils.join(",", integers);
    }

    public static ArrayList<String> convertStringToListByComma(String target) {
        ArrayList<String> results = new ArrayList<>();

        ArrayList<String> splitted = new ArrayList<>(Arrays.asList(target.split(",")));
        for (String split : splitted) {
            if (!split.isEmpty()) {
                results.add(split);
            }
        }

        return results;
    }

    public static String convertStringListToString(ArrayList<String> strings) {
        return android.text.TextUtils.join(",", strings);
    }
}
