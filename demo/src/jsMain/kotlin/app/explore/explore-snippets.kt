package app.explore

/**
 * Pure data for the explorer: the TypeScript-vs-Kotlin code samples shown in each
 * Examples panel, plus the "Get started" setup snippets. Everything here is an
 * illustrative string (deliberately tiny — not the demo's own source); the catalog
 * ([buildExamples]) highlights them with [highlightKotlin]/[highlightTs] and the component
 * binds the result with `[innerHTML]`.
 */

/**
 * A literal `"""` for embedding a Kotlin raw-string template *inside* these (also
 * triple-quoted) snippet constants — writing `"""` directly would close the string.
 */
private const val TQ = "\"\"\""

// ---- "Get started" setup snippets (highlighted on demand) ----

internal fun setupGradleHtml(): String = highlightKotlin(SETUP_GRADLE)
internal fun setupComponentHtml(): String = highlightKotlin(SETUP_COMPONENT)
internal fun jitEntryHtml(): String = highlightKotlin(JIT_ENTRY)
internal fun jitWebpackHtml(): String = highlightTs(JIT_WEBPACK)
internal fun aotEntryHtml(): String = highlightKotlin(AOT_ENTRY)
internal fun customizeAotHtml(): String = highlightKotlin(CUSTOMIZE_AOT)
internal fun customizeJitHtml(): String = highlightTs(CUSTOMIZE_JIT)

private val SETUP_GRADLE = """
// build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("io.github.vladkalyuzhny.angularkt")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets.jsMain.dependencies {
        implementation("io.github.vladkalyuzhny:angularkt:0.1.0")
    }
}

// gradle.properties — target Angular major (floor 16, default 22)
// angularKt.angularVersion=22
""".trim()

private val SETUP_COMPONENT = """
// AppComponent.kt
@JsExport
@Component(
    selector = "app-root",
    template = "<h1>Hello, {{name}}!</h1>",
)
class AppComponent {
    var name = "AngularKt"
}

@NgModule(
    declarations = [AppComponent::class],
    imports = [BrowserModule::class],
    bootstrap = [AppComponent::class],
)
class AppModule
""".trim()

private val JIT_ENTRY = """
// src/jsJit/kotlin/main.kt — classic platformBrowserDynamic bootstrap, the textbook JIT path
fun main() {
    registerAngularKt()  // KSP-generated: applies your @Component/@NgModule decorators
    // root @RoutingModule → RouterModule.forRoot(routes); BootstrapModule just imports AppRoutingModule
    platformBrowserDynamic(undefined).bootstrapModule(BootstrapModule::class.js)
}
""".trim()

private val JIT_WEBPACK = """
// webpack.config.d/angular-jit.js — REQUIRED for JIT
// zone.js + @angular/compiler must evaluate before any @angular module: Angular
// libraries ship in partial-Ivy format and JIT-link at class-definition time, so
// they throw if the compiler isn't loaded yet. main()'s body runs too late (its
// ES imports evaluate first), so prepend them as their own webpack entries.
if (config.entry && Array.isArray(config.entry.main)) {
    config.entry.main = ['zone.js', '@angular/compiler', ...config.entry.main];
}
""".trim()

private val AOT_ENTRY = """
// src/jsAot/kotlin/main.kt — standalone bootstrapApplication; the generated main.ts is a thin shim that calls it
@JsExport
fun main(root: JsClass<*>, providers: Array<EnvironmentProviders>) {
    // your own (non-Angular) startup code here — runs in AOT
    bootstrapApplication(root, jsObject { this.providers = appProviders() + providers })
}
""".trim()

private val CUSTOMIZE_AOT = """
// build.gradle.kts — global assets for the AOT build
angularKt {
    aotConfig {
        // a stylesheet: an npm specifier or a project-relative path
        styles.add("@angular/material/prebuilt-themes/indigo-pink.css")
        styles.add("src/jsMain/resources/custom-theme.css")

        // a global <script>, injected before the app boots
        scripts.add("node_modules/chart.js/dist/chart.umd.js")
    }
}
""".trim()

