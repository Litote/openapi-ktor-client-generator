package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asTypeName
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.serialization.json.Json
import org.litote.openapi.ktor.client.generator.ApiGeneratorConfiguration
import org.litote.openapi.ktor.client.generator.adapter.writer.KotlinPoetFileWriter
import org.litote.openapi.ktor.client.generator.domain.ClientConfigurationSpec
import org.litote.openapi.ktor.client.generator.domain.SecuritySchemeLocationSpec
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationGeneratorConfig
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationRenderer
import org.litote.openapi.ktor.client.generator.port.ApiFileSystemWriter

public class ApiClientConfigurationGenerator public constructor(
    private val clientConfiguration: ClientConfigurationSpec,
    private val configuration: ApiGeneratorConfiguration,
    private val fileSystemWriter: ApiFileSystemWriter = KotlinPoetFileWriter(),
) : ApiConfigurationRenderer,
    ApiConfigurationGeneratorConfig {
    private companion object {
        val engineFactoryType: ParameterizedTypeName =
            HttpClientEngineFactory::class.asTypeName().parameterizedBy(STAR)
        val httpClientConfigType: LambdaTypeName =
            LambdaTypeName.get(
                receiver = HttpClientConfig::class.asTypeName().parameterizedBy(STAR),
                returnType = UNIT,
            )
        val loggingClass: ClassName = ClassName("io.ktor.client.plugins.logging", "Logging")
        val contentNegotiationClass: ClassName =
            ClassName("io.ktor.client.plugins.contentnegotiation", "ContentNegotiation")
        val jsonMember: MemberName = MemberName("io.ktor.serialization.kotlinx.json", "json")
        val contentTypeClass: ClassName = ClassName("io.ktor.http", "ContentType")
        val cioMember: MemberName = MemberName("io.ktor.client.engine.cio", "CIO")
        val exceptionLoggerType: LambdaTypeName =
            LambdaTypeName.get(
                receiver = Throwable::class.asTypeName(),
                returnType = UNIT,
            )
    }

    private val hasApiKeys: Boolean = clientConfiguration.apiKeySchemes.isNotEmpty()
    private val headerApiKeys =
        clientConfiguration.apiKeySchemes.filter {
            it.location == SecuritySchemeLocationSpec.HEADER
        }
    private val queryApiKeys =
        clientConfiguration.apiKeySchemes.filter {
            it.location == SecuritySchemeLocationSpec.QUERY
        }

    private fun buildDefaultConfigLambda(): CodeBlock {
        val builder =
            CodeBlock
                .builder()
                .beginControlFlow("{")
                .addStatement("install(%T)", loggingClass)
                .beginControlFlow("install(%T)", contentNegotiationClass)
                .addStatement("%M(%N)", jsonMember, "json")

        if (clientConfiguration.hasYamlContentType) {
            val yamlConverterClass = ClassName(configuration.clientPackage, "YamlContentConverter")
            builder
                .addStatement(
                    "register(%T(%S, %S), %T(%N))",
                    contentTypeClass,
                    "application",
                    "yaml",
                    yamlConverterClass,
                    "json",
                ).addStatement(
                    "register(%T(%S, %S), %T(%N))",
                    contentTypeClass,
                    "application",
                    "x-yaml",
                    yamlConverterClass,
                    "json",
                )
        }

        builder
            .endControlFlow()
            .beginControlFlow("%M", MemberName("io.ktor.client.plugins", "defaultRequest"))
            .addStatement("url(%N)", "baseUrl")

        headerApiKeys.forEach { scheme ->
            builder.addStatement(
                "%N?.let { %L(%S, it) }",
                scheme.paramName,
                ALIAS_HEADER,
                scheme.keyName,
            )
        }

        queryApiKeys.forEach { scheme ->
            builder.addStatement(
                "%N?.let { url.parameters.append(%S, it) }",
                scheme.paramName,
                scheme.keyName,
            )
        }

        builder
            .endControlFlow()
            .addStatement("%N()", "httpClientAuthorization")
            .endControlFlow()

        return builder.build()
    }

    private fun buildDefaultHttpClientConfig(): FunSpec {
        val funBuilder =
            FunSpec
                .builder("defaultHttpClientConfig")
                .addParameter("baseUrl", String::class)
                .addParameter("json", Json::class)

        clientConfiguration.apiKeySchemes.forEach { scheme ->
            funBuilder.addParameter(
                ParameterSpec
                    .builder(scheme.paramName, String::class.asTypeName().copy(nullable = true))
                    .build(),
            )
        }

        funBuilder.addParameter(
            ParameterSpec
                .builder("httpClientAuthorization", httpClientConfigType)
                .build(),
        )

        return funBuilder
            .returns(httpClientConfigType)
            .addStatement("return %L", buildDefaultConfigLambda())
            .build()
    }

    // mutable properties for modules
    override val jsonDefaultValueProperties: MutableMap<String, String> = mutableMapOf("ignoreUnknownKeys" to "true")
    override var exceptionLoggingDefaultValue: String = "{ printStackTrace() }"
    override var httpClientAuthorizationDefaultValue: String = "{}"
    override val additionalStringParameters: MutableList<String> = mutableListOf()
    // end mutable properties for modules

    private val jsonDefaultValue: CodeBlock
        get() =
            CodeBlock
                .builder()
                .add(
                    "%T { ${
                        jsonDefaultValueProperties.entries.joinToString(
                            separator = "\n",
                            prefix = "\n",
                            postfix = "\n",
                        ) {
                            "${it.key} = ${it.value}"
                        }
                    } }",
                    Json::class,
                ).build()

    internal fun buildConstructor(): FunSpec {
        val builder =
            FunSpec
                .constructorBuilder()
                .addParameter(
                    ParameterSpec
                        .builder("baseUrl", String::class)
                        .defaultValue("%S", clientConfiguration.serverUrl)
                        .build(),
                )

        clientConfiguration.apiKeySchemes.forEach { scheme ->
            builder.addParameter(
                ParameterSpec
                    .builder(scheme.paramName, String::class.asTypeName().copy(nullable = true))
                    .defaultValue("null")
                    .build(),
            )
        }

        additionalStringParameters.forEach { paramName ->
            builder.addParameter(
                ParameterSpec
                    .builder(paramName, String::class.asTypeName().copy(nullable = true))
                    .defaultValue("null")
                    .build(),
            )
        }

        builder
            .addParameter(
                ParameterSpec
                    .builder("engine", engineFactoryType)
                    .defaultValue("%M", cioMember)
                    .build(),
            ).addParameter(
                ParameterSpec
                    .builder("json", Json::class)
                    .defaultValue(jsonDefaultValue)
                    .build(),
            ).addParameter(
                ParameterSpec
                    .builder("httpClientAuthorization", httpClientConfigType)
                    .defaultValue("%L", httpClientAuthorizationDefaultValue)
                    .build(),
            )

        val extraParams =
            buildString {
                if (hasApiKeys) {
                    append(clientConfiguration.apiKeySchemes.joinToString(", ") { it.paramName })
                    append(", ")
                }
                append("%N")
            }
        val httpClientConfigDefaultValue =
            CodeBlock.of("%N(%N, %N, $extraParams)", "defaultHttpClientConfig", "baseUrl", "json", "httpClientAuthorization")

        builder
            .addParameter(
                ParameterSpec
                    .builder("httpClientConfig", httpClientConfigType)
                    .defaultValue(httpClientConfigDefaultValue)
                    .build(),
            ).addParameter(
                ParameterSpec
                    .builder("client", HttpClient::class)
                    .defaultValue("%T(%N) { %N() }", HttpClient::class, "engine", "httpClientConfig")
                    .build(),
            ).addParameter(
                ParameterSpec
                    .builder("exceptionLogger", exceptionLoggerType)
                    .defaultValue(exceptionLoggingDefaultValue)
                    .build(),
            )

        return builder.build()
    }

    internal fun buildCompanion(): TypeSpec {
        val companionBuilder = TypeSpec.companionObjectBuilder()

        companionBuilder.addProperty(
            PropertySpec
                .builder("defaultClientConfiguration", ClassName("", "ClientConfiguration"))
                .delegate("lazy { %L() }", "ClientConfiguration")
                .build(),
        )

        clientConfiguration.componentParameters.forEach { spec ->
            val specDefaultValue = spec.defaultValue
            if (specDefaultValue != null) {
                val typeName = spec.type.toTypeName(configuration.resolvedModelPackage, configuration.modelPackageOverrides)
                // Only add const if the type supports it (primitives)
                if (isConstSupported(typeName)) {
                    companionBuilder.addProperty(
                        PropertySpec
                            .builder("${spec.constName}_DEFAULT_VALUE", typeName)
                            .addModifiers(KModifier.CONST)
                            .initializer(specDefaultValue.toCodeBlock())
                            .build(),
                    )
                }
            }
        }

        companionBuilder.addFunction(buildDefaultHttpClientConfig())
        return companionBuilder.build()
    }

    internal fun buildClientConfiguration(
        constructor: FunSpec,
        companion: TypeSpec,
    ): TypeSpec {
        val builder =
            TypeSpec
                .classBuilder("ClientConfiguration")
                .primaryConstructor(constructor)
                .addProperty(
                    PropertySpec
                        .builder("baseUrl", String::class)
                        .initializer("baseUrl")
                        .build(),
                )

        clientConfiguration.apiKeySchemes.forEach { scheme ->
            builder.addProperty(
                PropertySpec
                    .builder(scheme.paramName, String::class.asTypeName().copy(nullable = true))
                    .initializer(scheme.paramName)
                    .build(),
            )
        }

        additionalStringParameters.forEach { paramName ->
            builder.addProperty(
                PropertySpec
                    .builder(paramName, String::class.asTypeName().copy(nullable = true))
                    .initializer(paramName)
                    .build(),
            )
        }

        builder
            .addProperty(
                PropertySpec
                    .builder("engine", engineFactoryType)
                    .initializer("engine")
                    .build(),
            ).addProperty(
                PropertySpec
                    .builder("json", Json::class)
                    .initializer("json")
                    .build(),
            ).addProperty(
                PropertySpec
                    .builder("httpClientAuthorization", httpClientConfigType)
                    .initializer("httpClientAuthorization")
                    .build(),
            ).addProperty(
                PropertySpec
                    .builder("httpClientConfig", httpClientConfigType)
                    .initializer("httpClientConfig")
                    .build(),
            ).addProperty(
                PropertySpec
                    .builder("client", HttpClient::class)
                    .initializer("client")
                    .build(),
            ).addProperty(
                PropertySpec
                    .builder("exceptionLogger", exceptionLoggerType)
                    .initializer("exceptionLogger")
                    .build(),
            ).addType(companion)

        return builder.build()
    }

    internal fun writeFile(clientConfiguration: TypeSpec) {
        val fileSpec =
            FileSpec
                .builder(configuration.clientPackage, "ClientConfiguration")
                .apply {
                    if (headerApiKeys.isNotEmpty()) {
                        addAliasedImport(headerMember, ALIAS_HEADER)
                    }
                }.addType(clientConfiguration)
                .build()
        fileSystemWriter.write(fileSpec.toGeneratedFile(), configuration.outputDirectory)
    }

    override fun render() {
        val constructor = buildConstructor()
        val companion = buildCompanion()
        val clientConfiguration = buildClientConfiguration(constructor, companion)
        writeFile(clientConfiguration)
        if (this.clientConfiguration.hasYamlContentType) {
            YamlContentConverterGenerator(
                clientPackage = configuration.clientPackage,
                outputDirectory = configuration.outputDirectory,
                fileSystemWriter = fileSystemWriter,
            ).render()
        }
    }
}
