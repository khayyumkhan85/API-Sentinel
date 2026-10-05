rootProject.name = "api-sentinel"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

include(
    ":libs:common-model",
    ":libs:openapi-diff",
    ":libs:risk-engine",
    ":libs:test-support",
    ":apps:api-gateway",
    ":apps:contract-service",
    ":apps:dependency-service",
    ":apps:analysis-service",
    ":apps:mcp-server"
)