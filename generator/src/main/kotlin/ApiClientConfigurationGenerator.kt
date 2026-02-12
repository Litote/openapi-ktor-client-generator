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
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.serialization.json.Json
import java.io.File

public class ApiClientConfigurationGenerator internal constructor(public val apiModel: ApiModel) {

    private companion object {

        val engineFactoryType: ParameterizedTypeName =
            HttpClientEngineFactory::class.asTypeName().parameterizedBy(STAR)
        val httpClientConfigType: LambdaTypeName =
            LambdaTypeName.get(
                receiver = HttpClientConfig::class.asTypeName().parameterizedBy(STAR),
                returnType = UNIT
            )
        val loggingClass: ClassName = ClassName("io.ktor.client.plugins.logging", "Logging")
        val contentNegotiationClass: ClassName =
            ClassName("io.ktor.client.plugins.contentnegotiation", "ContentNegotiation")
        val jsonMember: MemberName = MemberName("io.ktor.serialization.kotlinx.json", "json")
        val cioMember: MemberName = MemberName("io.ktor.client.engine.cio", "CIO")
        val defaultConfigLambda: CodeBlock = CodeBlock.builder()
            .beginControlFlow("{")
            .addStatement("install(%T)", loggingClass)
            .beginControlFlow("install(%T)", contentNegotiationClass)
            .addStatement("%M(%N)", jsonMember, "json")
            .endControlFlow()
            .endControlFlow()
            .build()
        val defaultHttpClientConfig: FunSpec = FunSpec.builder("defaultHttpClientConfig")
            .addParameter("json", Json::class)
            .returns(httpClientConfigType)
            .addStatement("return %L", defaultConfigLambda)
            .build()
        val exceptionLoggerType: LambdaTypeName =
            LambdaTypeName.get(
                receiver = Throwable::class.asTypeName(),
                returnType = UNIT
            )
    }

    // mutable properties for modules
    public val jsonDefaultValueProperties: MutableMap<String, String> = mutableMapOf("ignoreUnknownKeys" to "true")
    public var exceptionLoggingDefaultValue: String = "{ printStackTrace() }"
    //end mutable properties for modules

    private val jsonDefaultValue: CodeBlock
        get() =
            CodeBlock.builder().add(
                "%T { ${
                    jsonDefaultValueProperties.entries.joinToString(separator = "\n", prefix = "\n", postfix = "\n") {
                        "${it.key} = ${it.value}"
                    }
                } }",
                Json::class
            ).build()

    internal fun buildConstructor(): FunSpec =
        FunSpec.constructorBuilder()
            .addParameter(
                ParameterSpec.builder("baseUrl", String::class)
                    .defaultValue("%S", apiModel.serverUrl)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder("engine", engineFactoryType)
                    .defaultValue("%M", cioMember)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder("json", Json::class)
                    .defaultValue(jsonDefaultValue)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder("httpClientConfig", httpClientConfigType)
                    .defaultValue("%N(%N)", "defaultHttpClientConfig", "json")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder("client", HttpClient::class)
                    .defaultValue("%T(%N) { %N() }", HttpClient::class, "engine", "httpClientConfig")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder("exceptionLogger", exceptionLoggerType)
                    .defaultValue(exceptionLoggingDefaultValue)
                    .build()
            )
            .build()

    internal fun buildCompanion(): TypeSpec {
        val parameterDefinitions = apiModel.componentParameters
        val companionBuilder = TypeSpec.companionObjectBuilder()

        companionBuilder.addProperty(
            PropertySpec.builder("defaultClientConfiguration", ClassName("", "ClientConfiguration"))
                .initializer("%L()", "ClientConfiguration")
                .build()
        )

        parameterDefinitions.forEach { parameter ->
            val name = constName(parameter.name)
            companionBuilder.addProperty(
                PropertySpec.builder("PARAMETER_$name", String::class)
                    .addModifiers(KModifier.CONST)
                    .initializer("%S", parameter.name)
                    .build()
            )

            val parameterTypeName = parameter.schema?.let { schema ->
                apiModel.getClassName(name, schema)
            } ?: String::class.asTypeName()
            val defaultLiteral = parameterDefaultLiteral(parameter.schema, parameterTypeName)
            if (defaultLiteral != null && isConstSupported(parameterTypeName)) {
                companionBuilder.addProperty(
                    PropertySpec.builder("PARAMETER_${name}_DEFAULT_VALUE", parameterTypeName)
                        .addModifiers(KModifier.CONST)
                        .initializer(defaultLiteral)
                        .build()
                )
            }
        }
        companionBuilder.addFunction(defaultHttpClientConfig)
        return companionBuilder.build()
    }

    internal fun buildClientConfiguration(constructor: FunSpec, companion: TypeSpec): TypeSpec =
        TypeSpec.classBuilder("ClientConfiguration")
            .primaryConstructor(constructor)
            .addProperty(
                PropertySpec.builder("baseUrl", String::class)
                    .initializer("baseUrl")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("engine", engineFactoryType)
                    .initializer("engine")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("json", Json::class)
                    .initializer("json")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("httpClientConfig", httpClientConfigType)
                    .initializer("httpClientConfig")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("client", HttpClient::class)
                    .initializer("client")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("exceptionLogger", exceptionLoggerType)
                    .initializer("exceptionLogger")
                    .build()
            )
            .addType(companion)
            .build()

    internal fun writeFile(clientConfiguration: TypeSpec) {
        val fileSpec = FileSpec.builder(apiModel.configuration.clientPackage, "ClientConfiguration")
            .addType(clientConfiguration)
            .build()
        val basePath = File(apiModel.outputDirectory).resolve("src/main/kotlin")
        println("Writing ClientConfiguration to $basePath")
        fileSpec.writeTo(basePath)
    }

}