private val CUSTOMIZE_JIT = """
// webpack.config.d/angular-jit.js — the same assets for JIT
if (config.entry && Array.isArray(config.entry.main)) {
    // 'style' lets webpack resolve a package's CSS export (Angular Material, …)
    config.resolve = config.resolve || {};
    config.resolve.conditionNames = ['style', '...'];

    // style-loader + css-loader inject these at runtime
    config.entry.main.push('@angular/material/prebuilt-themes/indigo-pink.css');
    config.entry.main.push('./src/jsMain/resources/custom-theme.css');
}
""".trim()

// ---- Example code samples (TypeScript / Angular on the left, Kotlin / AngularKt on the right) ----

internal val SIGNAL_TS = """
@Component({
  selector: 'app-counter',
  template: `<button (click)="dec()">−</button>
             <span>{{ count() }}</span>
             <button (click)="inc()">+</button>`,
})
export class CounterComponent {
  count = signal(0);
  inc() { this.count.update(n => n + 1); }
  dec() { this.count.update(n => n - 1); }
}
""".trim()

internal val SIGNAL_KT = """
@JsExport
@Component(
    selector = "app-counter",
    template = $TQ
        <button (click)='dec()'>−</button>
        <span>{{count()}}</span>
        <button (click)='inc()'>+</button>
    $TQ,
)
class CounterComponent {
    private val lifecycle = LifecycleScope()
    private val state = MutableStateFlow(0)

    // a Kotlin StateFlow exposed as a real Angular signal
    val count = state.asSignal(lifecycle)

    fun inc() { state.value += 1 }
    fun dec() { state.value -= 1 }
}
""".trim()

internal val IO_TS = """
@Component({
  selector: 'app-child',
  template: `<button (click)="ping()">Wave back 👋</button>`,
})
export class ChildComponent {
  @Input('childName') name = '';
  @Output() notify = new EventEmitter<string>();

  ping() { this.notify.emit('Nice to meet you!'); }
}
""".trim()

internal val IO_KT = """
@JsExport
@Component(
    selector = "app-child",
    template = "<button (click)='ping()'>Wave back 👋</button>",
)
class ChildComponent {
    @Input(alias = "childName")
    var name: String = ""

    @Output
    val notify = EventEmitter<String>()

    fun ping() { notify.emit("Nice to meet you!") }
}
""".trim()

internal val DI_TS = """
@Injectable({ providedIn: 'root' })
export class GreetingService {
  greet() { return 'Welcome back!'; }
}

@Component({ selector: 'app-di', template: `{{ message }}` })
export class DiComponent {
  message: string;
  constructor(greeting: GreetingService) {
    this.message = greeting.greet();
  }
}
""".trim()

internal val DI_KT = """
@JsExport
@Injectable(providedIn = "root")
class GreetingService {
    fun greet() = "Welcome back!"
}

@JsExport
@Component(selector = "app-di", template = "{{message}}")
class DiComponent(greeting: GreetingService) {
    val message = greeting.greet()
}
""".trim()

internal val KOIN_TS = """
// Angular has no Koin — this is the TS shape Koin's Kotlin DSL replaces:
@Injectable({ providedIn: 'root' })
export class QuoteService {
  next() { return '…'; }
}

@Component({ selector: 'app-koin', template: `{{ quote }}` })
export class KoinComponent {
  quote: string;
  constructor(private quotes: QuoteService) {
    this.quote = quotes.next();
  }
}
""".trim()

internal val KOIN_KT = """
// a plain class — no @Injectable, framework-agnostic
class QuoteService {
    fun next() = "…"
}

// register it in a Koin module, started once at bootstrap
val appKoinModule = module {
    single { QuoteService() }
}

@JsExport
@Component(selector = "app-koin", template = "{{quote}}")
class KoinDemoComponent : KoinComponent {
    private val quotes: QuoteService by inject()
    var quote = quotes.next()
}
""".trim()

internal val SANITIZER_TS = """
class IconComponent {
  safe: SafeHtml;
  constructor(s: DomSanitizer) {
    // trusted markup survives [innerHTML] sanitization
    this.safe = s.bypassSecurityTrustHtml(svg);
  }
}
""".trim()

