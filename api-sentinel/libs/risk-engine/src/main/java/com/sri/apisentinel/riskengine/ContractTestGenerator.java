package com.sri.apisentinel.riskengine;

import java.util.ArrayList;
import java.util.List;

public class ContractTestGenerator {

    public List<String> generate(String endpointPath, String method, List<String> requiredFields) {
        List<String> tests = new ArrayList<>();
        tests.add(generateStatusTest(endpointPath, method));
        for (String field : requiredFields) {
            tests.add(generateFieldTest(endpointPath, method, field));
        }
        return tests;
    }

    private String generateStatusTest(String path, String method) {
        return """
                @Test
                void %sReturnsOk() {
                    given()
                        .when()
                        .%s("%s")
                        .then()
                        .statusCode(200);
                }
                """.formatted(sanitize(path) + method, method.toLowerCase(), path);
    }

    private String generateFieldTest(String path, String method, String field) {
        return """
                @Test
                void %sContains_%s() {
                    given()
                        .when()
                        .%s("%s")
                        .then()
                        .statusCode(200)
                        .body("%s", notNullValue());
                }
                """.formatted(sanitize(path) + method, field, method.toLowerCase(), path, field);
    }

    private String sanitize(String path) {
        return path.replaceAll("[^A-Za-z0-9]", "_");
    }
}