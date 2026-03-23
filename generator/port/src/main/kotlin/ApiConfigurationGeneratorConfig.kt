package org.litote.openapi.ktor.client.generator.port

/** Configuration hook for the client configuration generator, exposed to `ApiGeneratorModule` implementors. */
public interface ApiConfigurationGeneratorConfig {
    /** Json properties added to the default Json configuration in the generated ClientConfiguration. */
    public val jsonDefaultValueProperties: MutableMap<String, String>

    /** Default lambda body for exception logging in the generated ClientConfiguration. */
    public var exceptionLoggingDefaultValue: String

    /**
     * Default lambda body for the `httpClientAuthorization` parameter in the generated ClientConfiguration.
     * The lambda type is `HttpClientConfig<*>.() -> Unit`.
     * Default is `{}` (no-op). Modules can override this to inject authorization logic.
     */
    public var httpClientAuthorizationDefaultValue: String

    /**
     * Additional nullable `String` parameters to inject into the ClientConfiguration constructor,
     * placed before `httpClientAuthorization`. Each entry is a parameter name; the type is always
     * `String?` with a default value of `null`.
     */
    public val additionalStringParameters: MutableList<String>
}
