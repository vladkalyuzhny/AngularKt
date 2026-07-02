@file:OptIn(ExperimentalJsExport::class)

package app.explore.pages

import app.explore.Feature
import app.explore.featureById
import at.angular.core.Component

/**
 * One routed component per Examples page. Each is intentionally tiny: it drops its live demo inside
 * the shared `<app-example>` frame (which renders the heading, description and TS-vs-Kotlin code
 * grid from the bound [Feature]) and is reached by its own `Route(path, component)` in
 * [app.AppRoutingModule] — real, declarative routing, one path per page (`/signal`, `/di`, …).
 *
 * `feature` is the catalog entry whose `id` matches the route path; the binding `[feature]="feature"`
 * hands it to the panel.
 */

@JsExport
@Component(
    selector = "app-di-page",
    template = "<app-example [feature]=\"feature\"><app-di></app-di></app-example>",
)
class DiPageComponent { val feature: Feature = featureById("di") }

@JsExport
@Component(
    selector = "app-koin-page",
    template = "<app-example [feature]=\"feature\"><app-koin></app-koin></app-example>",
)
class KoinPageComponent { val feature: Feature = featureById("koin") }

@JsExport
@Component(
    selector = "app-signal-page",
    template = "<app-example [feature]=\"feature\"><app-signal></app-signal></app-example>",
)
class SignalPageComponent { val feature: Feature = featureById("signal") }

@JsExport
@Component(
    selector = "app-forms-page",
    template = "<app-example [feature]=\"feature\"><app-form></app-form></app-example>",
)
class FormsPageComponent { val feature: Feature = featureById("forms") }

@JsExport
@Component(
    selector = "app-viewchild-page",
    template = "<app-example [feature]=\"feature\"><app-viewchild></app-viewchild></app-example>",
)
class ViewChildPageComponent { val feature: Feature = featureById("viewchild") }

@JsExport
@Component(
    selector = "app-lifecycle-page",
    template = "<app-example [feature]=\"feature\"><app-lifecycle></app-lifecycle></app-example>",
)
class LifecyclePageComponent { val feature: Feature = featureById("lifecycle") }

@JsExport
@Component(
    selector = "app-sanitizer-page",
    template = "<app-example [feature]=\"feature\"><app-sanitizer></app-sanitizer></app-example>",
)
class SanitizerPageComponent { val feature: Feature = featureById("sanitizer") }

@JsExport
@Component(
    selector = "app-http-page",
    template = "<app-example [feature]=\"feature\"><app-http></app-http></app-example>",
)
class HttpPageComponent { val feature: Feature = featureById("http") }

@JsExport
@Component(
    selector = "app-coroutines-page",
    template = "<app-example [feature]=\"feature\"><app-ticker></app-ticker></app-example>",
)
class CoroutinesPageComponent { val feature: Feature = featureById("coroutines") }

@JsExport
@Component(
    selector = "app-ktor-page",
    template = "<app-example [feature]=\"feature\"><app-ktor></app-ktor></app-example>",
)
class KtorPageComponent { val feature: Feature = featureById("ktor") }

@JsExport
@Component(
    selector = "app-pipe-page",
    template = """
        <app-example [feature]="feature">
            <p class="pipe-line"><code>'AngularKt' | exclaim</code> → {{ 'AngularKt' | exclaim }}</p>
        </app-example>
    """,
    styles = [".pipe-line code { background: var(--ak-inline-code-bg); padding: 1px 6px; border-radius: 5px; }"],
)
class PipePageComponent { val feature: Feature = featureById("pipe") }

@JsExport
@Component(
    selector = "app-directive-page",
    template = """
        <app-example [feature]="feature">
            <p appHighlight class="dir-demo">Hover me — the directive tints this element and toggles its "is-hot" class.</p>
        </app-example>
    """,
    styles = [
        ".dir-demo { display: inline-block; padding: 8px 12px; border-radius: 8px; color: var(--ak-text); transition: background-color .15s, box-shadow .15s; cursor: default; }",
        ".dir-demo.is-hot { box-shadow: 0 0 0 3px var(--ak-highlight-ring); }",
    ],
)
class DirectivePageComponent { val feature: Feature = featureById("directive") }

@JsExport
@Component(
    selector = "app-io-page",
    template = """
        <app-example [feature]="feature">
            <app-child [childName]="'AngularKt'" (notify)="onNotify(${"$"}event)"></app-child>
            <p class="reply" *ngIf="reply">Parent received: “{{reply}}”</p>
        </app-example>
    """,
    styles = [".reply { margin: 12px 0 0; }"],
)
class IoPageComponent {
    val feature: Feature = featureById("io")
    var reply = ""
    fun onNotify(message: String) { reply = message }
}

/**
 * The "Router" example. Its route (`/router`) owns nested children ([app.RouterChildRoutes]), so the
 * tabs below navigate real, deep URLs (`/router`, `/router/branch/leaf`, `/router/lazy`) and the
 * matching child renders in this page's own `<router-outlet>`.
 */
@JsExport
@Component(
    selector = "app-router-page",
    template = """
        <app-example [feature]="feature">
            <div class="router-play">
                <nav class="tabs">
                    <a routerLink="/router" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Root</a>
                    <a routerLink="/router/branch/leaf" routerLinkActive="active">Nested</a>
                    <a routerLink="/router/lazy" routerLinkActive="active">Lazy</a>
                </nav>
                <div class="outlet"><router-outlet></router-outlet></div>
            </div>
        </app-example>
    """,
    styles = [
        ".router-play .tabs { display: flex; align-items: center; gap: 2px; border-bottom: 1px solid var(--ak-border); margin-bottom: 14px; }",
        ".router-play .tabs a { padding: 9px 16px; margin-bottom: -1px; color: var(--ak-text-muted); text-decoration: none; font-size: 0.9rem; font-weight: 500; border-bottom: 2px solid transparent; transition: color .15s, border-color .15s; }",
        ".router-play .tabs a:hover { color: var(--ak-link); }",
        ".router-play .tabs a.active { color: var(--ak-active-fg); border-bottom-color: var(--ak-active-fg); }",
        ".router-play .outlet { padding: 16px; border: 1px dashed var(--ak-border); border-radius: 10px; background: var(--ak-surface-2); }",
    ],
)
class RouterPageComponent { val feature: Feature = featureById("router") }
