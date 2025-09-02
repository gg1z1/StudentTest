package com.stepup.main;

import java.util.stream.Stream;

public class GradeSource {
    public static Stream<Integer> invalidGrades() {
        return Stream.of(0, 1, 6, 7);
    }

    public static Stream<Integer> validGrades() {
        return Stream.of(2, 3, 4, 5);
    }
}
