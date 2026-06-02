package rxjs

external interface Observable<out T> {
    fun subscribe(next: (T) -> Unit): Subscription
}
