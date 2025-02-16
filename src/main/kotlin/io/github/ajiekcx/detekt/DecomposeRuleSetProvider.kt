package io.github.ajiekcx.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class DecomposeRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "DecomposeRuleSet"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(
                DecomposeComponentContextRule(config),
                SerializableDiscriminatorRule(config)
            ),
        )
    }
}
