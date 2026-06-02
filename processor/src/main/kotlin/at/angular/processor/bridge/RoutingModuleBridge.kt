package at.angular.processor.bridge

import at.angular.processor.bridge.references.NgReferences
import at.angular.processor.bridge.references.TsExpression
import at.angular.processor.bridge.references.TsRefResolver
import at.angular.processor.bridge.references.renderEntries
import at.angular.processor.models.RouteEntry
import at.angular.processor.models.RoutingModuleModel

/**
 * The root routing providers (`provideRouter(routes)`), or a lazy feature module's
 * `RouterModule.forChild(routes)`. The template spells out `@NgModule`, the router symbol and the
 * `Routes` type itself, so the bridge only supplies the route rows and a name — and names the
 * framework symbols up front so their imports are emitted.
 */
class RoutingModuleBridge(
    private val model: RoutingModuleModel,
    private val resolver: TsRefResolver,
) : Bridge {
    override fun createView(): BridgeView {
        val rows = model.routes.map(::routeFields)
        val symbols = buildList {
            // A standalone-lazy bundle is just `export const Name: Routes = [...]` — no @NgModule/router symbol.
            if (!model.lazy) {
                add(NgReferences.NgModule)
                add(if (model.forChild) NgReferences.RouterModule else NgReferences.provideRouter)
                if (!model.forChild) {
                    if (model.useHash) add(NgReferences.withHashLocation)
                    if (model.preloadAllModules) {
                        add(NgReferences.withPreloading)
                        add(NgReferences.PreloadAllModules)
                    }
                }
            }
            add(NgReferences.Routes)
            rows.forEach { row -> row.forEach { addAll(it.second.refs()) } }
        }
        return BridgeView.RoutingModule(
            imports = importsFrom(symbols),
            name = model.simpleName,
            routes = rows.map { Route(renderEntries(it)) },
            forChild = model.forChild,
            lazy = model.lazy,
            useHash = model.useHash,
            preloadAllModules = model.preloadAllModules,
        )
    }

    /**
     * `path: '…', component: X, redirectTo: '…', pathMatch: '…', children: [ … ]` — only the
     * present fields. Recurses into [RouteEntry.children]; the nested `Object`/`Array` carry their
     * own symbol refs, so child component imports are collected by the same `refs()` walk.
     */
    private fun routeFields(route: RouteEntry): List<Pair<String, TsExpression>> = buildList {
        add("path" to TsExpression.String(route.path))
        route.componentFqn?.let { add("component" to TsExpression.Symbol(resolver.local(it))) }
        route.redirectTo?.let { add("redirectTo" to TsExpression.String(it)) }
        route.pathMatch?.let { add("pathMatch" to TsExpression.String(it)) }
        route.loadChildren?.let {
            add("loadChildren" to TsExpression.LazyImport(it.fqn.substringAfterLast('.')))
        }
        if (route.children.isNotEmpty()) {
            val rows = route.children.map { TsExpression.Object(routeFields(it)) }
            add("children" to TsExpression.Array(rows))
        }
    }
}
