# Decompose Detekt Rules
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ajiekcx.detekt/decompose-detekt-rules?label=Maven%20Central)](https://central.sonatype.com/namespace/io.github.ajiekcx.detekt)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

[Decompose](https://github.com/arkivanov/Decompose) is a Kotlin Multiplatform library for breaking down your code into tree-structured lifecycle-aware business logic components (aka BLoC), with routing functionality and pluggable UI.
The Decompose Detekt Rules are a set of custom detekt rules that help you avoid critical mistakes when working with Decompose.

## Quick start

Specify the dependency on this set of rules using `detektPlugins`:

```kotlin
detektPlugins("io.github.ajiekcx.detekt:decompose-detekt-rules:0.1.0")
```

## Rules

### DecomposeComponentContextRule

A common mistake is to create a Decompose `ComponentContext` inside a Composable function scope. For example:
```kotlin
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // DON'T DO THIS
            val context = defaultComponentContext()
        }
    }
}
```

If recomposition occurs without Activity recreation, you will get the following exception:
```
java.lang.IllegalArgumentException: SavedStateProvider with the given key is already registered
```

Avoid using the `defaultComponentContext` function inside Composable functions.

## Enabling rules

By default, all rules are enabled, but you can configure them in your `detekt.yml` configuration file:
```yaml
DecomposeRuleSet:
  DecomposeComponentContextRule:
    active: true
```
