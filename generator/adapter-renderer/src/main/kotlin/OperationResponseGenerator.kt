package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import org.litote.openapi.ktor.client.generator.port.ApiFileSystemWriter

/**
 * Names of the interfaces implemented by the response types of every operation, generated next to `ClientConfiguration`.
 */
internal class OperationResponseTypes(
    packageName: String,
) {
    val response: ClassName = ClassName(packageName, "OperationResponse")
    val success: ClassName = ClassName(packageName, "OperationResponseSuccess")
    val failure: ClassName = ClassName(packageName, "OperationResponseFailure")
    val unknownFailure: ClassName = ClassName(packageName, "OperationResponseUnknownFailure")
}

/**
 * Generates the interfaces common to the response types of all the operations, so that they can be handled generically
 * (e.g. one function returning the body of any successful response, or throwing on failures).
 */
internal class OperationResponseGenerator(
    private val clientPackage: String,
    private val outputDirectory: String,
    private val fileSystemWriter: ApiFileSystemWriter,
) {
    private companion object {
        const val FILE_NAME = "OperationResponse"
        const val BODY_PROPERTY = "body"
        val headersClass: ClassName = ClassName("io.ktor.http", "Headers")
        val outT: TypeVariableName = TypeVariableName("T", variance = KModifier.OUT)
        val outE: TypeVariableName = TypeVariableName("E", variance = KModifier.OUT)
    }

    fun render() {
        val types = OperationResponseTypes(clientPackage)
        val fileSpec =
            FileSpec
                .builder(clientPackage, FILE_NAME)
                .addType(
                    TypeSpec
                        .interfaceBuilder(types.response)
                        .addKdoc("Response of an operation: [T] is the body type of its successful responses.\n")
                        .addTypeVariable(outT)
                        .addProperty(PropertySpec.builder("headers", headersClass).build())
                        .build(),
                ).addType(
                    TypeSpec
                        .interfaceBuilder(types.success)
                        .addKdoc("Successful response declared by the specification ([body] is `Unit` when it has none).\n")
                        .addTypeVariable(outT)
                        .addSuperinterface(types.response.parameterizedBy(TypeVariableName("T")))
                        .addProperty(PropertySpec.builder(BODY_PROPERTY, TypeVariableName("T")).build())
                        .build(),
                ).addType(
                    TypeSpec
                        .interfaceBuilder(types.failure)
                        .addKdoc("Failure response declared by the specification, of body [E] (`Unit` when it has none).\n")
                        .addTypeVariable(outE)
                        .addProperty(PropertySpec.builder(BODY_PROPERTY, TypeVariableName("E")).build())
                        .build(),
                ).addType(
                    TypeSpec
                        .interfaceBuilder(types.unknownFailure)
                        .addKdoc(
                            "Response with a status not declared by the specification, or failure of the call " +
                                "(network error, unreadable body: status 500).\n",
                        ).addProperty(PropertySpec.builder("statusCode", INT).build())
                        .build(),
                ).build()
        fileSystemWriter.write(fileSpec.toGeneratedFile(), outputDirectory)
    }
}
