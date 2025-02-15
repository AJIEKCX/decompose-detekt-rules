package io.github.ajiekcx.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClass

/**
 * A custom Detekt rule to prevent the use of `defaultComponentContext`
 * inside Composable functions.
 *
 * This rule applies only within `Activity` or `Fragment` classes and checks:
 * 1. If the file imports `com.arkivanov.decompose.defaultComponentContext`.
 * 2. If `defaultComponentContext` is used inside a Composable function.
 * 3. If `defaultComponentContext` is used inside `setContent {}`.
 */
class DecomposeComponentContextRule(config: Config) : Rule(config) {
    override val issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.CodeSmell,
        description = "Avoid using defaultComponentContext inside Composable functions.",
        debt = Debt(mins = 1)
    )

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        // Ensure the function is inside an Activity or Fragment
        val ktClass = function.containingClass() ?: return
        if (ktClass.isActivity().not() && ktClass.isFragment().not()) return

        // Check if the file imports `defaultComponentContext`
        if (function.containingKtFile.hasDefaultComponentContextImport().not()) return

        if (function.isComposableFun()) {
            // Directly scan the function body for `defaultComponentContext
            function.bodyExpression?.findDefaultComponentContextAndReport()
        } else {
            // If it's not a Composable, check if `setContent {}` is used
            val setContentCall = function.findDescendantCalleeExpression("setContent")
                ?: return

            // Extract the lambda block inside `setContent`
            val lambdaExpression =
                setContentCall.lambdaArguments.firstOrNull()?.getLambdaExpression() ?: return

            // Scan the lambda block for `defaultComponentContext`
            lambdaExpression.bodyExpression?.findDefaultComponentContextAndReport()
        }
    }

    /**
     * Checks if the Kotlin file imports `defaultComponentContext`.
     */
    private fun KtFile.hasDefaultComponentContextImport(): Boolean {
        return hasImport("import com.arkivanov.decompose.defaultComponentContext")
    }

    /**
     * Finds occurrences of `defaultComponentContext` inside an expression
     * and reports them as a code smell.
     */
    private fun KtExpression.findDefaultComponentContextAndReport() {
        val expression = findDescendantCalleeExpression("defaultComponentContext") ?: return

        report(
            CodeSmell(
                issue = issue,
                entity = Entity.from(expression),
                message = "Avoid using defaultComponentContext inside Composable functions."
            )
        )
    }
}
