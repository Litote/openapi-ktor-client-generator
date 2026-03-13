package org.litote.openapi.ktor.client.generator

/**
 * Result of the API generation process.
 */
public sealed class GenerationResult {
    /**
     * Successful generation.
     * @param clientsGenerated Number of client files generated
     * @param modelsGenerated Number of model files generated
     */
    public data class Success(
        val clientsGenerated: Int,
        val modelsGenerated: Int,
    ) : GenerationResult()

    /**
     * Failed generation.
     * @param error The exception that caused the failure
     * @param message A descriptive error message
     */
    public data class Failure(
        val error: Throwable,
        val message: String,
    ) : GenerationResult()

    /**
     * Returns true if the generation was successful.
     */
    public val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if the generation failed.
     */
    public val isFailure: Boolean get() = this is Failure

    /**
     * Returns the success result or null if failed.
     */
    public fun getOrNull(): Success? = this as? Success

    /**
     * Returns the success result or throws the error if failed.
     */
    public fun getOrThrow(): Success =
        when (this) {
            is Success -> this
            is Failure -> throw error
        }
}
