plugins {
    `java-library`
}

dependencies {
    api(project(":libs:common-model"))
    api(libs.swagger.parser)
    api(libs.jackson.dataformat.yaml)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.bundles.test.core)
    testImplementation(project(":libs:test-support"))
}