@file:OptIn(ExperimentalJsExport::class)

package app.explore

/**
 * One entry in the explorer's left nav: a short description plus a TypeScript-vs-Kotlin
 * code comparison. `ts`/`kt` hold pre-highlighted HTML (see [highlightTs]/[highlightKotlin])
 * bound with `[innerHTML]`; `@JsExport` keeps the property names un-mangled so the template
 * can read `f.label`, `current.ts`, etc. `id` is what the template switches the live demo on
 * (`*ngIf="current.id === '…'"`). The two "Get started" sections leave `ts`/`kt` empty and
 * render bespoke setup panels instead.
 */
@JsExport
class Feature(
    val id: String,
    val label: String,
    val icon: String,
    val desc: String,
    val ts: String,
    val kt: String,
)

/** The "Get started" group — Setup plus the two run modes, shown above Examples. */
internal fun buildGetStarted(): Array<Feature> = arrayOf(
    Feature(
        "setup", "Setup", "rocket_launch",
        "Two things wire AngularKt into a Gradle project: the plugin (it brings the Angular " +
            "toolchain and the @angular/* packages) and the runtime dependency. Then you write " +
            "components in Kotlin.",
        "", "",
    ),
    Feature(
        "jit", "JIT mode", "bolt",
        "Just-in-time: Angular compiles templates in the browser. The fastest inner loop — " +
            "instant rebuilds and hot reload. This is the default; any non-aot task runs JIT.",
        "", "",
    ),
    Feature(
        "aot", "AOT mode", "precision_manufacturing",
        "Ahead-of-time: templates are compiled to JavaScript at build time, with full template " +
            "type-checking and a smaller, faster bundle. Selected by running an aot* task.",
        "", "",
    ),
    Feature(
        "customize", "Customize", "palette",
        "Wire your own global stylesheets and scripts into the build — a custom theme, a CSS " +
            "library, an analytics snippet. Declare them once for AOT in the angularKt block and " +
            "mirror them for JIT in webpack.config.d.",
        "", "",
    ),
)

/** Every library capability, in the order they appear under "Examples" in the left nav. */
internal fun buildExamples(): Array<Feature> = arrayOf(
    Feature(
        "di", "Services & DI", "account_tree",
        "Services are plain classes annotated @Injectable(providedIn = root). Angular builds " +
            "and injects them through the constructor — same DI graph, Kotlin syntax. An " +
            "@Optional dependency that is never provided simply arrives as null.",
        highlightTs(DI_TS), highlightKotlin(DI_KT),
    ),
    Feature(
        "router", "Router", "alt_route",
        "Routes are declared with @RoutingModule, compiled to provideRouter(routes). Nested routes " +
            "point at another @RoutingModule class the processor inlines recursively, each level " +
            "rendered through a shared nested-outlet shell. " +
            "The tabs below navigate the root and routes one, two, and three levels deep.",
        highlightTs(ROUTER_TS), highlightKotlin(ROUTER_KT),
    ),
    Feature(
        "signal", "Signals", "exposure",
        "State is a Kotlin MutableStateFlow, surfaced to the template as a real Angular " +
            "signal via asSignal(). The view reads count() and repaints through signal-aware " +
            "change detection.",
        highlightTs(SIGNAL_TS), highlightKotlin(SIGNAL_KT),
    ),
    Feature(
        "forms", "Reactive forms", "keyboard",
        "A reactive FormControl's valueChanges Observable is consumed as a Kotlin Flow, so the " +
            "greeting and character count update on every keystroke.",
        highlightTs(FORMS_TS), highlightKotlin(FORMS_KT),
    ),
    Feature(
        "viewchild", "ViewChild", "center_focus_strong",
        "@ViewChild grabs a template reference (#box) as an ElementRef, so you can read live " +
            "DOM state on demand.",
        highlightTs(VIEWCHILD_TS), highlightKotlin(VIEWCHILD_KT),
    ),
    Feature(
        "io", "Inputs / Outputs", "swap_horiz",
        "@Input and @Output behave exactly like Angular TS — aliasing included. The child " +
            "receives childName and emits back up through a typed EventEmitter.",
        highlightTs(IO_TS), highlightKotlin(IO_KT),
    ),
    Feature(
        "lifecycle", "Lifecycle & CD", "sync",
        "Three @Component knobs at once: changeDetection = OnPush checks the child only when an " +
            "@Input reference changes; encapsulation = None makes its styles global; and OnChanges' " +
            "ngOnChanges fires on every @Input change — the declarative alternative to a property " +
            "setter. Click the button to change the input and watch the OnPush child react.",
        highlightTs(LIFECYCLE_TS), highlightKotlin(LIFECYCLE_KT),
    ),
    Feature(
        "directive", "Directives", "auto_fix_high",
        "Attribute directives support @HostBinding and @HostListener. This one injects " +
            "ElementRef and toggles a host class on hover — move your mouse over the text below.",
        highlightTs(DIRECTIVE_TS), highlightKotlin(DIRECTIVE_KT),
    ),
    Feature(
        "pipe", "Pipes", "filter_alt",
        "A @Pipe is a class with a transform function. Use it in any template with the | " +
            "operator, exactly like Angular.",
        highlightTs(PIPE_TS), highlightKotlin(PIPE_KT),
    ),
    Feature(
        "sanitizer", "Sanitizer", "shield",
        "Inline SVG bound through [innerHTML] is stripped by Angular's sanitizer. Inject " +
            "DomSanitizer and wrap trusted markup with bypassSecurityTrustHtml so glyphs render — " +
            "below, the raw binding stays blank while the trusted one shows the shield.",
        highlightTs(SANITIZER_TS), highlightKotlin(SANITIZER_KT),
    ),
    Feature(
        "http", "Angular HttpClient", "lightbulb",
        "Prefer Angular's own HttpClient? It is opt-in. The returned Observable is awaited as a " +
            "coroutine, so the call site stays suspend-flavoured.",
        highlightTs(HTTP_TS), highlightKotlin(HTTP_KT),
    ),
    Feature(
        "coroutines", "Coroutines", "timer",
        "No RxJS. A coroutine scoped to the component lifecycle ticks every 100 ms and cancels " +
            "itself on destroy via Angular's DestroyRef — there is no ngOnDestroy.",
        highlightTs(COROUTINES_TS), highlightKotlin(COROUTINES_KT),
    ),
    Feature(
        "ktor", "Ktor HTTP", "cloud_download",
        "Reach the network with Ktor and kotlinx.serialization — a plain suspend function " +
            "instead of an HttpClient that hands back an Observable.",
        highlightTs(KTOR_TS), highlightKotlin(KTOR_KT),
    ),
)
