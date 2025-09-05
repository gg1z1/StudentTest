package com.stepup.mock2;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
public class Student {
    @Getter @Setter
    private String name;
    private List<Integer> grades = new ArrayList<>();
    private StudentRepository repo;

    public Student(String name) {
        this.name = name;
    }

    public List<Integer> getGrades() {
        return grades;
    }

    public void addGrade(int grade) {
        if (grade < 2 || grade > 5) {
            throw new IllegalArgumentException(grade + " is wrong grade");
        }
        grades.add(grade);
    }

    public void setRepo(StudentRepository repo) {
        this.repo = repo;
    }

    public int rating() {
        return repo.getRatingForGradeSum(
                grades.stream()
                        .mapToInt(Integer::intValue)
                        .sum()
        );
    }
}
