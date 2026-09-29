package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.OperationParameterSpec
import org.litote.openapi.ktor.client.generator.domain.ParameterLocationSpec
import org.litote.openapi.ktor.client.generator.domain.ParameterStyleSpec
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParameterStylesTest {
    private companion object {
        val config =
            ApiGeneratorConfiguration(
                openApiFile = "src/test/resources/parameter-styles.json",
                outputDirectory = "build/parameter-styles-test",
                basePackage = "styles.test",
            )

        val spec: GenerationSpec by lazy { OpenApiSpecificationParser(config).parse(config.operationFilter) }

        /** Generated once for the class: tests run concurrently and would otherwise race on the output directory. */
        val clientSource: String by lazy {
            File(config.outputDirectory).deleteRecursively()
            val result = generate(config)
            assertTrue(result.isSuccess, "Generation should succeed: $result")
            File(config.outputDirectory)
                .walkTopDown()
                .first { it.name == "ItemsClient.kt" }
                .readText()
        }
    }

    private fun parameter(originalName: String): OperationParameterSpec =
        spec.clients
            .flatMap { it.operations }
            .first { it.name == "ListItems" }
            .parameters
            .first { it.originalName == originalName }

    private fun assertGenerated(code: String) = assertTrue(code in clientSource, "Missing `$code` in:\n$clientSource")

    @Test
    fun `GIVEN query array without style WHEN parsing THEN style is form and explode is true`() {
        val ids = parameter("ids")

        assertEquals(ParameterLocationSpec.QUERY, ids.location)
        assertEquals(ParameterStyleSpec.FORM, ids.style)
        assertTrue(ids.explode)
        assertFalse(ids.isObject)
    }

    @Test
    fun `GIVEN declared styles WHEN parsing THEN style and explode are kept`() {
        assertEquals(ParameterStyleSpec.FORM, parameter("tagsCsv").style)
        assertFalse(parameter("tagsCsv").explode)
        assertEquals(ParameterStyleSpec.SPACE_DELIMITED, parameter("tagsSpace").style)
        assertEquals(ParameterStyleSpec.PIPE_DELIMITED, parameter("tagsPipe").style)
        assertEquals(ParameterStyleSpec.DEEP_OBJECT, parameter("filter").style)
    }

    @Test
    fun `GIVEN object query parameters WHEN parsing THEN they are flagged as objects`() {
        assertTrue(parameter("filter").isObject)
        assertTrue(parameter("range").isObject)
        assertFalse(parameter("range").explode)
        assertTrue(parameter("page").isObject)
        assertTrue(parameter("page").explode)
    }

    @Test
    fun `GIVEN models referenced only by parameters WHEN generating THEN models are generated`() {
        clientSource // triggers generation
        val modelFiles =
            File(config.outputDirectory)
                .walkTopDown()
                .map { it.name }
                .toSet()

        assertTrue("Range.kt" in modelFiles, modelFiles.toString())
        assertTrue("Pagination.kt" in modelFiles, modelFiles.toString())
    }

    @Test
    fun `GIVEN inline object parameter WHEN generating THEN parameter is typed with the inline class`() {
        assertEquals(DomainTypeSpec.InlineTypeSpec("Filter").asNullable(), parameter("filter").type)
        assertGenerated("filter: Filter? = null")
    }

    @Test
    fun `GIVEN referenced enum parameters WHEN generating THEN enum serial names are sent`() {
        assertTrue(parameter("level").isEnum)
        assertTrue(parameter("levels").isEnumArray)
        assertGenerated("parameters.append(\"level\", level.serialName())")
        assertGenerated("parameters.appendAll(\"levels\", levels.map { it.serialName() })")
        assertGenerated("setHeader(\"X-Level\", xLevel.serialName())")
    }

    @Test
    fun `GIVEN array and free-form parameters WHEN generating THEN no empty object is generated`() {
        listOf("Ids", "TagsCsv", "TagsSpace", "TagsPipe", "Prefs", "XRequestIds", "Levels", "XFreeForm").forEach { name ->
            assertFalse("public object $name" in clientSource, "Unexpected object $name in:\n$clientSource")
        }
    }

    @Test
    fun `GIVEN free-form object parameter WHEN generating THEN it is not flattened`() {
        assertFalse(parameter("X-Free-Form").isObject)
        assertGenerated("setHeader(\"X-Free-Form\", xFreeForm)")
    }

    @Test
    fun `GIVEN cookie parameters WHEN parsing THEN location is cookie`() {
        val session = parameter("session")

        assertEquals(ParameterLocationSpec.COOKIE, session.location)
        assertTrue(session.isCookie)
        assertEquals(ParameterStyleSpec.FORM, session.style)
        assertEquals(ParameterLocationSpec.COOKIE, parameter("prefs").location)
    }

    @Test
    fun `GIVEN header parameter WHEN parsing THEN style is simple and explode is false`() {
        val header = parameter("X-Request-Ids")

        assertEquals(ParameterStyleSpec.SIMPLE, header.style)
        assertFalse(header.explode)
    }

    @Test
    fun `GIVEN unsupported style combinations WHEN parsing THEN style falls back to location default`() {
        assertEquals(ParameterStyleSpec.FORM, parameter("invalidMatrix").style)
        assertEquals(ParameterStyleSpec.FORM, parameter("invalidDeep").style)
    }

    @Test
    fun `GIVEN query arrays WHEN generating THEN arrays are serialized according to their style`() {
        assertGenerated("parameters.appendAll(\"ids\", ids)")
        assertGenerated("parameters.append(\"tagsCsv\", tagsCsv.joinToString(\",\"))")
        assertGenerated("parameters.append(\"tagsSpace\", tagsSpace.joinToString(\" \"))")
        assertGenerated("parameters.append(\"tagsPipe\", tagsPipe.joinToString(\"|\"))")
        assertGenerated("parameters.appendAll(\"statuses\", statuses.map { it.serialName() })")
    }

    @Test
    fun `GIVEN query objects WHEN generating THEN objects are flattened at runtime`() {
        assertGenerated("parameters.appendDeepObject(\"filter\", configuration.json.encodeToJsonElement(filter))")
        assertGenerated(
            "parameters.append(\"range\", configuration.json.encodeToJsonElement(range).jsonObject.toDelimitedString(\",\"))",
        )
        assertGenerated("parameters.appendExplodedObject(configuration.json.encodeToJsonElement(page).jsonObject)")
        assertGenerated("private fun ParametersBuilder.appendDeepObject(")
    }

    @Test
    fun `GIVEN cookie and header parameters WHEN generating THEN values are serialized`() {
        assertGenerated("cookie(\"session\", session)")
        assertGenerated("cookie(\"prefs\", prefs.joinToString(\",\"))")
        assertGenerated("setHeader(\"X-Request-Ids\", xRequestIds.joinToString(\",\"))")
        assertGenerated("setHeader(\"X-Mode\", xMode.serialName())")
        assertFalse("parameters.append(\"session\"" in clientSource, clientSource)
    }
}
