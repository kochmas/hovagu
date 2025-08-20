pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.4.1"
        kotlin("android") version "1.9.20"
        kotlin("plugin.serialization") version "1.9.20"
    }
}

rootProject.name = "ssplite-android"
include(":app")
