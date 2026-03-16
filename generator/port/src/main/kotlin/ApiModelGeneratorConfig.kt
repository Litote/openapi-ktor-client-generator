package org.litote.openapi.ktor.client.generator.port

/** Configuration hook for the model generator, exposed to [org.litote.openapi.ktor.client.generator.ApiGeneratorModule] implementors. */
public interface ApiModelGeneratorConfig {
    /** When set, this value is appended as the last enum constant to handle unknown values. */
    public var defaultEnumValue: String?
}
