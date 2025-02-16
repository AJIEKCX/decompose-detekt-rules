package io.github.ajiekcx.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.psiUtil.findPropertyByName

/**
 * A Detekt rule that checks for properties named as class discriminators
 * in polymorphic serializable classes. It prevents naming properties
 * using a reserved discriminator (e.g., "type") to avoid serialization issues.
 *
 * @see https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md
 */
class SerializableDiscriminatorRule(config: Config) : Rule(config) {
    // Retrieves the forbidden property name from the configuration (default is "type")
    private val classDiscriminator = config
        .subConfig("SerializableDiscriminatorRule")
        .valueOrDefault("classDiscriminator", "type")

    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "Do not name properties in polymorphic serializable classes as a class discriminator: $classDiscriminator",
        Debt.FIVE_MINS
    )

    /**
     * Visits the entire Kotlin file and checks for serializable sealed class hierarchies.
     * It ensures that subclasses of sealed serializable classes do not contain a property
     * named as the forbidden class discriminator.
     */
    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)

        // Skip processing if the file does not import kotlinx.serialization.Serializable
        if (!file.hasSerializableImport()) return

        // Iterate through all declarations in the file, filtering only sealed serializable classes
        file.declarations
            .filterIsInstance<KtClass>()
            .filter { it.isSealed() && it.isSerializable() }
            .forEach { sealedClass ->
                // Find all serializable subclasses of the sealed class and check for violations
                file.declarations
                    .filterIsInstance<KtClass>()
                    .filter { it.isSubclassOf(sealedClass) && it.isSerializable() }
                    .forEach { it.findAndReportClassDiscriminatorProperty() }
            }
    }

    /**
     * Visits each class declaration and checks for forbidden properties if the class
     * is a sealed and contains nested serializable classes.
     */
    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)

        // Skip processing if the containing file does not import kotlinx.serialization.Serializable
        if (!klass.containingKtFile.hasSerializableImport()) return

        // If the class is a sealed class and is marked as @Serializable
        if (klass.isSealed() && klass.isSerializable()) {
            // Check nested classes inside the sealed class
            klass.declarations
                .filterIsInstance<KtClass>()
                .filter { it.isSerializable() }
                .forEach { nestedClass ->
                    nestedClass.findAndReportClassDiscriminatorProperty()
                }
        }
    }

    /**
     * Checks whether a Kotlin file has the required import for kotlinx.serialization.Serializable.
     */
    private fun KtFile.hasSerializableImport(): Boolean {
        return hasImport("import kotlinx.serialization.Serializable")
    }

    /**
     * Finds and reports properties that match the forbidden class discriminator name.
     */
    private fun KtClass.findAndReportClassDiscriminatorProperty() {
        // If a property is explicitly annotated with @SerialName(classDiscriminator), report an issue
        if (findPropertyWithSerialName(classDiscriminator)) {
            reportIssue()

            return
        }

        // Otherwise, check if the property exists by name
        val property = findPropertyByName(classDiscriminator) ?: return

        // If the property has a different @SerialName annotation, do not report it
        if (property.hasSerialNameWithoutClassDiscriminator()) return

        // Report an issue if the property is named as the class discriminator
        reportIssue()
    }

    /**
     * Checks if a class has a primary constructor parameter with @SerialName matching the forbidden name.
     */
    private fun KtClass.findPropertyWithSerialName(name: String): Boolean {
        // Ensure the necessary import for @SerialName exists
        val hasImport = containingKtFile.hasSerialNameImport()

        return hasImport && primaryConstructorParameters.any { param ->
            param.hasValOrVar() && param.annotationEntries.any { it.text == "@SerialName(\"$name\")" }
        }
    }

    /**
     * Checks if a named property has an @SerialName annotation that does not match the forbidden discriminator.
     */
    private fun KtNamedDeclaration.hasSerialNameWithoutClassDiscriminator(): Boolean {
        val hasImport = containingKtFile.hasSerialNameImport()
        if (!hasImport) return false

        val serialNameAnnotation = annotationEntries.find { it.text.startsWith("@SerialName") }

        return serialNameAnnotation != null
                && serialNameAnnotation.text != "@SerialName(\"$classDiscriminator\")"
    }

    /**
     * Checks whether the Kotlin file has an import statement for kotlinx.serialization.SerialName.
     */
    private fun KtFile.hasSerialNameImport(): Boolean {
        return hasImport("import kotlinx.serialization.SerialName")
    }

    /**
     * Reports a code smell issue when a forbidden class discriminator property is found.
     */
    private fun KtClass.reportIssue() {
        report(
            CodeSmell(
                issue,
                Entity.from(this),
                """
                    Do not name properties in polymorphic serializable classes as a class discriminator: $classDiscriminator"
                    See https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md
                """.trimIndent()
            )
        )
    }
}
