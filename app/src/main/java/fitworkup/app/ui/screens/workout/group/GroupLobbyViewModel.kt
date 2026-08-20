package com.fitworkup.app.ui.screens.workout.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.remote.api.GroupApiService
import com.fitworkup.app.data.remote.dto.CreateGroupSessionRequestDto
import com.fitworkup.app.data.remote.dto.GroupSessionDto
import com.fitworkup.app.data.remote.dto.JoinGroupSessionRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

data class GroupLobbyUiState(
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,
    val session: GroupSessionDto? = null,
    val errorMessage: String? = null,
    val leftLobby: Boolean = false
)

@HiltViewModel
class GroupLobbyViewModel @Inject constructor(
    private val api: GroupApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupLobbyUiState())
    val uiState: StateFlow<GroupLobbyUiState> = _uiState.asStateFlow()

    private var initialized = false
    private var pollingJob: Job? = null

    fun initialize(
        mode: String,
        value: String,
        goalKm: Double?,
        friendsOnly: Boolean
    ) {
        if (initialized) return
        initialized = true
        viewModelScope.launch {
            _uiState.value = GroupLobbyUiState(isLoading = true)
            runCatching {
                if (mode == "create") {
                    api.create(
                        CreateGroupSessionRequestDto(
                            name = value,
                            targetDistanceKm = goalKm,
                            maxParticipants = 5,
                            friendsOnly = friendsOnly
                        )
                    )
                } else {
                    api.join(JoinGroupSessionRequestDto(value))
                }
            }.onSuccess(::handleInitialResponse)
                .onFailure { showError(connectionMessage(it)) }
        }
    }

    fun retry(
        mode: String,
        value: String,
        goalKm: Double?,
        friendsOnly: Boolean
    ) {
        initialized = false
        initialize(mode, value, goalKm, friendsOnly)
    }

    fun refresh() {
        val code = _uiState.value.session?.code ?: return
        viewModelScope.launch { updateFrom { api.get(code) } }
    }

    fun setReady(ready: Boolean) {
        val code = _uiState.value.session?.code ?: return
        viewModelScope.launch { updateFrom(processing = true) { api.setReady(code, ready) } }
    }

    fun startWorkout() {
        val code = _uiState.value.session?.code ?: return
        viewModelScope.launch { updateFrom(processing = true) { api.start(code) } }
    }

    fun leaveLobby() {
        val code = _uiState.value.session?.code ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)
            runCatching { api.leave(code) }
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        pollingJob?.cancel()
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            leftLobby = true
                        )
                    } else {
                        showError(responseMessage(response))
                    }
                }
                .onFailure { showError(connectionMessage(it)) }
        }
    }

    private fun handleInitialResponse(response: Response<GroupSessionDto>) {
        if (response.isSuccessful && response.body() != null) {
            val session = requireNotNull(response.body())
            _uiState.value = GroupLobbyUiState(isLoading = false, session = session)
            startPolling(session.code)
        } else {
            initialized = false
            showError(responseMessage(response))
        }
    }

    private suspend fun updateFrom(
        processing: Boolean = false,
        request: suspend () -> Response<GroupSessionDto>
    ) {
        if (processing) {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)
        }
        runCatching { request() }
            .onSuccess { response ->
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isProcessing = false,
                        session = response.body(),
                        errorMessage = null
                    )
                } else {
                    showError(responseMessage(response))
                }
            }
            .onFailure { showError(connectionMessage(it)) }
    }

    private fun startPolling(code: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive && _uiState.value.session?.status == "LOBBY") {
                delay(3_000)
                runCatching { api.get(code) }.onSuccess { response ->
                    if (response.isSuccessful && response.body() != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            session = response.body(),
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isProcessing = false,
            errorMessage = message
        )
    }

    private fun connectionMessage(error: Throwable): String =
        if (error is java.io.IOException) {
            "Não foi possível conectar à API. Verifique sua conexão."
        } else {
            error.message ?: "Não foi possível concluir a operação."
        }

    private fun responseMessage(response: Response<*>): String {
        val rawBody = response.errorBody()?.string().orEmpty()
        return runCatching {
            val json = JSONObject(rawBody)
            json.optString("message").ifBlank { json.optString("error") }
        }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: "Não foi possível concluir a operação (${response.code()})."
    }
}
