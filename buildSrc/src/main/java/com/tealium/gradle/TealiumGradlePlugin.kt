package com.tealium.gradle

import com.tealium.gradle.tests.TestType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.register

/**
 * Registers CI helper tasks used by GitHub Actions workflows:
 *  - `compressReports` — zips test reports for upload.
 *  - `updatedModules` / `updatedUnitTestModules` / `updatedInstrumentedTestModules`
 *    — emit comma-separated lists of library modules changed against a base ref.
 *  - `assembleModified<Variant>` / `testModified<Variant>UnitTest` /
 *    `testModified<Variant>InstrumentedTest` — aggregate build/test tasks scoped
 *    to modules listed in `-PMODIFIED_PROJECTS`.
 */
class TealiumGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureCompressReportsTask()
        project.configureUpdatedModulesTask()

        project.gradle.projectsEvaluated {
            val modifiedProjects = project.getPropertyOrEnvironmentVariable("MODIFIED_PROJECTS", "")
                .split(",")

            project.configureCiTasks(modifiedProjects)
        }
    }

    private fun Project.configureCompressReportsTask() {
        tasks.register<Zip>("compressReports") {
            group = "build"
            destinationDirectory.set(project.layout.projectDirectory.dir("reports"))
            from(project.rootDir) {
                include("*/build/reports/**")
                includeEmptyDirs = false
            }
            archiveFileName.set("reports.zip")
        }
        tasks.register<Zip>("compressOutputs") {
            group = "build"
            destinationDirectory.set(project.layout.projectDirectory.dir("reports"))
            from(project.rootDir) {
                include("*/build/outputs/**")
                includeEmptyDirs = false
            }
            archiveFileName.set("outputs.zip")
        }
    }

    private fun Project.configureUpdatedModulesTask() {
        val incoming = project.getPropertyOrEnvironmentVariable("GITHUB_HEAD_REF", "")
        val base = project.getPropertyOrEnvironmentVariable("GITHUB_BASE_REF", "")

        tasks.register("updatedModules", UpdatedModules::class.java) {
            incomingBranch.set(incoming)
            baseBranch.set(base)
        }
        tasks.register("updatedUnitTestModules", UpdatedModules::class.java) {
            testType.set(TestType.UnitTest)
            incomingBranch.set(incoming)
            baseBranch.set(base)
        }
        tasks.register("updatedInstrumentedTestModules", UpdatedModules::class.java) {
            testType.set(TestType.InstrumentedTest)
            incomingBranch.set(incoming)
            baseBranch.set(base)
        }
    }

    private fun Project.configureCiTasks(modifiedProjectNames: List<String>) {
        val libraryProjects = rootProject.subprojects
            .filter { it.plugins.hasPlugin("com.android.library") }

        // Android library modules emit Debug/Release variants by default. Avoid
        // depending on AGP's LibraryExtension here to keep buildSrc classpath light.
        listOf("Debug", "Release").forEach { variant ->
            val modifiedProjects =
                libraryProjects.filter { project -> modifiedProjectNames.contains(project.name) }
            configureAssembleModifiedTasks(variant, modifiedProjects)
            configureAggregateTestTasks(variant, modifiedProjects)
        }
    }

    private fun Project.configureAssembleModifiedTasks(
        variant: String,
        modifiedProjects: List<Project>
    ) {
        tasks.register("assembleModified$variant") {
            group = "Build"
            description = "Build modified projects"
            dependsOn.addAll(modifiedProjects.taskNames("assemble$variant"))
            dependsOn.addAll(modifiedProjects.taskNames("assemble${variant}AndroidTest"))
        }
    }

    private fun Project.configureAggregateTestTasks(
        variant: String,
        modifiedProjects: List<Project>
    ) {
        listOf(TestType.UnitTest, TestType.InstrumentedTest).forEach { testType ->
            tasks.register(testType.modifiedTaskName(variant)) {
                group = "Verification"
                description =
                    "Runs tests for all modules that are modified according to version control"
                dependsOn.addAll(modifiedProjects.taskNames(testType.taskName(variant)))
            }
        }
    }

    private fun List<Project>.taskNames(task: String) =
        map { project -> project.taskName(task) }

    private fun Project.taskName(task: String): String =
        "${name}:$task"
}