internal val SANITIZER_KT = """
class IconComponent(s: DomSanitizer) {
    // trusted markup survives [innerHTML] sanitization
    val safe: SafeHtml =
        s.bypassSecurityTrustHtml(svg)
}
""".trim()

internal val PIPE_TS = """
@Pipe({ name: 'exclaim' })
export class ExclaimPipe implements PipeTransform {
  transform(value: string): string {
    return value + '!!!';
  }
}
// usage: {{ 'Kotlin' | exclaim }}
""".trim()

internal val PIPE_KT = """
@JsExport
@Pipe(name = "exclaim")
class ExclaimPipe {
    fun transform(value: String): String = value + "!!!"
}
// usage: {{ 'Kotlin' | exclaim }}
""".trim()

internal val DIRECTIVE_TS = """
@Directive({ selector: '[appHighlight]' })
export class HighlightDirective {
  @HostBinding('class.is-hot') hot = false;
  constructor(private el: ElementRef) {}

  @HostListener('mouseenter') onEnter() { this.hot = true; }
  @HostListener('mouseleave') onLeave() { this.hot = false; }
}
""".trim()

internal val DIRECTIVE_KT = """
@JsExport
@Directive(selector = "[appHighlight]")
class HighlightDirective(private val el: ElementRef) {
    @HostBinding("class.is-hot")
    var hot = false

    @HostListener("mouseenter") fun onEnter() { hot = true }
    @HostListener("mouseleave") fun onLeave() { hot = false }
}
""".trim()

internal val VIEWCHILD_TS = """
@Component({
  selector: 'app-reader',
  template: `<input #box value="Try me">
             <button (click)="read()">Read</button>
             <p>{{ value }}</p>`,
})
export class ReaderComponent {
  @ViewChild('box') box!: ElementRef<HTMLInputElement>;
  value = '';
  read() { this.value = this.box.nativeElement.value; }
}
""".trim()

internal val VIEWCHILD_KT = """
@JsExport
@Component(
    selector = "app-reader",
    template = $TQ
        <input #box value='Try me'>
        <button (click)='read()'>Read</button>
        <p>{{value}}</p>
    $TQ,
)
class ReaderComponent {
    @ViewChild("box")
    var box: ElementRef? = null

    var value = ""
    fun read() { value = box?.nativeElement?.value as? String ?: "" }
}
""".trim()

internal val COROUTINES_TS = """
@Component({ selector: 'app-clock', template: `{{ elapsed }}` })
export class ClockComponent implements OnInit, OnDestroy {
  elapsed = 0;
  private sub?: Subscription;

  ngOnInit() {
    this.sub = interval(100).subscribe(() => this.elapsed++);
  }
  ngOnDestroy() { this.sub?.unsubscribe(); }
}
""".trim()

internal val COROUTINES_KT = """
@JsExport
@Component(selector = "app-clock", template = "{{elapsed}}")
class ClockComponent : OnInit {
    var elapsed = 0

    // scoped to the component — auto-cancels on destroy, no ngOnDestroy
    private val scope = LifecycleScope()

    override fun ngOnInit() {
        scope.launch {
            while (true) { delay(100); elapsed++ }
        }
    }
}
""".trim()

internal val FORMS_TS = """
@Component({
  selector: 'app-form',
  template: `<input [formControl]="name">
             <p>{{ message }}</p>`,
})
export class FormComponent implements OnInit {
  name = new FormControl('');
  message = '';

  ngOnInit() {
    this.name.valueChanges.subscribe(v => {
      this.message = v ? 'Hello, ' + v + '!' : '';
    });
  }
}
""".trim()

internal val FORMS_KT = """
@JsExport
@Component(
    selector = "app-form",
    template = "<input [formControl]='name'><p>{{message}}</p>",
)
class FormComponent : OnInit {
    val name = FormControl("")
    var message = ""
    private val scope = LifecycleScope()

    override fun ngOnInit() {
        scope.launch {
            // valueChanges (an RxJS Observable) consumed as a Kotlin Flow
            name.valueChanges.asFlow().collect { v ->
                message = if (v.isNullOrBlank()) "" else "Hello, " + v + "!"
            }
        }
    }
}
""".trim()

