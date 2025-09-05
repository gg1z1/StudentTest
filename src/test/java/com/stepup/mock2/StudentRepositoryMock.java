package com.stepup.mock2;

public class StudentRepositoryMock implements StudentRepository {
    @Override
    public int getRatingForGradeSum(int sum) {
        if (sum > 50) return 9;
        if (sum <= 50) return 10;
        return 0;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(Student entity) {}

    @Override
    public void deleteAll(Iterable<Student> entities) {}

    @Override
    public Iterable<Student> findAll() {
        return null;
    }

    @Override
    public Student save(Student entity) {
        return null;
    }

    @Override
    public Iterable<Student> saveAll(Iterable<Student> entities) {
        return null;
    }
}
