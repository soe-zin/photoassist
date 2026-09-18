// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}

group = "com.example.photographyassistant"
version = "1.0.0"

subprojects {
    afterEvaluate {
        // Ensure all subprojects use the same Kotlin version
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
}