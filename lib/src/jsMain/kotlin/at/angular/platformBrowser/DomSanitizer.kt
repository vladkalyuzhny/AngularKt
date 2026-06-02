@file:JsModule("@angular/platform-browser")

package at.angular.platformBrowser

/**
 * @see: https://angular.dev/api/platform-browser/SafeValue
 */
external interface SafeValue

/** @see: https://angular.dev/api/platform-browser/SafeHtml */
external interface SafeHtml : SafeValue

/** @see: https://angular.dev/api/platform-browser/SafeStyle */
external interface SafeStyle : SafeValue

/** @see: https://angular.dev/api/platform-browser/SafeScript */
external interface SafeScript : SafeValue

/** @see: https://angular.dev/api/platform-browser/SafeUrl */
external interface SafeUrl : SafeValue

/** @see: https://angular.dev/api/platform-browser/SafeResourceUrl */
external interface SafeResourceUrl : SafeValue

/**
 * @see: https://angular.dev/api/platform-browser/DomSanitizer
 */
external class DomSanitizer {
    fun bypassSecurityTrustHtml(value: String): SafeHtml
    fun bypassSecurityTrustStyle(value: String): SafeStyle
    fun bypassSecurityTrustScript(value: String): SafeScript
    fun bypassSecurityTrustUrl(value: String): SafeUrl
    fun bypassSecurityTrustResourceUrl(value: String): SafeResourceUrl
}
