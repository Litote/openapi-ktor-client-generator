package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING

private const val KOTLINX_JSON = "kotlinx.serialization.json"

internal val encodeToJsonElementMember = MemberName(KOTLINX_JSON, "encodeToJsonElement")
internal val jsonObjectMember = MemberName(KOTLINX_JSON, "jsonObject")

private val jsonElementClass = ClassName(KOTLINX_JSON, "JsonElement")
private val jsonObjectClass = ClassName(KOTLINX_JSON, "JsonObject")
private val jsonArrayClass = ClassName(KOTLINX_JSON, "JsonArray")
private val jsonPrimitiveClass = ClassName(KOTLINX_JSON, "JsonPrimitive")
private val jsonNullClass = ClassName(KOTLINX_JSON, "JsonNull")
private val parametersBuilderClass = ClassName("io.ktor.http", "ParametersBuilder")

internal const val APPEND_EXPLODED_OBJECT = "appendExplodedObject"
internal const val APPEND_DEEP_OBJECT = "appendDeepObject"
internal const val TO_DELIMITED_STRING = "toDelimitedString"
private const val TO_PARAMETER_VALUES = "toParameterValues"

/** Private helpers generated in a client class to serialize object parameters (flattened at runtime from JSON). */
internal enum class ParameterSerializationHelper {
    /** `form` + `explode=true` query object: one query parameter per property. */
    EXPLODED_OBJECT,

    /** `deepObject` query object: `name[property]=value`. */
    DEEP_OBJECT,

    /** Unexploded object: `k1,v1,k2,v2` (or `k1=v1,k2=v2` for exploded headers). */
    DELIMITED_OBJECT,
}

/** Builds the helper functions needed by [helpers], in a stable order. */
internal fun buildParameterSerializationHelpers(helpers: Set<ParameterSerializationHelper>): List<FunSpec> =
    if (helpers.isEmpty()) {
        emptyList()
    } else {
        ParameterSerializationHelper.entries
            .filter { it in helpers }
            .map { helper ->
                when (helper) {
                    ParameterSerializationHelper.EXPLODED_OBJECT -> appendExplodedObjectFun()
                    ParameterSerializationHelper.DEEP_OBJECT -> appendDeepObjectFun()
                    ParameterSerializationHelper.DELIMITED_OBJECT -> toDelimitedStringFun()
                }
            } + toParameterValuesFun()
    }

private fun appendExplodedObjectFun(): FunSpec =
    FunSpec
        .builder(APPEND_EXPLODED_OBJECT)
        .addModifiers(KModifier.PRIVATE)
        .receiver(parametersBuilderClass)
        .addParameter("value", jsonObjectClass)
        .addStatement("value.forEach { (key, element) -> appendAll(key, element.$TO_PARAMETER_VALUES()) }")
        .build()

private fun appendDeepObjectFun(): FunSpec =
    FunSpec
        .builder(APPEND_DEEP_OBJECT)
        .addModifiers(KModifier.PRIVATE)
        .receiver(parametersBuilderClass)
        .addParameter("name", STRING)
        .addParameter("value", jsonElementClass)
        .beginControlFlow("when (value)")
        .addStatement("is %T -> value.forEach { (key, element) -> $APPEND_DEEP_OBJECT(\"\$name[\$key]\", element) }", jsonObjectClass)
        .addStatement("is %T -> value.forEach { $APPEND_DEEP_OBJECT(name, it) }", jsonArrayClass)
        .addStatement("else -> value.$TO_PARAMETER_VALUES().forEach { append(name, it) }")
        .endControlFlow()
        .build()

private fun toDelimitedStringFun(): FunSpec =
    FunSpec
        .builder(TO_DELIMITED_STRING)
        .addModifiers(KModifier.PRIVATE)
        .receiver(jsonObjectClass)
        .returns(STRING)
        .addParameter("separator", STRING)
        .addParameter(ParameterSpec.builder("keyValueSeparator", STRING).defaultValue("separator").build())
        .addStatement(
            "return entries.filter { it.value !is %T }.joinToString(separator) { (key, element) -> " +
                "key + keyValueSeparator + element.$TO_PARAMETER_VALUES().joinToString(\",\") }",
            jsonNullClass,
        ).build()

private fun toParameterValuesFun(): FunSpec =
    FunSpec
        .builder(TO_PARAMETER_VALUES)
        .addModifiers(KModifier.PRIVATE)
        .receiver(jsonElementClass)
        .returns(LIST.parameterizedBy(STRING))
        .beginControlFlow("return when (this)")
        .addStatement("is %T -> emptyList()", jsonNullClass)
        .addStatement("is %T -> listOf(content)", jsonPrimitiveClass)
        .addStatement("is %T -> flatMap { it.$TO_PARAMETER_VALUES() }", jsonArrayClass)
        .addStatement("is %T -> listOf(toString())", jsonObjectClass)
        .endControlFlow()
        .build()
