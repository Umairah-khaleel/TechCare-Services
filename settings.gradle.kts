pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(
        org.gradle.api.initialization.resolve.RepositoriesMode.PREFER_SETTINGS
    )
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "My_Application"
include(":app")
