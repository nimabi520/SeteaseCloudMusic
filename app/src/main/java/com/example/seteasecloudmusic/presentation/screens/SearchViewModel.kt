package com.example.seteasecloudmusic.presentation.screens

import androidx.lifecycle.ViewModel
import com.example.seteasecloudmusic.domain.usecase.SearchMusicUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val errorMessage: String? = null
)

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    fun onQueryChanged(newQuery: String) {
        if (newQuery.isBlank()){
            current.copy(
                query = newQuery,
                tracks = emptyList(),
                errorMessage = null
                hasSearched = false
            )
        } else {
            current.copy(
                query = newQuery,
                errorMessage = null)
        }
    }

    fun onSearchSubmit(){
        
    }

    fun onRetryClick(){

    }

    fun onClearQuery(){

    }



}