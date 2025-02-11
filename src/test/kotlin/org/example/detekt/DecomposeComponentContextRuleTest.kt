package org.example.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class DecomposeComponentContextRuleTest(private val env: KotlinCoreEnvironment) {
    private val rule = DecomposeComponentContextRule(Config.empty)

    @Test
    fun `reports defaultComponentContext inside setContent in Activity`() {
        val code = """
        import androidx.appcompat.app.AppCompatActivity
        import androidx.compose.runtime.Composable
        import androidx.activity.compose.setContent
        import com.arkivanov.decompose.defaultComponentContext

        class MainActivity : AppCompatActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                setContent {
                    val context = defaultComponentContext()
                } 
            }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports defaultComponentContext inside setContent in Fragment`() {
        val code = """
        import com.arkivanov.decompose.defaultComponentContext
        import androidx.compose.ui.platform.ComposeView

        class MyFragment : BaseFragment() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?, 
                savedInstanceState: Bundle?
            ): View {
                return ComposeView(requireContext()).apply {
                    setContent {
                        defaultComponentContext(requireActivity().onBackPressedDispatcher) 
                    }
                }
            }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports defaultComponentContext inside setContent in Fragment with variables`() {
        val code = """
        import com.arkivanov.decompose.defaultComponentContext
        import androidx.compose.ui.platform.ComposeView

        class MyFragment : BaseFragment() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?, 
                savedInstanceState: Bundle?
            ): View {
                val view = ComposeView(requireContext())
                view.setContent {
                    defaultComponentContext(requireActivity().onBackPressedDispatcher)
                }
                return view
            }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `doesn't report defaultComponentContext inside setContent in Activity`() {
        val code = """
        import androidx.appcompat.app.AppCompatActivity
        import androidx.activity.compose.setContent
        import com.arkivanov.decompose.defaultComponentContext

        class MainActivity : AppCompatActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                val context = defaultComponentContext()
                setContent {
                    RootContent()
                }
            }
            
            @Composable
            fun RootContent() {
                
            }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report defaultComponentContext inside setContent Fragment`() {
        val code = """
        import com.arkivanov.decompose.defaultComponentContext

        class MyFragment : BaseFragment() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?, 
                savedInstanceState: Bundle?
            ): View {
                val context = defaultComponentContext(requireActivity().onBackPressedDispatcher)
                return ComposeView(requireContext()).apply {
                    setContent {
                    
                    }
                }
            }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `reports defaultComponentContext inside Composable function in Activity`() {
        val code = """
        import androidx.appcompat.app.AppCompatActivity
        import androidx.activity.compose.setContent
        import com.arkivanov.decompose.defaultComponentContext

        class MainActivity : AppCompatActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                setContent {
                    RootContent()
                }
            }
            
            @Composable
            fun RootContent() {
                val context = defaultComponentContext()
            }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports defaultComponentContext inside Composable function in Fragment`() {
        val code = """
        import com.arkivanov.decompose.defaultComponentContext
        import androidx.fragment.app.Fragment

        class MyFragment : Fragment() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?, 
                savedInstanceState: Bundle?
            ): View {
                return ComposeView(requireContext()).apply {
                    setContent {
                        RootContent()
                    }
                }
            }

            @Composable
            fun RootContent() {
                val context = defaultComponentContext(requireActivity().onBackPressedDispatcher)
            }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }
}
