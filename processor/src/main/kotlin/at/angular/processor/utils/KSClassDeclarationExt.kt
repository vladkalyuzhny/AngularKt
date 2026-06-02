package at.angular.processor.utils

import at.angular.core.ContentChild
import at.angular.core.ContentChildren
import at.angular.core.Host
import at.angular.core.HostBinding
import at.angular.core.HostListener
import at.angular.core.Inject
import at.angular.core.Optional
import at.angular.core.Self
import at.angular.core.SkipSelf
import at.angular.core.ViewChild
import at.angular.core.ViewChildren
import at.angular.processor.models.PropBindingModel
import at.angular.processor.models.CtorParam
import at.angular.processor.models.HostBindingModel
import at.angular.processor.models.HostListenerModel
import at.angular.processor.models.QueryKind
import at.angular.processor.models.QueryModel
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2

internal fun Resolver.classesWith(annotationFqn: String?): List<KSClassDeclaration> {
    if (annotationFqn == null) return emptyList()

    return getSymbolsWithAnnotation(annotationFqn)
        .filterIsInstance<KSClassDeclaration>()
        .toList()
}

internal fun KSClassDeclaration.fqn(): String = requireNotNull(qualifiedName).asString()

internal fun KSClassDeclaration.resolveTemplate(template: String?, templateUrl: String?): String? {
    if (templateUrl == null) return template
    if (template != null) error("AngularKt: ${fqn()} sets both `template` and `templateUrl` - use one")

    return readRelativeResource(templateUrl)
}

internal fun KSClassDeclaration.resolveStyles(styles: List<String>, styleUrls: List<String>): List<String> {
    if (styleUrls.isEmpty()) return styles

    return styles + styleUrls.map { readRelativeResource(it) }
}

private fun KSClassDeclaration.readRelativeResource(url: String): String {
    val srcPath = containingFile?.filePath
        ?: error("AngularKt: ${fqn()} has no source file to resolve '$url' against")
    val file = File(File(srcPath).parentFile, url)
    if (!file.isFile) error("AngularKt: ${fqn()} references '$url', but no file exists at ${file.path}")

    return file.readText()
}

internal fun KSClassDeclaration.ctorParams(): List<CtorParam> =
    primaryConstructor?.parameters.orEmpty().map { parameter ->
        val typeFqn = parameter.type.resolve().declaration.qualifiedName?.asString()
            ?: error("AngularKt: cannot resolve constructor parameter type on ${fqn()}")
        CtorParam(
            typeFqn = typeFqn,
            injectTokenFqn = parameter.annotationOrNull(Inject::class.qualifiedName)
                ?.classArg("token"),
            optional = parameter.hasAnnotation(Optional::class.qualifiedName),
            self = parameter.hasAnnotation(Self::class.qualifiedName),
            skipSelf = parameter.hasAnnotation(SkipSelf::class.qualifiedName),
            host = parameter.hasAnnotation(Host::class.qualifiedName),
        )
    }

internal fun KSClassDeclaration.bindings(annotationFqn: String?): List<PropBindingModel> =
    getDeclaredProperties().mapNotNull { property ->
        val annotation = property.annotationOrNull(annotationFqn)
            ?: return@mapNotNull null
        PropBindingModel(
            property = property.simpleName.asString(),
            alias = annotation.stringArg("alias")
        )
    }.toList()

internal fun KSClassDeclaration.hostBindings(): List<HostBindingModel> =
    getDeclaredProperties().mapNotNull { property ->
        val annotation = property.annotationOrNull(HostBinding::class.qualifiedName)
            ?: return@mapNotNull null
        HostBindingModel(
            property = property.simpleName.asString(),
            hostProperty = annotation.stringArg("hostPropertyName")
        )
    }.toList()

internal fun KSClassDeclaration.hostListeners(): List<HostListenerModel> =
    getDeclaredFunctions().mapNotNull { function ->
        val annotation = function.annotationOrNull(HostListener::class.qualifiedName)
            ?: return@mapNotNull null
        HostListenerModel(
            method = function.simpleName.asString(),
            event = annotation.stringArgOrEmpty("eventName"),
            args = annotation.stringArrayArg("args")
        )
    }.toList()

private val QUERY_KINDS = linkedMapOf(
    ViewChild::class.qualifiedName to QueryKind.VIEW_CHILD,
    ViewChildren::class.qualifiedName to QueryKind.VIEW_CHILDREN,
    ContentChild::class.qualifiedName to QueryKind.CONTENT_CHILD,
    ContentChildren::class.qualifiedName to QueryKind.CONTENT_CHILDREN,
)

internal fun KSClassDeclaration.queries(): List<QueryModel> =
    getDeclaredProperties().mapNotNull { property ->
        QUERY_KINDS.entries.firstNotNullOfOrNull { (annotationFqn, kind) ->
            property.annotationOrNull(annotationFqn)?.let { annotation ->
                QueryModel(
                    property = property.simpleName.asString(),
                    kind = kind,
                    selector = annotation.stringArgOrEmpty("selector"),
                    static = annotation.boolArg("static") ?: false,
                )
            }
        }
    }.toList()
