package org.litote.openapi.ktor.client.generator.port

/** Configuration hook for the client configuration generator, exposed to [ApiGeneratorModule] implementors. */
public interface ConfigurationGeneratorConfig {
    /** Json properties added to the default Json configuration in the generated ClientConfiguration. */
    public val jsonDefaultValueProperties: MutableMap<String, String>

    /** Default lambda body for exception logging in the generated ClientConfiguration. */
    public var exceptionLoggingDefaultValue: String
}
