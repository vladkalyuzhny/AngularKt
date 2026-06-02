package at.angular.processor.bridge.references

/**
 * A TypeScript symbol referenced in a generated bridge, paired with the `import` that brings it
 * in. Refs are embedded as leaves of the [TsExpression] values a bridge builds, so its import block is a
 * pure function of those values ([importsFrom]) — there is no separate registration step to keep in
 * sync, and no order-of-calls to get wrong.
 */
sealed interface TsReference {
    /** How the symbol is written at the use site. */
    val name: String

    /** Module specifier the symbol is imported from. */
    val module: String

    /** Token placed inside `import { … }`; differs from [name] only for aliased imports. */
    val importSpecifier: String get() = name

    /** A symbol imported by package + bare name, e.g. `{ NgModule } from '@angular/core'`. */
    data class External(override val module: String, override val name: String) : TsReference

    /** One of our own sibling bridges, imported from its `./Name` module. */
    data class Local(override val name: String) : TsReference {
        override val module: String get() = "./$name"
    }

    /** The Kotlin-compiled class a bridge `extends`, imported aliased as `${simpleName}Impl`. */
    data class Impl(override val module: String, val simpleName: String) : TsReference {
        override val name: String get() = "${simpleName}Impl"
        override val importSpecifier: String get() = "$simpleName as $name"
    }

    /**
     * A member access on an imported symbol — `Owner.member` at the use site, but only [owner] is
     * imported (e.g. `ViewEncapsulation.None` imports just `ViewEncapsulation`).
     */
    data class Member(override val module: String, val owner: String, val member: String) : TsReference {
        override val name: String get() = "$owner.$member"
        override val importSpecifier: String get() = owner
    }
}
