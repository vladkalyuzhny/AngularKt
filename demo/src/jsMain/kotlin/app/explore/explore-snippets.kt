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
    platformBrowserDynamic(undefined).bootstrapModule(AppModule::class.js)
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
        // names the standalone root @Component — DECIDES the generated main.ts: set → standalone
        // bootstrapApplication(root); unset → classic bootstrapModule of your root @NgModule.
        // (AOT-only; the JIT entry is hand-written, so it needs no such knob.)
        bootstrapComponent.set("app.AppComponent")

        // a stylesheet: an npm specifier or a project-relative path
        styles.add("@angular/material/prebuilt-themes/indigo-pink.css")
        styles.add("src/jsMain/resources/custom-theme.css")

        // a global <script>, injected before the app boots
        scripts.add("node_modules/chart.js/dist/chart.umd.js")
    }
}
""".trim()

private val CUSTOMIZE_JIT = """
// webpack.config.d/app-jit.js — the same assets for JIT
if (config.entry && Array.isArray(config.entry.main)) {
    // 'style' lets webpack resolve a package's CSS export (Angular Material, …)
    config.resolve = config.resolve || {};
    config.resolve.conditionNames = ['style', '...'];

    // style-loader + css-loader inject the stylesheets at runtime
    config.entry.main.push('@angular/material/prebuilt-themes/indigo-pink.css');
    config.entry.main.push('./src/jsMain/resources/custom-theme.css');

    // a global script — the JIT mirror of scripts.add(…)
    config.entry.main.push('chart.js/dist/chart.umd.js');
}
""".trim()

// ---- Example code samples (TypeScript / Angular on the left, Kotlin / AngularKt on the right) ----

internal val SIGNAL_TS = """
@Component({
  selector: 'app-counter',
  template: `<button (click)="dec()">−</button>
             <span>{{ count() }}</span>
             <button (click)="inc()">+</button>
             <p>×2 (computed) = {{ doubled() }}</p>`,
})
export class CounterComponent {
  count = signal(0);
  // derived + memoized: recomputes only when count() changes
  doubled = computed(() => this.count() * 2);
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
        <p>×2 (computed) = {{doubled()}}</p>
    $TQ,
)
class CounterComponent {
    private val lifecycle = LifecycleScope()
    private val state = MutableStateFlow(0)

    // a Kotlin StateFlow exposed as a real Angular signal
    val count = state.asSignal(lifecycle)

    // derived + memoized: recomputes only when count() changes
    val doubled = computed { count() * 2 }

    fun inc() { state.value += 1 }
    fun dec() { state.value -= 1 }
}
""".trim()

internal val IO_TS = """
@Component({
  selector: 'app-child',
  template: `<p>The app says: “{{ name }}”</p>
             <button (click)="ping()">Wave back 👋</button>
             <p *ngIf="waves > 0">You've waved {{ waves }} time(s)</p>`,
})
export class ChildComponent {
  @Input('childName') name = '';
  @Output() notify = new EventEmitter<string>();
  waves = 0;

  ping() {
    this.waves++;
    this.notify.emit(this.waves === 1 ? 'Nice to meet you! 😊' : 'Waving back again! 👋');
  }
}
""".trim()

internal val IO_KT = """
@JsExport
@Component(
    selector = "app-child",
    template = $TQ
        <p>The app says: “{{name}}”</p>
        <button (click)='ping()'>Wave back 👋</button>
        <p *ngIf='waves > 0'>You've waved {{waves}} time(s)</p>
    $TQ,
)
class ChildComponent {
    @Input(alias = "childName")
    var name: String = ""

    @Output
    val notify = EventEmitter<String>()

    var waves = 0

    fun ping() {
        waves++
        notify.emit(if (waves == 1) "Nice to meet you! 😊" else "Waving back again! 👋")
    }
}
""".trim()

internal val DI_TS = """
@Injectable({ providedIn: 'root' })
export class GreetingService {
  greet() { return 'Welcome back! 👋'; }
}

@Component({ selector: 'app-di', template: `{{ message }}` })
export class DiDemoComponent {
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
    fun greet() = "Welcome back! 👋"
}

@JsExport
@Component(selector = "app-di", template = "{{message}}")
class DiDemoComponent(greeting: GreetingService) {
    val message = greeting.greet()
}
""".trim()

internal val KOIN_TS = """
// Angular has no Koin — this is the TS shape Koin's Kotlin DSL replaces:
@Injectable({ providedIn: 'root' })
export class QuoteService {
  next() { return '…'; }
}

