package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.domain.ComponentParameterSpec
import org.litote.openapi.ktor.client.generator.domain.DefaultValueSpec
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DomainSpecTest {
    @Test
    fun `GIVEN DefaultValueSpec WHEN constructing all subtypes THEN values are accessible`() {
        val string = DefaultValueSpec.StringDefaultSpec("hello")
        val bool = DefaultValueSpec.BooleanDefaultSpec(true)
        val int = DefaultValueSpec.IntDefaultSpec(42)
        val long = DefaultValueSpec.LongDefaultSpec(100L)
        val double = DefaultValueSpec.DoubleDefaultSpec(3.14)
        val float = DefaultValueSpec.FloatDefaultSpec(1.5f)
        val enum = DefaultValueSpec.EnumDefaultSpec("Status", "ACTIVE")

        assertEquals("hello", string.value)
        assertEquals(true, bool.value)
        assertEquals(42, int.value)
        assertEquals(100L, long.value)
        assertEquals(3.14, double.value)
        assertEquals(1.5f, float.value)
        assertEquals("Status", enum.typeName)
        assertEquals("ACTIVE", enum.enumValue)
    }

    @Test
    fun `GIVEN ComponentParameterSpec WHEN created without defaultValue THEN defaultValue is null`() {
        val spec =
            ComponentParameterSpec(
                originalName = "x-api-version",
                constName = "PARAMETER_API_VERSION",
                type = DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.STRING),
            )

        assertEquals("x-api-version", spec.originalName)
        assertEquals("PARAMETER_API_VERSION", spec.constName)
        assertNull(spec.defaultValue)
    }

    @Test
    fun `GIVEN ComponentParameterSpec WHEN created with defaultValue THEN defaultValue is accessible`() {
        val defaultValue = DefaultValueSpec.StringDefaultSpec("v1")
        val spec =
            ComponentParameterSpec(
                originalName = "x-api-version",
                constName = "PARAMETER_API_VERSION",
                type = DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.STRING),
                defaultValue = defaultValue,
            )

        assertNotNull(spec.defaultValue)
        assertEquals(defaultValue, spec.defaultValue)
    }

    @Test
    fun `GIVEN ApiGeneratorModule WHEN getModule called with unknown id THEN returns null`() {
        val result = ApiGeneratorModule.getModule("unknown-module-id-that-does-not-exist")
        assertNull(result)
    }

    @Test
    fun `GIVEN ApiGeneratorModule implementation WHEN id accessed THEN returns simple class name`() {
        val module = object : ApiGeneratorModule {}
        // Anonymous objects have null simpleName, but named classes return their simple name
        val namedModule = NamedTestModule()
        assertEquals("NamedTestModule", namedModule.id)
    }
}

private class NamedTestModule : ApiGeneratorModule
