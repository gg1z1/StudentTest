package com.stepup.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

public class StudentMockTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().port(5352));
        wireMockServer.start();
        WireMock.configureFor("localhost", 5352);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testAddValidGrade() {
        // Настраиваем заглушку
        stubFor(get(urlPathEqualTo("/checkGrade"))
                .withQueryParam("grade", equalTo("5"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("true")));

        Student student = new Student("Тест");
        try {
            student.addGrade(5);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Integer> grades = student.getGrades();
        assertEquals(1, grades.size());
        assertEquals(5, grades.get(0));
    }
}
