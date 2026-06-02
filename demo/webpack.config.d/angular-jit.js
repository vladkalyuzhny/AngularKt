// JIT-only webpack tweaks. Guard on a browser entry being present: in AOT mode the
// Kotlin/JS target is a `library()`, which has no executable webpack entry, so this
// whole block is a no-op there (and the real bundle is built by `ng build` in ng-aot/,
// which never reads this file — keeping @angular/compiler out of the AOT bundle).
if (config.entry && Array.isArray(config.entry.main)) {
    // zone.js and @angular/compiler must run before any @angular module is evaluated.
    // Angular libraries ship in "partial" Ivy format: their `ɵfac = ɵɵngDeclareFactory(...)`
    // initializers JIT-link at class-definition time and throw if @angular/compiler isn't
    // loaded yet; zone.js must patch the environment first too.
    //
    // This can't be done from Kotlin main(): the entry module's `@angular/*` imports are
    // ES modules, so they evaluate (and throw) before main()'s body runs. Prepending these
    // as their own entries makes webpack evaluate them first.
    config.entry.main = ['zone.js', '@angular/compiler', ...config.entry.main];

    // Bundle Angular Material's prebuilt theme: style-loader + css-loader inject it into
    // the document at runtime, so no external stylesheet is needed (Material Icons + Roboto
    // fonts still load from a CDN <link> in index.html). The theme is only resolvable
    // because @angular/material's package.json `exports` exposes it under the `style`
    // condition — so conditionNames must include 'style' ('...' keeps webpack's defaults:
    // import/module/browser/…).
    config.resolve = config.resolve || {};
    config.resolve.conditionNames = ['style', '...'];
    config.entry.main.push('@angular/material/prebuilt-themes/indigo-pink.css');

    // highlight.js code theme for the "Explore" snippets — a plain .css file (no export
    // condition needed), injected by the style-loader/css-loader rule. Mirrors aotConfig.styles
    // in build.gradle.kts so JIT and AOT render the snippets identically.
    config.entry.main.push('highlight.js/styles/github.css');
}
