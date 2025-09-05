package com.stepup.mock2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StudentMock2Test {
    private Student student;
    private StudentRepository repoMock;

    @BeforeEach
    public void setUp() {
        repoMock = new StudentRepositoryMock();
        student = new Student("Test Student");
        student.setRepo(repoMock);
    }

    @Test
    public void testRating_highSum() {
        student.addGrade(5);
        student.addGrade(5);
        student.addGrade(5);
        student.addGrade(5);
        student.addGrade(5);
        student.addGrade(5);
        student.addGrade(5);
        student.addGrade(5);
        student.addGrade(5);
        student.addGrade(5);
        student.addGrade(5);

        assertEquals(9, student.rating());
    }

    @Test
    public void testRating_lowSum() {
        student.addGrade(4);
        student.addGrade(4);

        assertEquals(10, student.rating());
    }
}
