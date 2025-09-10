package com.stepup.mock2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class RestTests {

    //java -jar "C:\Users\artim\Downloads\web service for testing\RestApp.jar" --server.port=8080
    private static final String BASE_URL = "http://localhost:8080";
    private static final String STUDENT_ENDPOINT = BASE_URL + "/student";
    private static final String TOP_STUDENT_ENDPOINT = BASE_URL + "/topStudent";


    @BeforeEach
    public void setUp() {
        // Простая очистка без проверок статуса
        for (int i = 1; i <= 100; i++) {
            try {
                given()
                        .when()
                        .delete(STUDENT_ENDPOINT + "/" + i);
            } catch (Exception e) {
                // Пропускаем все ошибки
            }
        }
    }

    private void createStudent(int id, String name, int[] marks) {
        // Формируем массив оценок в правильном формате
        String marksArray = Arrays.toString(marks)
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "");

        // Создаем JSON с правильным форматированием
        String jsonBody = String.format(
                "{\"id\":%d,\"name\":\"%s\",\"marks\":[%s]}",
                id,
                name,
                marksArray
        );

        System.out.println(jsonBody);

        given()
                .contentType("application/json")  // Добавляем заголовок Content-Type
                .body(jsonBody)
        .when()
                .post(STUDENT_ENDPOINT)
        .then()
                .statusCode(201);  // Добавляем логирование для отладки
    }

    // Перегруженная версия без ID
    private Response createStudent(String name, int[] marks) {
        String marksArray = Arrays.toString(marks)
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "");

        String jsonBody = String.format(
                "{\"name\":\"%s\",\"marks\":[%s]}",
                name,
                marksArray
        );

        System.out.println(jsonBody);

        return given()
                .contentType("application/json")
                .body(jsonBody)
                .when()
                .post(STUDENT_ENDPOINT)
                .then()
                .statusCode(201)
                .extract()
                .response();
    }


    public void clearDatabase() {
        // Предварительно создаем студента
        for (int i = 1; i <= 100; i++) {
            try {
                given()
                        .when()
                        .delete(STUDENT_ENDPOINT + "/" + i);
            } catch (Exception e) {
                // Пропускаем все ошибки
            }
        }
    }


    // Тесты get /student/{id}
    // 1. get /student/{id} возвращает JSON студента с указанным ID и заполненным именем, если такой есть в базе, код 200.
    @DisplayName("1.testGetExistingStudent Проверка 200 get /student/{id}")
    @Test
    public void testGetExistingStudent() {
        // Предварительно создаем студента
        createStudent(1, "Test Student", new int[]{4, 5});

        given()
                .baseUri(BASE_URL)
                .pathParam("id", 1)
        .when()
                .get(STUDENT_ENDPOINT + "/{id}")
        .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("Test Student"))
                .body("marks", hasItems(4, 5));

    }

    //2.	get /student/{id} возвращает код 404, если студента с данным ID в базе нет.
    @DisplayName("2.testGetNonExistingStudent Проверка 404 get /student/{id}")
    @Test
    public void testGetNonExistingStudent() {
        given()
                .when()
                .get(STUDENT_ENDPOINT + "/999")
                .then()
                .statusCode(404);
    }

    // Тесты для POST /student
    //3.	post /student добавляет студента в базу, если студента с таким ID ранее не было, при этом имя заполнено, код 201
    @DisplayName("3.testPostNewStudent Проверка 201 post /student")
    @Test
    public void testPostNewStudent() {
        String studentName = "New Student";
        int studentId = 3;

        createStudent(studentId, studentName, new int[]{});

        given()
                .baseUri(BASE_URL)
                .pathParam("id", studentId)
        .when()
                .get(STUDENT_ENDPOINT + "/{id}")
        .then()
                .statusCode(200)
                .body("name", equalTo(studentName))
                .body("id", notNullValue())
                .body("id", equalTo(studentId));

    }

    //4.	post /student обновляет студента в базе, если студент с таким ID ранее был, при этом имя заполнено, код 201.
    @DisplayName("4.testPostUpdateStudent Проверка 201 post /student")
    @Test
    public void testPostUpdateExistingStudent() {
        // Создаем начального студента
        int studentId = 4;
        createStudent(studentId, "Old Name", new int[]{4, 5, 4});

        // Получаем информацию о созданном студенте
        Response initialStudent = given()
                .baseUri(BASE_URL)
                .pathParam("id", studentId)
        .when()
                .get(STUDENT_ENDPOINT + "/{id}")
        .then()
                .statusCode(200)
                .extract().response();

        // Обновляем данные студента
        String newName = "Updated Name";
        int[] newMarks = {5, 5, 5};
        createStudent(studentId, newName, newMarks);

        // Проверяем, что студент обновился
        given()
                .baseUri(BASE_URL)
                .pathParam("id", studentId)
        .when()
                .get(STUDENT_ENDPOINT + "/{id}")
        .then()
                .statusCode(200)
                .body("id", equalTo(studentId))
                .body("name", equalTo(newName))
                .body("marks", equalTo(
                        Arrays.stream(newMarks)
                                .boxed()
                                .collect(Collectors.toList())))
                .body("marks", hasSize(newMarks.length))
                .body("name", not(equalTo(initialStudent.jsonPath().getString("name"))));
    }

    //5.	post /student добавляет студента в базу, если ID null, то возвращается назначенный ID, код 201.
    @DisplayName("5.testPostCreateStudentWithoutId Проверка 201 post /student")
    @Test
    void testPostCreateStudentWithoutId() {
        // Данные для создания студента
        String studentName = "AutoId Student";
        int[] marks = {4, 5, 4};

        // Создаем студента и получаем ID
        int createdId = createStudent(studentName, marks).jsonPath().getInt("");

        // Проверяем, что ID был успешно назначен
        Assertions.assertTrue(createdId > 0, "ID должен быть положительным числом");

        // Проверяем корректность создания студента
        given()
                .baseUri(BASE_URL)
                .pathParam("id", createdId)
        .when()
                .get(STUDENT_ENDPOINT + "/{id}")
        .then()
                .statusCode(200)
                .body("id", equalTo(createdId))
                .body("name", equalTo(studentName))
                .body("marks", equalTo(
                        Arrays.stream(marks)
                                .boxed()
                                .collect(Collectors.toList())
                ));
    }


    //6.	post /student возвращает код 400, если имя не заполнено.
    @DisplayName("6.testCreateStudentWithEmptyName Проверка 400 post /student")
    @Test
    void testCreateStudentWithEmptyName() {
        // Подготавливаем данные для теста
        int[] marks = {4, 5, 4};

        // Тест 1: Пустое имя
        //testInvalidNameCreation("", marks, "Пустое имя");

        // Тест 2: Пробел в имени
        //testInvalidNameCreation("   ", marks, "Имя состоит только из пробелов");

        // Тест 3: Null вместо имени
        testInvalidNameCreation(null, marks, "Имя не передано");
    }

    private void testInvalidNameCreation(String name, int[] marks, String description) {

        String jsonBody = createJsonBody(name, marks);
        System.out.println(jsonBody);

        try {
            given()
                    .contentType("application/json")
                    .body(jsonBody)
            .when()
                    .post(STUDENT_ENDPOINT)
            .then()
                    .statusCode(400)  // Ожидаем ошибку 400
                    .body("error", containsString("Bad Request"));
        } catch (AssertionError e) {
            throw new AssertionError("Тест не прошел для случая: " + description, e);
        }
    }

    private String createJsonBody(String name, int[] marks) {
        String marksArray = Arrays.toString(marks)
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "");

        return String.format(
                "{\"name\":%s,\"marks\":%s}",
                name == null ? "null" : "\"" + name + "\"",
                marksArray
        );
    }

    // 7.	delete /student/{id} удаляет студента с указанным ID из базы, код 200.
    @DisplayName("7.testDeleteExistingStudent Проверка 200 delete /student/{id}")
    @Test
    public void testDeleteExistingStudent() {
        // Задаем заранее известный ID студента
        int studentId = 7; // Предположим, что такой студент существует в базе
        String expectedName = "Student For Delete";

        createStudent(studentId, expectedName, new int[]{4, 5});

        // Проверяем существование студента перед удалением
        given()
                .baseUri(BASE_URL)
                .pathParam("id", studentId)
        .when()
                .get(STUDENT_ENDPOINT + "/{id}")
        .then()
                .statusCode(200)
                .body("id", equalTo(studentId))
                .body("name", equalTo(expectedName));

        // Выполняем удаление
        given()
                .baseUri(BASE_URL)
                .pathParam("id", studentId)
        .when()
                .delete(STUDENT_ENDPOINT + "/{id}")
        .then()
                .statusCode(200);

        // Проверяем, что студент удален
        given()
                .baseUri(BASE_URL)
                .pathParam("id", studentId)
        .when()
                .get(STUDENT_ENDPOINT + "/{id}")
        .then()
                .statusCode(404); // Студент должен быть удален
    }


    //8.	delete /student/{id} возвращает код 404, если студента с таким ID в базе нет.
    @DisplayName("8.testDeleteNonExistingStudent Проверка 404 delete /student/{id}")
    @Test
    public void testDeleteNonExistingStudent() {
        given()
        .when()
                .delete(STUDENT_ENDPOINT + "/999")
        .then()
                .statusCode(404);
    }


    // Тесты для GET /topStudent
    //9.	get /topStudent код 200 и пустое тело, если студентов в базе нет.
    @DisplayName("9.testTopStudentEmpty Проверка 200 get /topStudent ")
    @Test
    public void testTopStudentEmpty() {
        // Очищаем базу данных (если необходимо)
        //clearDatabase();

        // Выполняем запрос
        String responseBody = given()
                .baseUri(BASE_URL)
                .when()
                .get(TOP_STUDENT_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .asString();

        System.out.println(responseBody);

        Assertions.assertTrue(responseBody.isEmpty(), "Ответ не должен содержать тело");
    }


    //10.	get /topStudent код 200 и пустое тело, если ни у кого из студентов в базе нет оценок.
    @DisplayName("10.testGetTopStudentWhenNoMarks Проверка 200 get /topStudent")
    @Test
    void testGetTopStudentWhenNoMarks() {
        //
        //clearDatabase();
        // Создаем нескольких студентов без оценок
        createStudent(10,"Student 1", new int[0]);
        createStudent(11,"Student 2", new int[0]);
        createStudent(12,"Student 3", new int[0]);

        // Выполняем запрос
        String responseBody = given()
                .baseUri(BASE_URL)
        .when()
                .get(TOP_STUDENT_ENDPOINT)
        .then()
                .statusCode(200)
                .extract()
                .asString();

        // Проверяем, что ответ пустой
        Assertions.assertTrue(
                responseBody.isEmpty(),
                "Ответ должен быть пустым при отсутствии оценок у всех студентов"
        );
    }


    //11.	get /topStudent код 200 и один студент, если у него максимальная средняя оценка,
    @DisplayName("11.testGetTopStudentWithMaxAverage Проверка 200 get /topStudent")
    @Test
    void testGetTopStudentWithMaxAverage() {
        //clearDatabase();

        // Создаем студентов с разными средними оценками
        createStudent(13,"Student 1", new int[]{5, 5, 5});    // Средний балл 5.0
        createStudent(14,"Student 2", new int[]{4, 4, 4});    // Средний балл 4.0
        createStudent(15,"Student 3", new int[]{5, 4, 5});    // Средний балл 4.67

        Response response = given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .log().all()
                .when()
                .get(TOP_STUDENT_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Получаем тело ответа
        String responseBody = response.asString();
        System.out.println("Ответ сервера: " + responseBody);

        // Создаем ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Парсим JSON в список карт
            List<Map<String, Object>> students = objectMapper.readValue(
                    responseBody,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // Проверяем, что список не пустой
            Assertions.assertFalse(students.isEmpty(), "Массив студентов пуст");

            // Получаем первого студента
            Map<String, Object> student = students.get(0);

            // Получаем значения
            int id = (int) student.get("id");
            String name = (String) student.get("name");
            List<Integer> marks = (List<Integer>) student.get("marks");

            // Добавляем проверки
            Assertions.assertEquals(13, id);
            Assertions.assertEquals("Student 1", name);
            Assertions.assertEquals(3, marks.size());
            Assertions.assertTrue(marks.containsAll(List.of(5, 5, 5)));

            // Проверка среднего балла
            double average = marks.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            Assertions.assertEquals(5.0, average);

        } catch (Exception e) {
            System.out.println("Ошибка при парсинге JSON: " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Не удалось распарсить JSON: " + e.getMessage());
        }
    }

//    @DisplayName("12.testGetTopStudentWithMaxAverage Проверка 200 get /topStudent")
//    @Test
//    void testGetTopStudentWithMaxAverage2() {
//        clearDatabase();
//
//        createStudent(13, "Student 1", new int[]{5, 5, 5});
//        createStudent(14, "Student 2", new int[]{4, 4, 4});
//        createStudent(15, "Student 3", new int[]{5, 4, 5});
//
//        String responseBody = given()
//                .baseUri(BASE_URL)
//                .log().all()
//                .when()
//                .get(TOP_STUDENT_ENDPOINT)
//                .then()
//                .statusCode(200)
//                .extract()
//                .asString();
//
//        System.out.println("Полученный ответ от сервера: " + responseBody);
//        System.out.println("Тип ответа: " + responseBody.getClass().getSimpleName());
//        System.out.println("Длина ответа: " + responseBody.length());
//
//        // Детальный анализ строки
//        System.out.println("Побайтовый анализ:");
//        for (int i = 0; i < responseBody.length(); i++) {
//            char c = responseBody.charAt(i);
//            System.out.println("Позиция " + i + ": '" + c + "' (код: " + (int) c + ")");
//        }
//
//        // Проверим специальные символы
//        System.out.println("Содержит непечатные символы: " + containsNonPrintableChars(responseBody));
//
//        JsonPath jsonPath = new JsonPath(responseBody);
//
//        // Разные способы доступа
//        System.out.println("jsonPath.get(\"$\"): " + jsonPath.get("$"));
//        System.out.println("jsonPath.getList(\"\"): " + jsonPath.getList(""));
//        System.out.println("jsonPath.getList(\"$\"): " + jsonPath.getList("$"));
//        System.out.println("jsonPath.getObject(\"$\", Object.class): " + jsonPath.getObject("$", Object.class));
//
//        // Попробуем получить как Map
//        try {
//            Map<String, Object> map = jsonPath.getMap("$[0]");
//            System.out.println("jsonPath.getMap(\"$[0]\"): " + map);
//        } catch (Exception e) {
//            System.out.println("Ошибка getMap: " + e.getMessage());
//        }
//
//        // Попробуем получить как Object
//        try {
//            Object obj = jsonPath.get("$[0]");
//            System.out.println("jsonPath.get(\"$[0]\"): " + obj);
//            System.out.println("Тип: " + (obj != null ? obj.getClass() : "null"));
//        } catch (Exception e) {
//            System.out.println("Ошибка get: " + e.getMessage());
//        }
//    }
//
//    private boolean containsNonPrintableChars(String str) {
//        for (int i = 0; i < str.length(); i++) {
//            char c = str.charAt(i);
//            if (c < 32 || c > 126) {
//                if (c != '\n' && c != '\r' && c != '\t') {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }


    //... либо же среди всех студентов с максимальной средней у него их больше всего.
    @DisplayName("11.testGetTopStudentWithMostMarks Проверка 200 get /topStudent")
    @Test
    void testGetTopStudentWithMostMarks() {
        //clearDatabase();

        // Создаем студентов с разными средними и количеством оценок
        createStudent(13, "Student 1", new int[]{5, 5, 5}); // avg = 5.0, marks = 3
        createStudent(14, "Student 2", new int[]{5, 5, 5, 5}); // avg = 5.0, marks = 4
        createStudent(15, "Student 3", new int[]{5, 4, 5}); // avg = 4.67, marks = 3
        createStudent(16, "Student 4", new int[]{5, 5}); // avg = 5.0, marks = 2

        Response response = given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .log().all()
                .when()
                .get(TOP_STUDENT_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Получаем тело ответа
        String responseBody = response.asString();
        System.out.println("Ответ сервера: " + responseBody);

        // Создаем ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Парсим JSON в список карт
            List<Map<String, Object>> students = objectMapper.readValue(
                    responseBody,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // Проверяем, что список не пустой
            Assertions.assertFalse(students.isEmpty(), "Массив студентов пуст");

            // Получаем первого студента
            Map<String, Object> topStudent = students.get(0);

            // Получаем значения для топ-студента
            int topStudentId = (int) topStudent.get("id");
            String topStudentName = (String) topStudent.get("name");
            List<Integer> topStudentMarks = (List<Integer>) topStudent.get("marks");
            double topStudentAverage = calculateAverage(topStudentMarks);

            // Собираем всех студентов для сравнения
            List<Map<String, Object>> allStudents = objectMapper.readValue(
                    responseBody,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // Находим максимальную среднюю оценку
            double maxAverage = allStudents.stream()
                    .map(student -> calculateAverage((List<Integer>) student.get("marks")))
                    .max(Double::compareTo)
                    .orElse(0.0);

            // Фильтруем студентов с максимальной средней оценкой
            List<Map<String, Object>> maxAverageStudents = allStudents.stream()
                    .filter(student ->
                            calculateAverage((List<Integer>) student.get("marks")) == maxAverage
                    )
                    .toList();

            // Находим максимальное количество оценок среди студентов с maxAverage
            int maxMarksCount = maxAverageStudents.stream()
                    .mapToInt(student -> ((List<Integer>) student.get("marks")).size())
                    .max()
                    .orElse(0);

            // Проверяем, что наш топ-студент имеет максимальное количество оценок
            Assertions.assertEquals(
                    maxMarksCount,
                    topStudentMarks.size(),
                    "Топ-студент не имеет максимального количества оценок среди студентов с максимальной средней"
            );

            // Дополнительные проверки
            Assertions.assertEquals(14, topStudentId);
            Assertions.assertEquals("Student 2", topStudentName);
            Assertions.assertEquals(4, topStudentMarks.size());
            Assertions.assertEquals(5.0, topStudentAverage);

        } catch (Exception e) {
            System.out.println("Ошибка при парсинге JSON: " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Не удалось распарсить JSON: " + e.getMessage());
        }
    }

    private double calculateAverage(List<Integer> marks) {
        if (marks == null || marks.isEmpty()) {
            return 0.0;
        }

        // Вычисляем среднее значение
        return marks.stream()
                .mapToInt(Integer::intValue) // преобразуем в IntStream
                .average()                   // получаем среднее значение
                .orElse(0.0);               // если поток пустой, возвращаем 0.0
    }

    //12.	get /topStudent код 200 и несколько студентов, если у них всех эта оценка максимальная и при этом они равны по количеству оценок.
    @DisplayName("12.testGetTopStudentsWithEqualMaxAverageAndMarksCount Проверка 200 get /topStudent")
    @Test
    void testGetTopStudentsWithEqualMaxAverageAndMarksCount() {
        //clearDatabase();

        // Создаем студентов с одинаковыми средними и количеством оценок
        createStudent(13, "Student 1", new int[]{5, 5, 5}); // avg = 5.0, marks = 3
        createStudent(14, "Student 2", new int[]{5, 5, 5}); // avg = 5.0, marks = 3
        createStudent(15, "Student 3", new int[]{5, 4, 5}); // avg = 4.67, marks = 3
        createStudent(16, "Student 4", new int[]{5, 5, 5}); // avg = 5.0, marks = 3

        // Выполняем GET-запрос
        Response response = given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .log().all()
                .when()
                .get(TOP_STUDENT_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Получаем тело ответа
        String responseBody = response.asString();
        System.out.println("Ответ сервера: " + responseBody);

        // Создаем ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Парсим JSON в список карт
            List<Map<String, Object>> students = objectMapper.readValue(
                    responseBody,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // Проверяем, что список не пустой
            Assertions.assertFalse(students.isEmpty(), "Массив студентов пуст");

            // Проверяем количество топ-студентов
            Assertions.assertEquals(3, students.size(), "Неверное количество топ-студентов");

            // Проверяем каждого студента
            for (Map<String, Object> student : students) {
                int id = (int) student.get("id");
                String name = (String) student.get("name");
                List<Integer> marks = (List<Integer>) student.get("marks");
                double average = calculateAverage(marks);

                // Проверки для каждого студента
                Assertions.assertEquals(5.0, average, "Неверная средняя оценка");
                Assertions.assertEquals(3, marks.size(), "Неверное количество оценок");
            }

            // Проверяем конкретные ID студентов
            List<Integer> expectedIds = List.of(13, 14, 16);
            List<Integer> actualIds = students.stream()
                    .map(student -> (int) student.get("id"))
                    .sorted()
                    .toList();

            Assertions.assertEquals(expectedIds, actualIds, "Неверные ID топ-студентов");

        } catch (Exception e) {
            System.out.println("Ошибка при парсинге JSON: " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Не удалось распарсить JSON: " + e.getMessage());
        }
    }

}
