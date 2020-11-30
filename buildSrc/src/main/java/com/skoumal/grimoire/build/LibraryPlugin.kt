package com.skoumal.grimoire.build

import org.gradle.api.Plugin
import org.gradle.api.Project

class LibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val options = LibraryFactory(target)
            .applyLibrary()
            .applyKotlin()
            .applyPublishing()
            .build()

        options
            .setTargetSdk(30)
            .setMinSdk(21)

        LibraryPublishing(target)
            .applyPublication()
            .applyBintrayOnPublication()
    }

}