package com.sri.apisentinel.openapidiff;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.util.List;

public class OpenApiParser {

    private final OpenAPIV3Parser parser = new OpenAPIV3Parser();

    public OpenAPI parse(String specification) {
        if (specification == null || specification.isBlank()) {
            throw new IllegalArgumentException("Specification is empty");
        }
        SwaggerParseResult result = parser.readContents(specification);
        if (result.getOpenAPI() == null) {
            List<String> messages = result.getMessages();
            throw new IllegalArgumentException("Invalid OpenAPI specification: " +
                    (messages == null ? "unknown" : String.join("; ", messages)));
        }
        return result.getOpenAPI();
    }
}