@Component({
  selector: 'app-koin',
  template: `<blockquote>“{{ quote }}”</blockquote>
             <button (click)="next()">Next quote</button>`,
})
export class KoinComponent {
  quote: string;
  constructor(private quotes: QuoteService) {
    this.quote = quotes.next();
  }
  next() { this.quote = this.quotes.next(); }
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
@Component(
    selector = "app-koin",
    template = $TQ
        <blockquote>“{{quote}}”</blockquote>
        <button (click)='next()'>Next quote</button>
    $TQ,
)
class KoinDemoComponent : KoinComponent {
    // resolved from Koin's global container, not Angular's injector
    private val quotes: QuoteService by inject()
    var quote = quotes.next()

    fun next() { quote = quotes.next() }
}
""".trim()

internal val SANITIZER_TS = """
@Component({
  selector: 'app-sanitizer',
  template: `<p>Raw string: <span [innerHTML]="raw"></span></p>
             <p>Trusted (bypassSecurityTrustHtml): <span [innerHTML]="safe"></span></p>`,
})
export class SanitizerComponent {
  svg = '<svg>…</svg>';
  raw = svg;              // sanitizer strips the <svg> → blank
  safe: SafeHtml;
  constructor(s: DomSanitizer) {
    this.safe = s.bypassSecurityTrustHtml(svg);  // trusted → renders
  }
}
""".trim()

internal val SANITIZER_KT = """
@JsExport
@Component(
    selector = "app-sanitizer",
    template = $TQ
        <p>Raw string: <span [innerHTML]='raw'></span></p>
        <p>Trusted (bypassSecurityTrustHtml): <span [innerHTML]='safe'></span></p>
    $TQ,
)
class SanitizerComponent(s: DomSanitizer) {
    val svg = "<svg>…</svg>"
    val raw: String = svg                                        // sanitizer strips the <svg> → blank
    val safe: SafeHtml = s.bypassSecurityTrustHtml(svg)          // trusted → renders
}
""".trim()

internal val PIPE_TS = """
@Pipe({ name: 'exclaim' })
export class ExclaimPipe implements PipeTransform {
  transform(value: string): string {
    return value + '!!!';
  }
}
// usage: {{ 'AngularKt' | exclaim }}
""".trim()

internal val PIPE_KT = """
@JsExport
@Pipe(name = "exclaim")
class ExclaimPipe {
    fun transform(value: String): String = value + "!!!"
}
// usage: {{ 'AngularKt' | exclaim }}
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
             <button mat-raised-button color="primary" (click)="read()">Read DOM value</button>
             <p *ngIf="value">You wrote: {{ value }}</p>`,
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
        <button mat-raised-button color='primary' (click)='read()'>Read DOM value</button>
        <p *ngIf='value'>You wrote: {{value}}</p>
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
@Component({
  selector: 'app-ticker',
  template: `<p>{{ display }}</p>
             <button (click)="toggle()">{{ running ? 'Pause' : 'Start' }}</button>
             <button (click)="reset()">Reset</button>`,
})
export class TickerComponent implements OnInit, OnDestroy {
  elapsed = 0;              // tenths of a second
  running = false;
  private sub?: Subscription;

  get display() { return (this.elapsed / 10).toFixed(1) + ' s'; }

  ngOnInit() {
    this.sub = interval(100).subscribe(() => { if (this.running) this.elapsed++; });
  }
  ngOnDestroy() { this.sub?.unsubscribe(); }

  toggle() { this.running = !this.running; }
  reset() { this.running = false; this.elapsed = 0; }
}
""".trim()

internal val COROUTINES_KT = """
@JsExport
@Component(
    selector = "app-ticker",
    template = $TQ
        <p>{{display}}</p>
        <button (click)='toggle()'>{{ running ? 'Pause' : 'Start' }}</button>
        <button (click)='reset()'>Reset</button>
    $TQ,
)
class TickerComponent : OnInit {
    var elapsed = 0          // tenths of a second
    var running = false

    // scoped to the component — auto-cancels on destroy, no ngOnDestroy
    private val lifecycle = LifecycleScope()

    val display get() = (elapsed / 10.0).toString() + " s"

    override fun ngOnInit() {
        lifecycle.launch {
            while (true) { delay(100); if (running) elapsed++ }
        }
    }

    fun toggle() { running = !running }
    fun reset() { running = false; elapsed = 0 }
}
""".trim()

internal val FORMS_TS = """
@Component({
  selector: 'app-form',
  template: `<input [formControl]="nameControl" placeholder="Type your name">
             <p>{{ message }}</p>
             <p>{{ count }} character(s)</p>
             <button mat-raised-button color="primary" (click)="clear()">Clear</button>`,
})
export class FormComponent implements OnInit {
  nameControl = new FormControl('');
  message = 'Start typing above…';
  count = 0;

  ngOnInit() {
    this.nameControl.valueChanges.subscribe(value => {
      const text = value ?? '';
      this.count = text.length;
      this.message = text ? `Hello, ${'$'}{text}! 👋` : 'Start typing above…';
    });
  }

  clear() { this.nameControl.setValue(''); }
}
""".trim()

internal val FORMS_KT = """
@JsExport
@Component(
    selector = "app-form",
    template = $TQ
        <input [formControl]='nameControl' placeholder='Type your name'>
        <p>{{message}}</p>
        <p>{{count}} character(s)</p>
        <button mat-raised-button color='primary' (click)='clear()'>Clear</button>
    $TQ,
)
class FormComponent : OnInit {
    val nameControl = FormControl("")
    var message = "Start typing above…"
    var count = 0
    private val lifecycle = LifecycleScope()

    override fun ngOnInit() {
        lifecycle.launch {
            // valueChanges (an RxJS Observable) consumed as a Kotlin Flow
            nameControl.valueChanges.asFlow().collect { value ->
                val text = "${'$'}value"
                count = text.length
                message = if (text.isBlank()) "Start typing above…" else "Hello, ${'$'}text! 👋"
            }
        }
    }

    fun clear() { nameControl.setValue("") }
}
""".trim()

internal val KTOR_TS = """
@Injectable({ providedIn: 'root' })
export class TodoService {
  constructor(private http: HttpClient) {}
  fetchTodos(limit: number): Observable<Todo[]> {
    return this.http.get<Todo[]>('/todos?_limit=' + limit);
  }
}

@Component({
  selector: 'app-ktor',
  template: `<p *ngIf="loading">Loading…</p>
             <li *ngFor="let t of items" (click)="toggle(t)">
               {{ t.done ? '☑' : '☐' }} {{ t.title }}
             </li>
             <input #box (keyup.enter)="add(box.value)">
             <button (click)="add(box.value)">Add</button>`,
})
export class KtorComponent implements OnInit {
  loading = true;
  items: TaskItem[] = [];
  constructor(private todos: TodoService) {}

  ngOnInit() {
    this.todos.fetchTodos(4).subscribe(todos => {
      this.items = todos.map(t => ({ title: t.title, done: t.completed }));
      this.loading = false;
    });
  }

  toggle(item: TaskItem) { item.done = !item.done; }
  add(title: string) { this.items.push({ title, done: false }); }
}
""".trim()

internal val KTOR_KT = """
@Serializable
data class Todo(val title: String, val completed: Boolean)

class TaskItem(val title: String, var done: Boolean)

@JsExport
@Injectable(providedIn = "root")
class TodoService {
    private val client = HttpClient(Js)
    private val json = Json { ignoreUnknownKeys = true }

    // a plain suspend function; kotlinx.serialization decodes the JSON — no Observable
    internal suspend fun fetchTodos(limit: Int): List<Todo> {
        val body = client.get("/todos?_limit=" + limit).bodyAsText()
        return json.decodeFromString(ListSerializer(Todo.serializer()), body)
    }
}

@JsExport
@Component(
    selector = "app-ktor",
    template = $TQ
        <p *ngIf='loading'>Loading…</p>
        <li *ngFor='let t of items' (click)='toggle(t)'>
            {{ t.done ? '☑' : '☐' }} {{t.title}}
        </li>
        <input #box (keyup.enter)='add(box.value)'>
        <button (click)='add(box.value)'>Add</button>
    $TQ,
)
class KtorComponent(private val todos: TodoService) : OnInit {
    private val lifecycle = LifecycleScope()
    var loading = true
    var items: Array<TaskItem> = arrayOf()

    override fun ngOnInit() {
        lifecycle.launch {
            items = todos.fetchTodos(4)
                .map { TaskItem(it.title, it.completed) }
                .toTypedArray()
            loading = false
        }
    }

    fun toggle(item: TaskItem) { item.done = !item.done }
    fun add(title: String) { items += TaskItem(title, false) }
}
""".trim()

internal val HTTP_TS = """
@Injectable({ providedIn: 'root' })
export class TodoService {
  constructor(private http: HttpClient) {}
  fetch(id: number) {
    return this.http.get<Todo>('https://jsonplaceholder.typicode.com/todos/' + id);
  }
}

@Component({
  selector: 'app-http',
  template: `<p>💡 {{ tip }}</p>
             <button [disabled]="loading" (click)="another()">
               {{ loading ? 'Loading…' : 'Another tip' }}
             </button>`,
})
export class HttpComponent implements OnInit {
  tip = '…';
  loading = false;
  constructor(private api: TodoService) {}

  ngOnInit() { this.another(); }

  another() {
    if (this.loading) return;
    this.loading = true;
    const id = 1 + Math.floor(Math.random() * 199);
    this.api.fetch(id).subscribe(t => {
      this.tip = t.title;
      this.loading = false;
    });
  }
}
""".trim()

internal val HTTP_KT = """
@JsExport
@Injectable(providedIn = "root")
class TodoService(private val http: HttpClient) {
    // Angular's one-shot Observable, awaited as a coroutine
    suspend fun fetch(id: Int): Todo =
        http.get<Todo>("https://jsonplaceholder.typicode.com/todos/" + id).await()
}

@JsExport
@Component(
    selector = "app-http",
    template = $TQ
        <p>💡 {{tip}}</p>
        <button [disabled]='loading' (click)='another()'>
            {{ loading ? 'Loading…' : 'Another tip' }}
        </button>
    $TQ,
)
class HttpComponent(private val api: TodoService) : OnInit {
    private val lifecycle = LifecycleScope()
    var tip = "…"
    var loading = false

    override fun ngOnInit() { another() }

    fun another() {
        if (loading) return
        loading = true
        val id = Random.nextInt(1, 200)
        lifecycle.launch {
            tip = api.fetch(id).title.replaceFirstChar { it.uppercase() }
            loading = false
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
// @RoutingModule compiles to provideRouter(routes).
@RoutingModule(
    routes = [
        Route(path = "", component = TreeComponent::class),
        Route(path = "branch", component = BranchComponent::class, children = BranchRoutes::class),
        Route(path = "lazy", loadChildren = LazyRoutes::class),
    ]
)
class AppRoutingModule

// children points at another @RoutingModule; the processor inlines its routes here.
@RoutingModule(routes = [
    Route(path = "", redirectTo = "leaf", pathMatch = "full"),
    Route(path = "leaf", component = LeafComponent::class),
])
class BranchRoutes

// loadChildren resolves to a @RoutingModule's routes array.
@RoutingModule(routes = [Route(path = "", component = LazyComponent::class)])
class LazyRoutes

// Shell for the nested routes — just a <router-outlet>.
@JsExport
@Component(selector = "app-branch", template = "<router-outlet></router-outlet>")
class BranchComponent
""".trim()

internal val LIFECYCLE_TS = """
// child: OnPush is re-checked only when its @Input changes; ngOnChanges then fires
@Component({
  selector: 'app-onpush',
  template: `<p>count = {{ count }}</p>
             <p>ngOnChanges fired {{ changes }}× · last: {{ lastDelta }}</p>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OnPushComponent implements OnChanges {
  @Input() count = 0;
  changes = 0;
  lastDelta = '—';

  ngOnChanges(changes: SimpleChanges) {
    this.changes++;
    const c = changes['count'];
    this.lastDelta = c ? c.previousValue + ' → ' + c.currentValue : '—';
  }
}

// parent: bumping count changes the child's @Input
@Component({
  selector: 'app-lifecycle',
  template: `<button (click)="bump()">Change @Input → count = {{ count }}</button>
             <app-onpush [count]="count"></app-onpush>`,
})
export class LifecycleComponent {
  count = 0;
  bump() { this.count++; }
}
""".trim()

internal val LIFECYCLE_KT = """
// child: OnPush is re-checked only when its @Input changes; ngOnChanges then fires
@JsExport
@Component(
    selector = "app-onpush",
    template = $TQ
        <p>count = {{count}}</p>
        <p>ngOnChanges fired {{changes}}× · last: {{lastDelta}}</p>
    $TQ,
    changeDetection = ChangeDetectionStrategy.OnPush,
)
class OnPushComponent : OnChanges {
    @Input
    var count: Int = 0
    var changes = 0
    var lastDelta = "—"

    override fun ngOnChanges(changes: SimpleChanges) {
        this.changes++
        val c = changes["count"]  // SimpleChanges.get operator
        lastDelta = c?.let { "${'$'}{it.previousValue} → ${'$'}{it.currentValue}" } ?: "—"
    }
}

// parent: bumping count changes the child's @Input
@JsExport
@Component(
    selector = "app-lifecycle",
    template = $TQ
        <button (click)='bump()'>Change @Input → count = {{count}}</button>
        <app-onpush [count]='count'></app-onpush>
    $TQ,
)
class LifecycleComponent {
    var count = 0
    fun bump() { count++ }
}
""".trim()

