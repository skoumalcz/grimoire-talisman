package com.skoumal.grimoire.build

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate

class LibraryFactory(
    private val project: Project
) {

    private val plugins get() = project.plugins

    fun applyLibrary() = apply {
        applyPluginSafely(PLUGIN_LIBRARY)

        project.dependencies {
            val jUnitAndroidXVersion: String by project.parent!!.extra

            testImplementation(LIBRARY_TEST_JUNIT_ANDROID, jUnitAndroidXVersion)
        }
    }

    fun applyKotlin() = apply {
        applyPluginSafely(PLUGIN_KOTLIN)
        applyPluginSafely(PLUGIN_KOTLIN_KAPT)

        project.dependencies {
            val kotlinVersion: String by project.parent!!.extra
            val coroutinesVersion: String by project.parent!!.extra

            implementation(LIBRARY_KOTLIN, kotlinVersion)
            implementation(LIBRARY_KOTLIN_REFLECT, kotlinVersion)
            implementation(LIBRARY_COROUTINES, coroutinesVersion)

            val jUnitVersion: String by project.parent!!.extra
            val truthVersion: String by project.parent!!.extra
            val mockitoVersion: String by project.parent!!.extra

            testImplementation(LIBRARY_TEST_JUNIT, jUnitVersion)
            testImplementation(LIBRARY_TEST_COROUTINES, coroutinesVersion)
            testImplementation(LIBRARY_TEST_TRUTH, truthVersion)
            testImplementation(LIBRARY_TEST_MOCKITO, mockitoVersion)
            testImplementation(LIBRARY_TEST_MOCKITO_INLINE, mockitoVersion)
        }
    }

    fun applyExtensions() = apply {
        applyPluginSafely(PLUGIN_KOTLIN_EXT)
    }

    fun applyPublishing() = apply {
        applyPluginSafely(PLUGIN_MAVEN)
        applyPluginSafely(PLUGIN_BINTRAY)
    }

    // ---

    fun build(): LibraryConfigurationOptions {
        return LibraryConfigurationOptions(
            project,
            project.extensions.getByName(EXTENSION_ANDROID) as LibraryExtension
        )
    }

    // ---

    private fun applyPluginSafely(name: String) {
        if (plugins.findPlugin(name) != null) {
            println("Plugin [name=${name}] has already been applied to project [name=${project.name}]")
            return
        }
        plugins.apply(name)
    }

    private fun DependencyHandlerScope.implementation(notation: String, version: String) =
        add("implementation", "%s:%s".format(notation, version))

    private fun DependencyHandlerScope.testImplementation(notation: String, version: String) =
        add("testImplementation", "%s:%s".format(notation, version))

    companion object {

        private const val PLUGIN_LIBRARY = "com.android.library"
        private const val PLUGIN_KOTLIN = "kotlin-android"
        private const val PLUGIN_KOTLIN_KAPT = "kotlin-kapt"
        private const val PLUGIN_KOTLIN_EXT = "kotlin-android-extensions"
        private const val PLUGIN_MAVEN = "maven-publish"
        private const val PLUGIN_BINTRAY = "com.jfrog.bintray"

        private const val EXTENSION_ANDROID = "android"

        private const val LIBRARY_KOTLIN = "org.jetbrains.kotlin:kotlin-stdlib"
        private const val LIBRARY_KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"
        private const val LIBRARY_COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-android"

        private const val LIBRARY_TEST_JUNIT = "junit:junit"
        private const val LIBRARY_TEST_JUNIT_ANDROID = "androidx.test.ext:junit"
        private const val LIBRARY_TEST_COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-test"
        private const val LIBRARY_TEST_TRUTH = "com.google.truth:truth"
        private const val LIBRARY_TEST_MOCKITO = "org.mockito:mockito-core"
        private const val LIBRARY_TEST_MOCKITO_INLINE = "org.mockito:mockito-inline"

    }

}