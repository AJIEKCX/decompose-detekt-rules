package io.github.ajiekcx.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class SerializableDiscriminatorRuleTest(private val env: KotlinCoreEnvironment) {
    private val defaultRule = SerializableDiscriminatorRule(Config.empty)

    @Test
    fun `reports type property in serializable class`() {
        val code = """
        import kotlinx.serialization.Serializable

        @Serializable
        sealed class ScreenConfig {
            @Serializable
            data object Input : ScreenConfig()
        
            @Serializable
            data class Details(
                val message: String,
                val type: String
            ) : ScreenConfig()
        }
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports type property in serializable private class`() {
        val code = """
        import kotlinx.serialization.Serializable

        @Serializable
        sealed class ScreenConfig {
            @Serializable
            data object Input : ScreenConfig()
        
            @Serializable
            data class Details(
                val message: String,
                val type: String
            ) : ScreenConfig()
        }
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports type property in serializable class with custom config`() {
        val code = """
        import kotlinx.serialization.Serializable

        @Serializable
        sealed class ScreenConfig {
            @Serializable
            data object Input : ScreenConfig()
        
            @Serializable
            data class Details(
                val message: String,
                val customDiscriminator: String
            ) : ScreenConfig()
        }
        """
        val rule = SerializableDiscriminatorRule(CustomConfig("customDiscriminator"))
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports type property in serializable interface in file`() {
        val code = """
        import kotlinx.serialization.Serializable

        @Serializable
        sealed interface ScreenConfig

        @Serializable
        data object InputConfig : ScreenConfig
    
        @Serializable
        data class DetailsConfig(
            val message: String,
            val type: String
        ) : ScreenConfig
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports type property in serializable class in file`() {
        val code = """
        import kotlinx.serialization.Serializable

        @Serializable
        sealed class ScreenConfig

        @Serializable
        data object InputConfig : ScreenConfig()
    
        @Serializable
        data class DetailsConfig(
            val message: String,
            val type: String
        ) : ScreenConfig()
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports type property in serializable interface`() {
        val code = """
        import kotlinx.serialization.Serializable

        @Serializable
        sealed interface ScreenConfig {
            @Serializable
            data object Input : ScreenConfig
        
            @Serializable
            data class Details(
                val message: String,
                val type: String
            ) : ScreenConfig
        }
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports type property with the same SerialName in serializable class`() {
        val code = """
        import kotlinx.serialization.Serializable
        import kotlinx.serialization.SerialName

        @Serializable
        sealed class ScreenConfig {
            @Serializable
            data object Input : ScreenConfig()
        
            @Serializable
            data class Details(
                val message: String,
                @SerialName("type")
                val type: String
            ) : ScreenConfig()
        }
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `doesn't report type property in serializable class in file without supertype`() {
        val code = """
        import kotlinx.serialization.Serializable

        @Serializable
        sealed class ScreenConfig

        @Serializable
        data object InputConfig : ScreenConfig()
    
        @Serializable
        data class DetailsConfig(
            val message: String,
            val type: String
        )
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report without type property in serializable class`() {
        val code = """
        import kotlinx.serialization.Serializable

        @Serializable
        sealed class ScreenConfig {
            @Serializable
            data object Input : ScreenConfig()
        
            @Serializable
            data class Details(
                val message: String
            ) : ScreenConfig()
        }
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report without type property in serializable class in file`() {
        val code = """
        import kotlinx.serialization.Serializable

        @Serializable
        sealed class ScreenConfig

        @Serializable
        data object InputConfig : ScreenConfig()
    
        @Serializable
        data class DetailsConfig(
            val message: String
        ) : ScreenConfig()
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report without type property in serializable interface`() {
        val code = """
        import kotlinx.serialization.Serializable

        @Serializable
        sealed interface ScreenConfig {
            @Serializable
            data object Input : ScreenConfig
        
            @Serializable
            data class Details(
                val message: String
            ) : ScreenConfig
        }
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report type property with different SerialName in serializable class`() {
        val code = """
        import kotlinx.serialization.Serializable
        import kotlinx.serialization.SerialName

        @Serializable
        sealed class ScreenConfig {
            @Serializable
            data object Input : ScreenConfig()
        
            @Serializable
            data class Details(
                val message: String,
                @SerialName("t")
                val type: String
            ) : ScreenConfig()
        }
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `reports property with type SerialName in serializable class`() {
        val code = """
        import kotlinx.serialization.Serializable
        import kotlinx.serialization.SerialName

        @Serializable
        sealed class ScreenConfig {
            @Serializable
            data object Input : ScreenConfig()
        
            @Serializable
            data class Details(
                val message: String,
                @SerialName("type")
                val messageType: String
            ) : ScreenConfig()
        }
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `doesn't report type property in serializable data class`() {
        val code = """
        import kotlinx.serialization.Serializable

        @Serializable
        data class Details(
            val message: String,
            val type: String
        )
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report type property in non serializable class`() {
        val code = """
        sealed class ScreenConfig {
            data object Input : ScreenConfig()

            data class Details(
                val message: String,
                val type: String
            ) : ScreenConfig()
        }
        """
        val findings = defaultRule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    private class CustomConfig(private val classDiscriminator: String) : Config {
        override fun subConfig(key: String): CustomConfig = this

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> valueOrNull(key: String): T? = when (key) {
            Config.ACTIVE_KEY -> true as? T
            else -> null
        }

        override fun <T : Any> valueOrDefault(key: String, default: T): T {
            if (key == "classDiscriminator") {
                return classDiscriminator as T
            }

            return super.valueOrDefault(key, default)
        }
    }
}
