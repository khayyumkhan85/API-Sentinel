package com.sri.apisentinel.testsupport;

public final class Fixtures {

    public static final String CUSTOMER_V1 = """
            openapi: 3.0.0
            info:
              title: Customer API
              version: 1.0.0
            paths:
              /customers/{id}:
                get:
                  parameters:
                    - name: id
                      in: path
                      required: true
                      schema:
                        type: string
                  responses:
                    '200':
                      description: ok
                      content:
                        application/json:
                          schema:
                            $ref: '#/components/schemas/Customer'
            components:
              schemas:
                Customer:
                  type: object
                  required:
                    - id
                    - customerName
                  properties:
                    id:
                      type: string
                    customerName:
                      type: string
            """;

    public static final String CUSTOMER_V2_BREAKING = """
            openapi: 3.0.0
            info:
              title: Customer API
              version: 2.0.0
            paths:
              /customers/{id}:
                get:
                  parameters:
                    - name: id
                      in: path
                      required: true
                      schema:
                        type: string
                  responses:
                    '200':
                      description: ok
                      content:
                        application/json:
                          schema:
                            $ref: '#/components/schemas/Customer'
            components:
              schemas:
                Customer:
                  type: object
                  required:
                    - id
                  properties:
                    id:
                      type: string
                    customer:
                      type: object
            """;

    public static final String CUSTOMER_V1_NON_BREAKING = """
            openapi: 3.0.0
            info:
              title: Customer API
              version: 1.1.0
            paths:
              /customers/{id}:
                get:
                  parameters:
                    - name: id
                      in: path
                      required: true
                      schema:
                        type: string
                  responses:
                    '200':
                      description: ok
                      content:
                        application/json:
                          schema:
                            $ref: '#/components/schemas/Customer'
            components:
              schemas:
                Customer:
                  type: object
                  required:
                    - id
                    - customerName
                  properties:
                    id:
                      type: string
                    customerName:
                      type: string
                    email:
                      type: string
            """;

    private Fixtures() {}
}