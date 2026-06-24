// Project-specific JIT webpack tweaks. The framework-mandatory bit (prepending zone.js +
// @angular/compiler to the entry) is no longer here — the AngularKt plugin generates it into
// webpack.config.d/angularkt-jit.generated.js. What stays below are this app's own choices.
//
// Guard on a browser entry being present: in AOT mode the Kotlin/JS target is a `library()`,
// which has no executable webpack entry, so this whole block is a no-op there (and the real
// bundle is built by `ng build` in ng-aot/, which never reads this file).
if (config.entry && Array.isArray(config.entry.main)) {
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

    // SPA deep links: the router uses real paths (`/signal`, `/router/branch/leaf`), so a refresh
    // on one of them must serve index.html instead of 404ing. `ng serve` does this by default; the
    // JIT webpack-dev-server needs it spelled out. (No-op when devServer is absent, e.g. a build.)
    config.devServer = config.devServer || {};
    config.devServer.historyApiFallback = true;
}
