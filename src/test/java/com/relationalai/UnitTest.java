package com.relationalai;

import java.util.function.Predicate;

public abstract class UnitTest {
    static String databaseName = "sdk-test";
    static String engineName = "sdk-test";
    static String oauthClientName = "sdk-test";
    static String userName = "sdk-test@relational.ai";

    // Return the item in the given array that satisfies the given predicate.
    static <T> T find(T[] items, Predicate<T> predicate) {
        for (var item : items) {
            if (predicate.test(item))
                return item;
        }
        return null;
    }
}
