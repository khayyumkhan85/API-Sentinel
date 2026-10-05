plugins {
    `java-library`
}

dependencies {
    api(project(":libs:common-model"))
    api(project(":libs:openapi-diff"))

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.bundles.test.core)
}