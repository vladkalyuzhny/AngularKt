package app

import at.angular.common.http.HttpClientModule
import at.angular.core.NgModule
import at.angular.platformBrowser.animations.BrowserAnimationsModule

/**
 * A thin wrapper that reuses [AppModule] (its declarations, Material, CommonModule and RouterModule
 * directives) and adds the pieces a module bootstrap needs but a standalone one forbids:
 * BrowserAnimationsModule (the browser platform + animation engine) and HttpClient via
 * HttpClientModule. It imports [AppRoutingModule] for the root routes. Used by the module-based
 * bootstrap; a standalone `bootstrapApplication` supplies the same routes via the root @RoutingModule
 * directly, so this wrapper is not involved there.
 */
@NgModule(
    imports = [
        AppModule::class,
        BrowserAnimationsModule::class,
        AppRoutingModule::class,
        HttpClientModule::class,
    ],
    bootstrap = [AppComponent::class],
)
class BootstrapModule