internal val KTOR_TS = """
@Injectable({ providedIn: 'root' })
export class TodoService {
  constructor(private http: HttpClient) {}

  load(): Observable<Todo[]> {
    return this.http.get<Todo[]>('/todos');
  }
}
""".trim()

internal val KTOR_KT = """
@JsExport
@Injectable(providedIn = "root")
class TodoService {
    private val client = HttpClient(Js)

    // a plain suspend function + kotlinx.serialization — no Observable.
    // Note: @JsExport can't export a suspend fun
    internal suspend fun load(): List<Todo> =
        client.get("/todos").body()
}
""".trim()

internal val HTTP_TS = """
@Component({ selector: 'app-tip', template: `{{ tip }}` })
export class TipComponent {
  tip = '';
  constructor(private http: HttpClient) {}

  another() {
    this.http.get<Todo>('/todos/' + rand())
      .subscribe(t => this.tip = t.title);
  }
}
""".trim()

internal val HTTP_KT = """
@JsExport
@Component(selector = "app-tip", template = "{{tip}}")
class TipComponent(private val http: HttpClient) {
    var tip = ""
    private val scope = LifecycleScope()

    fun another() {
        scope.launch {
            // Angular's HttpClient Observable, awaited as a coroutine
            val todo = http.get<Todo>("/todos/" + rand()).await()
            tip = todo.title
        }
    }
}
""".trim()

internal val ROUTER_TS = """
const routes: Routes = [
  { path: '', component: TreeComponent },
  {
    path: 'branch', component: BranchComponent, children: [
      { path: '', redirectTo: 'leaf', pathMatch: 'full' },
      { path: 'leaf', component: LeafComponent },
    ],
  },
  { path: 'lazy', loadChildren: () => import('./lazy.routes').then(m => m.LazyRoutes) },
];

@Component({ selector: 'app-branch', template: '<router-outlet></router-outlet>' })
export class BranchComponent {}
""".trim()

internal val ROUTER_KT = """
// Root routes → AOT (standalone) emits provideRouter(routes); JIT (classic NgModule)
// decorates AppRoutingModule with RouterModule.forRoot(routes) — idiomatic for each bootstrap style.
@RoutingModule(
    routes = [
        Route(path = "", component = TreeComponent::class),
        Route(path = "branch", component = BranchComponent::class, children = BranchRoutes::class),
        Route(path = "lazy", loadChildren = LazyRoutes::class),
    ]
)
class AppRoutingModule

// The branch points at another @RoutingModule class; the processor inlines it recursively,
// so routes nest like a tree — the branch shell holds a leaf.
@RoutingModule(routes = [
    Route(path = "", redirectTo = "leaf", pathMatch = "full"),
    Route(path = "leaf", component = LeafComponent::class),
])
class BranchRoutes

// The lazy target is a @RoutingModule (no @NgModule), so loadChildren resolves to its routes array.
@RoutingModule(routes = [Route(path = "", component = LazyComponent::class)])
class LazyRoutes

// The branch is a shell: it renders the nested outlet so only the leaf shows a path.
@JsExport
@Component(selector = "app-branch", template = "<router-outlet></router-outlet>")
class BranchComponent
""".trim()

internal val LIFECYCLE_TS = """
@Component({
  selector: 'app-onpush',
  templateUrl: './onpush.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class OnPushComponent implements OnChanges {
  @Input() count = 0;

  ngOnChanges(changes: SimpleChanges) {
    const c = changes['count'];
    // react to the input change (the alternative to a property setter)
  }
}
""".trim()

internal val LIFECYCLE_KT = """
@JsExport
@Component(
    selector = "app-onpush",
    templateUrl = "./onpush.component.html",
    changeDetection = ChangeDetectionStrategy.OnPush,
    encapsulation = ViewEncapsulation.None,
)
class OnPushComponent : OnChanges {
    @Input
    var count: Int = 0

    override fun ngOnChanges(changes: SimpleChanges) {
        val c = changes["count"]  // SimpleChanges.get operator
        // react to the input change (the alternative to a property setter)
    }
}
""".trim()

