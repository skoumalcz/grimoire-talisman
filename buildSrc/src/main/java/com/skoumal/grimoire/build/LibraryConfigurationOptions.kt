package com.skoumal.grimoire.build

import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CompileOptions
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class LibraryConfigurationOptions(
    private val project: Project,
    private val plugin: LibraryExtension
) {

    init {
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    fun setTargetSdk(sdkInt: Int) = apply {
        plugin.compileSdk = sdkInt
        plugin.defaultConfig.targetSdk = sdkInt
    }

    fun setMinSdk(sdkInt: Int) = apply {
        plugin.defaultConfig.minSdk = sdkInt
    }

    fun addBuildType(type: String, body: BuildType.() -> Unit) = apply {
        plugin.buildTypes.maybeCreate(type).body()
    }

    fun compileOptions(body: CompileOptions.() -> Unit) = apply {
        plugin.compileOptions(body)
    }

    fun kotlinOptions(body: KotlinJvmOptions.() -> Unit) = apply {
        project.tasks.withType(KotlinCompile::class.java).all {
            kotlinOptions(body)
        }
    }

}