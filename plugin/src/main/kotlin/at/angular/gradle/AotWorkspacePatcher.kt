package at.angular.gradle

import java.io.File

/**
 * In-place JSON surgery on a freshly `ng new`-scaffolded workspace, so Angular AOT builds the
 * AngularKt-generated entry instead of the default app. Replaces the former `node -e` script,
 * dropping the dependency on Node for the patch step and the env-var/`$`-escaping plumbing that
 * came with it. Unknown keys in each file are preserved.
 */
internal object AotWorkspacePatcher {
    fun patch(
        ngAot: File,
        tsModule: String,
        ngDeps: Map<String, String>,
        libRelativePath: String,
        globalStyles: List<String>,
        globalScripts: List<String>,
    ) {
        patchAngularJson(ngAot, globalStyles, globalScripts)
        patchPackageJson(ngAot, tsModule, ngDeps, libRelativePath)
        patchTsconfigApp(ngAot)
        patchTsconfig(ngAot)
    }

    /** angular.json — build the generated entry, resolve the file: lib, lift budgets, add assets. */
    @Suppress("UNCHECKED_CAST")
    private fun patchAngularJson(ngAot: File, globalStyles: List<String>, globalScripts: List<String>) {
        val file = File(ngAot, "angular.json")
        val angular = file.readJsonObject()
        // `ng new` scaffolds exactly one project; take it.
        val project = angular.obj("projects").values.first() as MutableMap<String, Any?>
        val build = project.obj("architect").obj("build")
        val opts = build.obj("options")
        opts["index"] = "src/index.html"
        opts["preserveSymlinks"] = true
        opts["polyfills"] = listOf("zone.js")
        // The entry-point option is builder-specific. The esbuild `application` builder (Angular 17+
        // default) takes `browser` and understands `externalDependencies`; the webpack `:browser`
        // builder (Angular 16 default) takes `main` and has neither in its schema — writing `browser`
        // there fails validation with "must have required property 'main'". Detect from the scaffolded
        // builder id and write the shape that builder expects, removing the other key.
        if ((build["builder"] as? String)?.endsWith(":application") == true) {
            opts.remove("main")
            opts["browser"] = "src/generated/main.ts"
            opts["externalDependencies"] = listOf("ws")
        } else {
            opts.remove("browser")
            opts["main"] = "src/generated/main.ts"
        }
        build.obj("configurations").obj("production")["budgets"] = listOf(
            mapOf("type" to "initial", "maximumWarning" to "4MB", "maximumError" to "8MB"),
            mapOf("type" to "anyComponentStyle", "maximumWarning" to "6kB", "maximumError" to "8kB"),
        )
        val schematics = project.obj("schematics")
        for (kind in listOf("component", "directive", "pipe")) {
            schematics["@schematics/angular:$kind"] = mapOf("standalone" to false, "skipTests" to true)
        }
        // Mirror the JIT global styles/scripts (angularkt.assets.json) so AOT renders the same —
        // appended to whatever `ng new` scaffolded (`styles: ["src/styles.css"]`), deduped.
        appendAssets(opts, "styles", globalStyles)
        appendAssets(opts, "scripts", globalScripts)
        file.writeJson(angular)
    }

    /** Append normalized [assets] to the build option [key] (`styles`/`scripts`), keeping order, no dups. */
    private fun appendAssets(opts: MutableMap<String, Any?>, key: String, assets: List<String>) {
        if (assets.isEmpty()) return

        val existing = (opts[key] as? List<*>)?.map { it.toString() } ?: emptyList()
        // A bare package specifier (`@angular/material/prebuilt-themes/azure-blue.css`) is rewritten to
        // its `node_modules/…` path so Angular's `styles`/`scripts` resolution loads the file directly,
        // bypassing the package's `exports` map (which may expose the asset only under a `style`
        // condition that the AOT builder doesn't apply). Project-relative entries pass through untouched.
        val normalized = assets.map { spec ->
            if (spec.startsWith(".") || spec.startsWith("/") || spec.startsWith("node_modules/")) spec
            else "node_modules/$spec"
        }
        opts[key] = (existing + normalized).distinct()
    }

    /** package.json — the Kotlin library (file: dep) plus the app's @angular deps. */
    private fun patchPackageJson(
        ngAot: File,
        tsModule: String,
        ngDeps: Map<String, String>,
        libRelativePath: String
    ) {
        val file = File(ngAot, "package.json")
        val pkg = file.readJsonObject()
        val deps = pkg.obj("dependencies")
        deps[tsModule] = "file:$libRelativePath"
        ngDeps.forEach { (name, version) -> deps[name] = version }
        file.writeJson(pkg)
    }

    /**
     * tsconfig.app.json — compile the generated entry, not the (deleted) default main.ts. Patch the
     * scaffolded file rather than rewriting it from scratch, so Angular's defaults (strictTemplates
     * and the rest of `angularCompilerOptions`) survive and AOT stays as strict as JIT.
     */
    private fun patchTsconfigApp(ngAot: File) {
        val file = File(ngAot, "tsconfig.app.json")
        val tree = file.readJsonObject(jsonc = true)
        tree.obj("compilerOptions")["types"] = emptyList<String>()
        tree["files"] = listOf("src/generated/main.ts")
        tree["include"] = listOf("src/**/*.d.ts")
        file.writeJson(tree)
    }

    /** tsconfig.json — Angular needs experimentalDecorators + skipLibCheck; ensure both (JSONC read). */
    private fun patchTsconfig(ngAot: File) {
        val file = File(ngAot, "tsconfig.json")
        val tree = file.readJsonObject(jsonc = true)
        val compilerOptions = tree.obj("compilerOptions")
        compilerOptions["experimentalDecorators"] = true
        // The Kotlin/JS-generated library .d.ts references Angular types (OnInit, ElementRef, Signal)
        // by bare name without emitting TS imports for them — type-checking its body fails with
        // "Cannot find name". Angular 17+ scaffolds skipLibCheck:true by default; 16 doesn't, so set
        // it for a uniform workspace. (App sources still type-check; only dependency .d.ts bodies skip.)
        compilerOptions["skipLibCheck"] = true
        file.writeJson(tree)
    }
}
