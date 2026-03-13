package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.jvm.tasks.Jar
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

        if (project == project.rootProject) {
            project.tasks.register("initApiClientSubproject", InitSubprojectTask::class.java) { task ->
                task.group = "api client generation"
                task.description =
                    "Generate a new Gradle subproject pre-configured with the OpenAPI Ktor client generator. " +
                    "Usage: ./gradlew initApiClientSubproject -PopenApiFile=<path> [-PsubprojectName=<name>] [-PbasePackage=<pkg>]"
                task.openApiFilePath.set(project.findProperty("openApiFile") as String?)
                task.subprojectName.set(project.findProperty("subprojectName") as String?)
                task.basePackage.set(project.findProperty("basePackage") as String?)
                task.rootDirectory.set(project.rootDir)
                task.kotlinVersion.convention(
                    extension.initSubproject.kotlinVersion.orElse(DEFAULT_KOTLIN_VERSION),
                )
                task.ktorVersion.convention(
                    extension.initSubproject.ktorVersion.orElse(DEFAULT_KTOR_VERSION),
                )
                task.coroutinesVersion.convention(
                    extension.initSubproject.coroutinesVersion.orElse(DEFAULT_COROUTINES_VERSION),
                )
                task.serializationVersion.convention(
                    extension.initSubproject.serializationVersion.orElse(DEFAULT_SERIALIZATION_VERSION),
                )
                task.buildScriptTemplate.convention(extension.initSubproject.buildScriptTemplate)
                task.generatorConfigExtra.convention(extension.initSubproject.generatorConfigExtra)
                task.splitByClient.set(project.findProperty("splitByClient")?.toString()?.toBoolean() ?: false)
                task.splitGranularity.set(project.findProperty("splitGranularity") as String?)
                task.sharedModelGranularity.set(project.findProperty("sharedModelGranularity") as String?)
                task.subprojectRootDirectory.set(
                    (project.findProperty("subprojectRootDirectory") as String?)
                        ?: extension.initSubproject.subprojectRootDirectory.orNull,
                )
                task.multiplatform.set(
                    (project.findProperty("multiplatformTargets") as String?)?.toBoolean()
                        ?: extension.initSubproject.multiplatform.orNull,
                )
            }
        }

        project.afterEvaluate {
            val skip = extension.skip.getOrNull()
            extension.generators.names.all { generatorName ->
                val generatorExtension = extension.generators.getByName(generatorName)
                generatorExtension.initConventions(project)
                val task =
                    project.tasks.register(
                        "generate${generatorExtension.name.capitalize()}",
                        GenerateTask::class.java,
                    ) { task ->
                        task.group = "api client generation"
                        task.openApiFile.set(generatorExtension.openApiFile)
                        task.outputDirectory.set(generatorExtension.outputDirectory)
                        task.basePackage.set(generatorExtension.basePackage)
                        task.allowedPaths.set(generatorExtension.allowedPaths)
                        task.modulesIds.set(generatorExtension.modulesIds)
                        task.splitByClient.set(generatorExtension.splitByClient)
                        task.targetClientName.set(generatorExtension.targetClientName)
                        task.sharedBasePackage.set(generatorExtension.sharedBasePackage)
                        task.splitGranularity.set(generatorExtension.splitGranularity)
                        task.sharedModelGranularity.set(generatorExtension.sharedModelGranularity)
                        task.targetSharedGroup.set(generatorExtension.targetSharedGroup)
                        task.additionalSharedGroupPackages.set(generatorExtension.additionalSharedGroupPackages)
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
                project.tasks.withType(Jar::class.java).configureEach {
                    it.dependsOn(task.get())
                }

                project.tasks
                    .named {
                        it.startsWith("lintKotlin")
                    }.configureEach { t: Task ->
                        t.dependsOn(task.get())
                    }

                true
            }
        }
    }
}
