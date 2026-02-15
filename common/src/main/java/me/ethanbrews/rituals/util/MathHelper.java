package me.ethanbrews.rituals.util;

public class MathHelper {
    public static int roundUpToMultiple(int n, int y) {
        return ((n + y - 1) / y) * y;
    }
}
