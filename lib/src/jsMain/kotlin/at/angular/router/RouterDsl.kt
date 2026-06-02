package at.angular.router

import kotlinx.coroutines.await

/** Coroutine-friendly navigation: `suspend` instead of a Promise. */
suspend fun Router.navigate(vararg commands: String): Boolean =
    navigate(commands.unsafeCast<Array<dynamic>>()).await()
