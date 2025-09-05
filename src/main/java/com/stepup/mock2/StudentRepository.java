package com.stepup.mock2;

public interface StudentRepository {
    int getRatingForGradeSum(int sum);
    long count();
    void delete(Student entity);
    void deleteAll(Iterable<Student> entities);
    Iterable<Student> findAll();
    Student save(Student entity);
    Iterable<Student> saveAll(Iterable<Student> entities);
}
