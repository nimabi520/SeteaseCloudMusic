package com.example.seteasecloudmusic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seteasecloudmusic.domain.usecase.GetSearchSuggestionsUseCase
import com.example.seteasecloudmusic.domain.usecase.SearchMusicUseCase
import com.example.seteasecloudmusic.presentation.screens.SearchViewModel

/**
 * SearchViewModel 的工厂类，负责创建带有正确依赖注入的 ViewModel 实例。
 */
class SearchViewModelFactory(
    private val searchMusicUseCase: SearchMusicUseCase,
    private val getSearchSuggestionsUseCase: GetSearchSuggestionsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(
                searchMusicUseCase = searchMusicUseCase,
                getSearchSuggestionsUseCase = getSearchSuggestionsUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
