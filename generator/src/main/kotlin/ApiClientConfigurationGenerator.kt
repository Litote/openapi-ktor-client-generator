package org.litote.openapi.ktor.client.generator

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
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.serialization.json.Json
import java.io.File

public class ApiClientConfigurationGenerator internal constructor(
    public val apiModel: ApiModel,
) {
    private companion object {
        private val logger = KotlinLogging.logger {}

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
        val cioMember: MemberName = MemberName("io.ktor.client.engine.cio", "CIO")
        val exceptionLoggerType: LambdaTypeName =
            LambdaTypeName.get(
                receiver = Throwable::class.asTypeName(),
                returnType = UNIT,
            )
    }

    private val hasApiKeys: Boolean = apiModel.apiKeySecuritySchemes.isNotEmpty()
    private val headerApiKeys: List<ApiSecurityScheme> =
        apiModel.apiKeySecuritySchemes.filter { it.location == ApiSecurityScheme.ApiKeyLocation.HEADER }
    private val queryApiKeys: List<ApiSecurityScheme> =
        apiModel.apiKeySecuritySchemes.filter { it.location == ApiSecurityScheme.ApiKeyLocation.QUERY }

    private fun buildDefaultConfigLambda(): CodeBlock {
        val builder =
            CodeBlock
                .builder()
                .beginControlFlow("{")
                .addStatement("install(%T)", loggingClass)
                .beginControlFlow("install(%T)", contentNegotiationClass)
                .addStatement("%M(%N)", jsonMember, "json")
                .endControlFlow()
                .beginControlFlow("%M", MemberName("io.ktor.client.plugins", "defaultRequest"))
                .addStatement("url(%N)", "baseUrl")

        // Add header API keys
        headerApiKeys.forEach { apiKey ->
            val paramName = apiKeyParamName(apiKey)
            builder.addStatement(
                "%N?.let { header(%S, it) }",
                paramName,
                apiKey.keyName,
            )
        }

        // Add query API keys
        queryApiKeys.forEach { apiKey ->
            val paramName = apiKeyParamName(apiKey)
            builder.addStatement(
                "%N?.let { url.parameters.append(%S, it) }",
                paramName,
                apiKey.keyName,
            )
        }

        builder
            .endControlFlow()
            .endControlFlow()

        return builder.build()
    }

    private fun buildDefaultHttpClientConfig(): FunSpec {
        val funBuilder =
            FunSpec
                .builder("defaultHttpClientConfig")
                .addParameter("baseUrl", String::class)
                .addParameter("json", Json::class)

        // Add API key parameters
        apiModel.apiKeySecuritySchemes.forEach { apiKey ->
            funBuilder.addParameter(
                ParameterSpec
                    .builder(apiKeyParamName(apiKey), String::class.asTypeName().copy(nullable = true))
                    .build(),
            )
        }

        return funBuilder
            .returns(httpClientConfigType)
            .addStatement("return %L", buildDefaultConfigLambda())
            .build()
    }

    private fun apiKeyParamName(apiKey: ApiSecurityScheme): String =
        apiKey.name
            .replace("_", " ")
            .split(" ")
            .mapIndexed { index, word -> if (index == 0) word.lowercase() else word.replaceFirstChar { it.uppercase() } }
            .joinToString("")

    // mutable properties for modules
    public val jsonDefaultValueProperties: MutableMap<String, String> = mutableMapOf("ignoreUnknownKeys" to "true")
    public var exceptionLoggingDefaultValue: String = "{ printStackTrace() }"
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
                        .defaultValue("%S", apiModel.serverUrl)
                        .build(),
                )

        // Add API key parameters
        apiModel.apiKeySecuritySchemes.forEach { apiKey ->
            builder.addParameter(
                ParameterSpec
                    .builder(apiKeyParamName(apiKey), String::class.asTypeName().copy(nullable = true))
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
            )

        // Build httpClientConfig default value with API keys
        val httpClientConfigDefaultValue =
            if (hasApiKeys) {
                val apiKeyParams = apiModel.apiKeySecuritySchemes.joinToString(", ") { apiKeyParamName(it) }
                CodeBlock.of("%N(%N, %N, $apiKeyParams)", "defaultHttpClientConfig", "baseUrl", "json")
            } else {
                CodeBlock.of("%N(%N, %N)", "defaultHttpClientConfig", "baseUrl", "json")
            }

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
        val parameterDefinitions = apiModel.componentParameters
        val companionBuilder = TypeSpec.companionObjectBuilder()

        companionBuilder.addProperty(
            PropertySpec
                .builder("defaultClientConfiguration", ClassName("", "ClientConfiguration"))
                .delegate("lazy { %L() }", "ClientConfiguration")
                .build(),
        )

        parameterDefinitions.forEach { parameter ->
            val name = constName(parameter.name)
            companionBuilder.addProperty(
                PropertySpec
                    .builder("PARAMETER_$name", String::class)
                    .addModifiers(KModifier.CONST)
                    .initializer("%S", parameter.name)
                    .build(),
            )

            val parameterTypeName =
                parameter.schema?.let { schema ->
                    apiModel.getClassName(name, schema)
                } ?: String::class.asTypeName()
            val defaultLiteral = parameterDefaultLiteral(parameter.schema, parameterTypeName)
            if (defaultLiteral != null && isConstSupported(parameterTypeName)) {
                companionBuilder.addProperty(
                    PropertySpec
                        .builder("PARAMETER_${name}_DEFAULT_VALUE", parameterTypeName)
                        .addModifiers(KModifier.CONST)
                        .initializer(defaultLiteral)
                        .build(),
                )
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

        // Add API key properties
        apiModel.apiKeySecuritySchemes.forEach { apiKey ->
            val paramName = apiKeyParamName(apiKey)
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
                .builder(apiModel.configuration.clientPackage, "ClientConfiguration")
                .addType(clientConfiguration)
                .build()
        val basePath = File(apiModel.outputDirectory).resolve("src/main/kotlin")
        logger.debug { "Writing ClientConfiguration to $basePath" }
        fileSpec.writeTo(basePath)
    }
}
