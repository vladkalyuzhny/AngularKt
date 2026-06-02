package at.angular.processor.models

import com.google.devtools.ksp.symbol.KSFile

/** `@Component` or `@Directive` (a directive is a component without a view). */
data class ComponentModel(
    override val fqn: String,
    override val simpleName: String,
    override val containingFile: KSFile?,
    override val ctorParams: List<CtorParam>,
    val isComponent: Boolean,
    val selector: String?,
    val template: String?,
    val templateUrl: String?,
    val styles: List<String>,
    val styleUrls: List<String>,
    val inputs: List<PropBindingModel>,
    val outputs: List<PropBindingModel>,
    val standalone: Boolean,
    /** Standalone components only: the declarables/modules they pull in directly. */
    val imports: List<String>,
    /** `ViewEncapsulation` entry name (e.g. "None"), or null when the default (Emulated) is in effect. */
    val encapsulation: String?,
    /** `ChangeDetectionStrategy` entry name (e.g. "OnPush"), or null when the default (Default) is in effect. */
    val changeDetection: String?,
    val hostBindings: List<HostBindingModel>,
    val hostListeners: List<HostListenerModel>,
    val queries: List<QueryModel>,
) : NgDeclaration