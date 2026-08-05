package com.fitworkup.app.ui.screens.ranking.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.repository.RankingRepository
import com.fitworkup.app.domain.model.RankingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val rankingRepository: RankingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RankingUiState>(RankingUiState.Loading)
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        loadRanking()
    }

    fun loadRanking() {
        viewModelScope.launch {
            _uiState.value = RankingUiState.Loading
            rankingRepository.fetchWeeklyRanking()
                .onSuccess { state ->
                    _uiState.value = state
                }
                .onFailure {
                    _uiState.value = RankingUiState.Error("Não foi possível carregar a liga. Tente novamente.")
                }
        }
    }
}