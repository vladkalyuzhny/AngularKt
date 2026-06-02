package app

import app.examples.coroutines.TickerComponent
import app.examples.di.DiDemoComponent
import app.examples.directive.HighlightDirective
import app.examples.forms.FormComponent
import app.examples.http.HttpComponent
import app.examples.io.ChildComponent
import app.examples.ktor.KtorComponent
import app.examples.lifecycle.LifecycleComponent
import app.examples.lifecycle.OnPushComponent
import app.examples.pipe.ExclaimPipe
import app.examples.sanitizer.SanitizerComponent
import app.examples.signal.SignalComponent
import app.examples.standalone.BadgeComponent
import app.examples.viewchild.ViewChildDemoComponent
import app.explore.ExploreLibraryComponent
import app.routing.branch.BranchComponent
import app.routing.leaf.LeafComponent
import app.routing.tree.TreeComponent
import at.angular.common.CommonModule
import at.angular.core.NgModule
import at.angular.forms.ReactiveFormsModule
import at.angular.material.MatButtonModule
import at.angular.material.MatCardModule
import at.angular.material.MatDividerModule
import at.angular.material.MatIconModule
import at.angular.material.MatListModule
import at.angular.material.MatSidenavModule
import at.angular.material.MatTabsModule
import at.angular.material.MatToolbarModule
import at.angular.router.RouterModule

/**
 * `@NgModule` with metadata on the annotation, exactly like Angular TS.
 * No companion-object holder; the processor reads these arrays and generates the
 * runtime `NgModule(...)` registration.
 */
@NgModule(
    declarations = [
        AppComponent::class, ChildComponent::class, KtorComponent::class,
        HttpComponent::class, TickerComponent::class, TreeComponent::class,
        FormComponent::class, HighlightDirective::class,
        SignalComponent::class, ExclaimPipe::class, ExploreLibraryComponent::class,
        DiDemoComponent::class, ViewChildDemoComponent::class,
        BranchComponent::class, LeafComponent::class,
        LifecycleComponent::class, OnPushComponent::class,
        SanitizerComponent::class
    ],
    imports = [
        // CommonModule supplies *ngIf/*ngFor to the declared components — under a standalone
        // bootstrap these no longer arrive via BrowserModule. Animations are provided
        // functionally (provideAnimations) instead of importing BrowserAnimationsModule.
        // Plain RouterModule gives the router-outlet/routerLink directives; the routes themselves
        // are supplied by the root @RoutingModule (see app.routing.kt).
        CommonModule::class, ReactiveFormsModule::class, RouterModule::class,
        // BadgeComponent is standalone — an NgModule imports it, never declares it.
        BadgeComponent::class,
        // Angular Material — used by AppComponent's toolbar + card shell, and the
        // explorer's responsive navigation drawer (MatSidenavModule).
        MatToolbarModule::class, MatCardModule::class, MatButtonModule::class,
        MatIconModule::class, MatListModule::class, MatDividerModule::class,
        MatTabsModule::class, MatSidenavModule::class
    ],
    // Exported so the standalone root component (bootstrapApplication) can render <app-root>.
    exports = [AppComponent::class],
)
class AppModule
