package at.angular.gradle

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import javax.inject.Inject

/**
 * The `angularKt { }` DSL in the consumer's build script. Grouped by build: knobs that only affect
 * the AOT build live under [aotConfig].
 *
 * ```
 * angularKt {
 *     aotConfig {
 *         bootstrapComponent = "app.AppComponent"
 *         styles.add("@angular/material/prebuilt-themes/azure-blue.css")
 *     }
 * }
 * ```
 */
abstract class AngularKtExtension @Inject constructor(objects: ObjectFactory) {
    /** AOT-only build config (added to the generated workspace's `angular.json`). */
    @get:Nested
    val aotConfig: AotConfig = objects.newInstance(AotConfig::class.java)

    fun aotConfig(action: Action<AotConfig>) = action.execute(aotConfig)
}

/**
 * AOT build knobs. Global styles/scripts are added to the AOT workspace's `angular.json`
 * `styles`/`scripts`. Entries may be project-relative paths or bare npm specifiers
 * (`@angular/material/prebuilt-themes/azure-blue.css`); the patcher normalizes the latter to a
 * `node_modules/…` path that Angular's `styles`/`scripts` resolution accepts.
 */
abstract class AotConfig {
    /**
     * FQN of the standalone root `@Component` the AOT build bootstraps via `bootstrapApplication`
     * (the generated `main.ts` names it). Unset → the AOT build does a classic module bootstrap of
     * the root `@NgModule` (the one with a `bootstrap` array). JIT names its root directly in the
     * hand-written entry, so this knob only feeds the AOT codegen.
     */
    abstract val bootstrapComponent: Property<String>

    /** Global stylesheets added to the AOT workspace's `angular.json` `styles`. */
    abstract val styles: ListProperty<String>

    /** Global scripts added to the AOT workspace's `angular.json` `scripts`. */
    abstract val scripts: ListProperty<String>
}
