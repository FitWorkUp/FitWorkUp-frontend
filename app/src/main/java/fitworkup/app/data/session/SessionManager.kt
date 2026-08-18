package com.fitworkup.app.data.session

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class SessionManager @Inject constructor(
    private val tokenStore: TokenStore
) {
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    @Synchronized
    fun invalidateIfCurrent(expectedToken: String) {
        if (tokenStore.getTokenBlocking() != expectedToken) return

        tokenStore.clearBlocking()
        _sessionExpired.tryEmit(Unit)
    }
}
