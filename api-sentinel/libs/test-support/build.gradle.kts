plugins {
    `java-library`
}

dependencies {
    api(project(":libs:common-model"))
    api(libs.junit.jupiter)
    api(libs.assertj.core)
    api(libs.mockito.core)
}