package at.angular.processor.utils

import at.angular.processor.models.LazyRoutesTarget
import at.angular.processor.models.RouteEntry
import at.angular.router.NoComponent
import at.angular.router.RoutingModule
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

internal fun KSAnnotated.annotation(fqn: String?): KSAnnotation =
    requireNotNull(annotationOrNull(fqn)) {
        "annotation $fqn not present on $this, guard with hasAnnotation() or use annotationOrNull()"
    }

internal fun KSAnnotated.annotationOrNull(fqn: String?): KSAnnotation? =
    if (fqn == null) null else annotations.firstOrNull { it.matches(fqn) }

internal fun KSAnnotated.hasAnnotation(fqn: String?): Boolean =
    fqn != null && annotations.any { it.matches(fqn) }

private fun KSAnnotation.matches(fqn: String): Boolean =
    annotationType.resolve().declaration.qualifiedName?.asString() == fqn

internal fun KSAnnotation.stringArg(name: String): String? =
    (argValue(name) as? String)?.ifEmpty { null }

internal fun KSAnnotation.stringArgOrEmpty(name: String): String =
    (argValue(name) as? String).orEmpty()

internal fun KSAnnotation.boolArg(name: String): Boolean? = argValue(name) as? Boolean

internal fun KSAnnotation.stringArrayArg(name: String): List<String> =
    (argValue(name) as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

internal fun KSAnnotation.classArrayArg(name: String): List<String> =
    (argValue(name) as? List<*>)
        ?.mapNotNull { (it as? KSType)?.declaration?.qualifiedName?.asString() } ?: emptyList()

internal fun KSAnnotation.classArg(name: String): String? =
    (argValue(name) as? KSType)?.declaration?.qualifiedName?.asString()

/**
 * The simple name of an enum-entry argument (e.g. `ViewEncapsulation.None` → "None"). KSP may hand
 * the value back as the entry's [KSClassDeclaration] or as a `KSType` of it; both resolve through
 * `simpleName`, with a `toString()` fallback (`"None"`) for any other shape.
 */
internal fun KSAnnotation.enumArg(name: String): String? = when (val v = argValue(name)) {
    is KSClassDeclaration -> v.simpleName.asString()
    is KSType -> v.declaration.simpleName.asString()
    else -> v?.toString()?.substringAfterLast('.')?.ifEmpty { null }
}

/** The class-typed argument [name] as its declaration, for reading annotations off it. */
internal fun KSAnnotation.classDeclarationArg(name: String): KSClassDeclaration? =
    (argValue(name) as? KSType)?.declaration as? KSClassDeclaration

internal fun KSAnnotation.annotationArrayArg(name: String): List<KSAnnotation> =
    (argValue(name) as? List<*>)?.filterIsInstance<KSAnnotation>() ?: emptyList()

private fun KSAnnotation.argValue(name: String): Any? =
    arguments.firstOrNull { it.name?.asString() == name }?.value

/**
 * The `routes` array of a `@RoutingModule`. A route's `children` points at another
 * `@RoutingModule`-annotated class, whose own routes are read recursively; [visited] breaks an
 * accidental cycle (a child pointing back at an ancestor class) so codegen can't loop forever.
 */
internal fun KSAnnotation.readRoutes(visited: Set<String> = emptySet()): List<RouteEntry> =
    annotationArrayArg("routes").map { route ->
        val loadChildren = route.classDeclarationArg("loadChildren")
            ?.takeIf { it.qualifiedName?.asString() != NoComponent::class.qualifiedName }
        RouteEntry(
            path = route.stringArgOrEmpty("path"),
            componentFqn = route.classArg("component")
                ?.takeIf { it != NoComponent::class.qualifiedName },
            redirectTo = route.stringArg("redirectTo"),
            pathMatch = route.stringArg("pathMatch"),
            children = route.readChildren(visited),
            loadChildren = loadChildren?.qualifiedName?.asString()?.let { fqn ->
                // A `@RoutingModule` target = standalone-lazy Routes bundle; an `@NgModule` target = classic lazy.
                LazyRoutesTarget(fqn, isRoutesBundle =
                    loadChildren.hasAnnotation(RoutingModule::class.qualifiedName))
            },
        )
    }

private fun KSAnnotation.readChildren(visited: Set<String>): List<RouteEntry> {
    val childClass = classDeclarationArg("children")
        ?.takeIf { it.qualifiedName?.asString() != NoComponent::class.qualifiedName }
        ?: return emptyList()
    val fqn = childClass.qualifiedName?.asString() ?: return emptyList()
    if (fqn in visited) return emptyList()

    return childClass.annotationOrNull(RoutingModule::class.qualifiedName)
        ?.readRoutes(visited + fqn)
        ?: emptyList()
}

/**
 * FQNs this `@RoutingModule`'s routes inline as a nested `children` bundle. A `@RoutingModule` named
 * here is a *child* — inlined into its parent and never emitting its own output.
 */
internal fun KSAnnotation.childrenFqns(): Set<String> =
    annotationArrayArg("routes").mapNotNullTo(mutableSetOf()) { route ->
        route.classArg("children")?.takeIf { it != NoComponent::class.qualifiedName }
    }

/**
 * FQNs this `@RoutingModule`'s routes lazy-load via `loadChildren` (an `@NgModule`, or a
 * standalone-lazy `@RoutingModule` Routes bundle). The latter still emits its own output.
 */
internal fun KSAnnotation.loadChildrenFqns(): Set<String> =
    annotationArrayArg("routes").mapNotNullTo(mutableSetOf()) { route ->
        route.classArg("loadChildren")?.takeIf { it != NoComponent::class.qualifiedName }
    }