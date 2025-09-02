package com.stepup.main;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {
    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student("Test");
    }

    @DisplayName("Проверка валидации неверных оценок")
    @Test
    void testAddGrade() {
        student.addGrade(4);
        assertEquals(1, student.getGrades().size());
    }

    @DisplayName("Проверка не корректных оценок")
    @ParameterizedTest
    @MethodSource("com.stepup.main.GradeSource#invalidGrades")
    void testNotCorrectGrades(int invalidGrade) {
        Student stud = new Student("vasia");
        Assertions.assertThrows(IllegalArgumentException.class, () -> stud.addGrade(invalidGrade));
    }

    @DisplayName("Проверка правильных оценок")
    @ParameterizedTest
    @MethodSource("com.stepup.main.GradeSource#validGrades")
    void testValidGrades(int validGrade) {
        Student stud = new Student("vasia");
        stud.addGrade(validGrade);
        assertTrue(stud.getGrades().contains(validGrade));
    }


    @DisplayName("Проверка инкапсуляции для получения списка оценок")
    @Test
    void testEncapsulation() {
        List<Integer> grades = student.getGrades();
        grades.add(5); // Попытка изменить исходный список
        assertTrue(student.getGrades().isEmpty()); // Список должен остаться неизменным
    }

    @DisplayName("Проверка equals")
    @Test
    void testEquals() {
        Student other = new Student("Test");
        other.addGrade(4);
        student.addGrade(4);
        assertTrue(student.equals(other));
    }

    @DisplayName("Проверка hashCode")
    @Test
    void testHashCode() {
        Student other = new Student("Test");
        assertEquals(student.hashCode(), other.hashCode());
    }

    @DisplayName("Проверка toString")
    @Test
    void testToString() {
        // Проверяем формат вывода
        assertEquals("Student{name=Test, marks=[]}", student.toString());

        // Добавляем оценки и проверяем снова
        student.addGrade(4);
        student.addGrade(5);
        assertEquals("Student{name=Test, marks=[4, 5]}", student.toString());
    }

    @DisplayName("Проверка отсутствия регресса при смене имени у студента")
    @Test
    void testSetName() {
        // Проверяем установку нового имени
        student.setName("NewName");
        assertEquals("NewName", student.getName());

        // Проверяем, что другие данные не изменились
        student.addGrade(4);
        student.setName("AnotherName");
        assertEquals(4, student.getGrades().get(0));
    }

    @DisplayName("Проверка getName")
    @Test
    void testGetName() {
        // Проверяем получение имени
        assertEquals("Test", student.getName());

        // Проверяем после изменения
        student.setName("ChangedName");
        assertEquals("ChangedName", student.getName());
    }
}
