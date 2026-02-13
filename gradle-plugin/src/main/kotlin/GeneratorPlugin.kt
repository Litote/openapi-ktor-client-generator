package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.litote.openapi.ktor.client.generator.shared.capitalize

public class GeneratorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("apiClientGenerator", ApiClientGeneratorsExtension::class.java)

        project.afterEvaluate {
            val skip = extension.skip.getOrNull()
            extension.generators.names.all { generatorName ->
                val generatorExtension = extension.generators.getByName(generatorName)
                generatorExtension.initConventions(project)
                val task =
                    project.tasks.register("generate${generatorExtension.name.capitalize()}", GenerateTask::class.java) { task ->
                        task.group = "api client generation"
                        task.openApiFile.set(generatorExtension.openApiFile)
                        task.outputDirectory.set(generatorExtension.outputDirectory)
                        task.basePackage.set(generatorExtension.basePackage)
                        task.allowedPaths.set(generatorExtension.allowedPaths)
                        task.modulesIds.set(generatorExtension.modulesIds)
                        val generatorSkip: Boolean? = generatorExtension.skip.getOrNull()
                        if (skip == true && generatorSkip != false) {
                            task.skip.set(true)
                        } else {
                            task.skip.set(generatorSkip == true)
                        }
                    }

                project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
                    val kotlinExtension = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
                    if (kotlinExtension != null) {
                        project.afterEvaluate {
                            kotlinExtension.sourceSets.getByName("commonMain").kotlin.srcDir(
                                generatorExtension.outputDirectory.dir(
                                    "src/main/kotlin",
                                ),
                            )
                        }
                        project.tasks.withType(KotlinCompileCommon::class.java).configureEach {
                            it.dependsOn(task.get())
                        }
                        project.tasks.withType(KotlinNativeCompile::class.java).configureEach {
                            it.dependsOn(task.get())
                        }
                        project.tasks.withType(KotlinJvmCompile::class.java).configureEach {
                            it.dependsOn(task.get())
                        }
                    }
                }
                project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                    project.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets.named("main") {
                        it.kotlin.srcDir(generatorExtension.outputDirectory.dir("src/main/kotlin"))
                    }
                }
                project.tasks.withType(KotlinCompile::class.java).configureEach {
                    it.dependsOn(task.get())
                }
                true
            }
        }
    }
}
