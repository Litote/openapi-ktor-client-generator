package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import org.litote.openapi.ktor.client.generator.port.ApiTypeMappingConfig
import org.litote.openapi.ktor.client.generator.port.StringFormatType
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringFormatTypesTest {
    private val testSpec = "src/test/resources/string-formats.json"

    private val allFormatsModule =
        object : ApiGeneratorModule {
            override fun processTypeMapping(config: ApiTypeMappingConfig) {
                config.stringFormatTypes["date-time"] = StringFormatType("kotlin.time.Instant")
                config.stringFormatTypes["date"] = StringFormatType("kotlinx.datetime.LocalDate")
                config.stringFormatTypes["uuid"] = StringFormatType("kotlin.uuid.Uuid")
            }
        }

    private val truncatingModule =
        object : ApiGeneratorModule {
            override fun processTypeMapping(config: ApiTypeMappingConfig) {
                config.stringFormatTypes["date-time"] = StringFormatType("kotlin.time.Instant", formatSuffix = ".truncated()")
                config.stringFormatTypes["uuid"] = StringFormatType("kotlin.uuid.Uuid")
            }
        }

    private fun generateFiles(
        modules: List<ApiGeneratorModule>,
        splitByClient: Boolean = false,
        targetClientName: String? = null,
    ): Map<String, String> {
        val outputDir = Files.createTempDirectory("string-formats-test").toFile()
        try {
            val result =
                generate(
                    ApiGeneratorConfiguration(
                        openApiFile = testSpec,
                        outputDirectory = outputDir.absolutePath,
                        modules = modules,
                        splitByClient = splitByClient,
                        targetClientName = targetClientName,
                    ),
                )
            assertTrue(result.isSuccess, "Generation should succeed: $result")
            return outputDir
                .walkTopDown()
                .filter(File::isFile)
                .associate { it.name to it.readText() }
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `GIVEN string formats WHEN parsing THEN PrimitiveSpec keeps the format`() {
        val configuration = ApiGeneratorConfiguration(openApiFile = testSpec, outputDirectory = "")
        val spec = OpenApiSpecificationParser(configuration).parse { true }

        val event = spec.models.filterIsInstance<ModelSpec.DataClassSpec>().first { it.name == "Event" }
        val types = event.properties.associate { it.originalName to it.type }
        assertEquals(DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.STRING, format = "uuid"), types["id"])
        assertEquals(
            DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.STRING, nullable = true, format = "date-time"),
            types["updatedAt"],
        )
        assertEquals(
            DomainTypeSpec.ListTypeSpec(
                DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.STRING, format = "uuid"),
                nullable = true,
            ),
            types["relatedIds"],
        )
    }

    @Test
    fun `GIVEN no module WHEN generating THEN formatted strings stay String`() {
        val files = generateFiles(emptyList())

        val event = files.getValue("Event.kt")
        assertTrue(event.contains("public val id: String"), event)
        assertTrue(event.contains("public val createdAt: String"), event)
        assertTrue(event.contains("public val day: String"), event)
        assertTrue(event.contains("public val startDay: String? = \"2024-01-01\""), event)
        assertFalse(event.contains("kotlin.uuid"), event)
        val client = files.getValue("EventClient.kt")
        assertTrue(client.contains("eventId: String"), client)
        assertTrue(client.contains("day: String? = \"2024-01-31\""), client)
    }

    @Test
    fun `GIVEN format mappings WHEN generating models THEN rich types are used`() {
        val event = generateFiles(listOf(allFormatsModule)).getValue("Event.kt")

        assertTrue(event.contains("import kotlin.time.Instant"), event)
        assertTrue(event.contains("import kotlin.uuid.Uuid"), event)
        assertTrue(event.contains("import kotlinx.datetime.LocalDate"), event)
        assertTrue(event.contains("public val id: Uuid"), event)
        assertTrue(event.contains("public val createdAt: Instant"), event)
        assertTrue(event.contains("public val updatedAt: Instant? = null"), event)
        assertTrue(event.contains("public val day: LocalDate"), event)
        assertTrue(event.contains("public val relatedIds: List<Uuid>?"), event)
        assertTrue(event.contains("public val startDay: LocalDate? = LocalDate.parse(\"2024-01-01\")"), event)
    }

    @Test
    fun `GIVEN format mappings WHEN generating models THEN unmapped formats stay String`() {
        val event = generateFiles(listOf(allFormatsModule)).getValue("Event.kt")

        assertTrue(event.contains("public val contact: String?"), event)
        assertTrue(event.contains("public val attachment: String?"), event)
    }

    @Test
    fun `GIVEN format mappings WHEN generating client THEN parameters use rich types and are converted to string`() {
        val client = generateFiles(listOf(allFormatsModule)).getValue("EventClient.kt")

        assertTrue(client.contains("eventId: Uuid"), client)
        assertTrue(client.contains("since: Instant? = null"), client)
        assertTrue(client.contains("day: LocalDate? = LocalDate.parse(\"2024-01-31\")"), client)
        assertTrue(client.contains("eventId.toString().encodeURLPathPart()"), client)
        assertTrue(client.contains("parameters.append(\"since\", since.toString())"), client)
    }

    @Test
    fun `GIVEN a format with a custom suffix WHEN generating client THEN parameters and form fields use it`() {
        val client = generateFiles(listOf(truncatingModule)).getValue("EventClient.kt")

        assertTrue(client.contains("parameters.append(\"since\", since.truncated())"), client)
        assertTrue(client.contains("parameters.appendAll(\"slots\", slots.map { value -> value.truncated() })"), client)
        assertTrue(client.contains("append(\"at\", form.at.truncated())"), client)
        assertTrue(client.contains("append(\"until\", value.truncated())"), client)
    }

    @Test
    fun `GIVEN a format with a custom suffix WHEN generating models THEN properties use the generated serializer`() {
        val event = generateFiles(listOf(truncatingModule)).getValue("Event.kt")

        assertTrue(event.contains("public val createdAt: @Serializable(with = DateTimeFormatSerializer::class) Instant"), event)
        assertTrue(event.contains("public val updatedAt: @Serializable(with = DateTimeFormatSerializer::class) Instant? = null"), event)
        assertTrue(event.contains("public val history: List<@Serializable(with = DateTimeFormatSerializer::class) Instant>?"), event)
        assertTrue(event.contains("import org.example.client.DateTimeFormatSerializer"), event)
        assertTrue(event.contains("public val id: Uuid"), event)
    }

    @Test
    fun `GIVEN a format with a custom suffix WHEN generating THEN a serializer using it is generated`() {
        val serializers = generateFiles(listOf(truncatingModule)).getValue("StringFormatSerializers.kt")

        assertTrue(serializers.contains("public object DateTimeFormatSerializer : KSerializer<Instant>"), serializers)
        assertTrue(serializers.contains("encoder.encodeString(value.truncated())"), serializers)
        assertTrue(serializers.contains("Instant = Instant.parse(decoder.decodeString())"), serializers)
        assertFalse(serializers.contains("Uuid"), serializers)
    }

    @Test
    fun `GIVEN only toString formats WHEN generating THEN no serializer is generated`() {
        val files = generateFiles(listOf(allFormatsModule))

        assertFalse(files.containsKey("StringFormatSerializers.kt"), files.keys.toString())
        assertFalse(files.getValue("Event.kt").contains("FormatSerializer"))
    }

    @Test
    fun `GIVEN a format with a custom suffix and split by client WHEN generating THEN the serializer is only in the shared subproject`() {
        val clientFiles = generateFiles(listOf(truncatingModule), splitByClient = true, targetClientName = "EventClient")
        val sharedFiles = generateFiles(listOf(truncatingModule), splitByClient = true)

        assertFalse(clientFiles.containsKey("StringFormatSerializers.kt"), clientFiles.keys.toString())
        assertTrue(clientFiles.getValue("Event.kt").contains("DateTimeFormatSerializer::class"))
        assertTrue(sharedFiles.containsKey("StringFormatSerializers.kt"), sharedFiles.keys.toString())
    }

    @Test
    fun `GIVEN no module WHEN generating component parameter default THEN it is a const String`() {
        val configuration = generateFiles(emptyList()).getValue("ClientConfiguration.kt")

        assertTrue(configuration.contains("const val PARAMETER_TENANTID_DEFAULT_VALUE: String"), configuration)
    }

    @Test
    fun `GIVEN format mappings WHEN generating component parameter default THEN it is a parsed val`() {
        val files = generateFiles(listOf(allFormatsModule))
        val configuration = files.getValue("ClientConfiguration.kt")

        assertTrue(configuration.contains("public val PARAMETER_TENANTID_DEFAULT_VALUE: Uuid ="), configuration)
        assertTrue(configuration.contains("Uuid.parse(\"123e4567-e89b-12d3-a456-426614174000\")"), configuration)
        assertTrue(files.getValue("EventClient.kt").contains("tenantId: Uuid? = ClientConfiguration.PARAMETER_TENANTID_DEFAULT_VALUE"))
    }

    @Test
    fun `GIVEN format mappings and split by client WHEN generating client subproject THEN rich types are used`() {
        val files = generateFiles(listOf(allFormatsModule), splitByClient = true, targetClientName = "EventClient")

        assertTrue(files.getValue("EventClient.kt").contains("eventId: Uuid"))
        assertTrue(files.getValue("Event.kt").contains("public val id: Uuid"))
    }
}
