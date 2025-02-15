package io.github.ajiekcx.detekt

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * Finds a descendant function call expression with a specific name.
 *
 * @param text The function name to search for (e.g., "setContent").
 * @return The matching `KtCallExpression`, or `null` if not found.
 */
internal fun KtExpression.findDescendantCalleeExpression(text: String): KtCallExpression? {
    return collectDescendantsOfType<KtCallExpression>()
        .find { it.calleeExpression?.text == text }
}

/**
 * Checks if the Kotlin file contains a specific import statement.
 *
 * @param import The fully qualified import statement to check.
 * @return `true` if the import exists, otherwise `false`.
 */
internal fun KtFile.hasImport(import: String): Boolean {
    return importDirectives.any { it.text == import }
}

/**
 * Determines if a function is marked with the `@Composable` annotation.
 *
 * @return `true` if the function is Composable, otherwise `false`.
 */
internal fun KtFunction.isComposableFun(): Boolean {
    return annotationEntries.any { it.text == "@Composable" }
}

/**
 * Determines if a class extends an `Activity`.
 *
 * @return `true` if the class is an Activity, otherwise `false`.
 */
internal fun KtClass.isActivity(): Boolean {
    return hasSuperTypeEndingWith("Activity")
}

/**
 * Determines if a class extends a `Fragment`.
 *
 * @return `true` if the class is a Fragment, otherwise `false`.
 */
internal fun KtClass.isFragment(): Boolean {
    return hasSuperTypeEndingWith("Fragment")
}

/**
 * Checks if the class has a superclass that ends with a given suffix.
 *
 * This is useful for detecting inheritance from `Activity`, `Fragment`, or any custom base classes.
 *
 * @param suffix The suffix to check (e.g., "Activity", "Fragment").
 * @return `true` if the class inherits from a matching type, otherwise `false`.
 */
private fun KtClass.hasSuperTypeEndingWith(suffix: String): Boolean {
    return superTypeListEntries
        .mapNotNull { it.typeReference?.text }
        .any { it.endsWith(suffix) }
}
