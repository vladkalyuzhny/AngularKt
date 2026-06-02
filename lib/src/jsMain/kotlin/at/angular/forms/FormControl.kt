@file:JsModule("@angular/forms")

package at.angular.forms

import rxjs.Observable

/**
 * @see: https://angular.dev/api/forms/FormControl
 */
external class FormControl(initialValue: dynamic = definedExternally) {
    val value: dynamic
    val valueChanges: Observable<dynamic>
    val statusChanges: Observable<dynamic>
    fun setValue(value: dynamic)
}
