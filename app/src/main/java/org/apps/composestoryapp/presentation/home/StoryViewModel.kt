package org.apps.composestoryapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apps.composestoryapp.UiEvent
import org.apps.composestoryapp.ViewState
import org.apps.composestoryapp.repository.StoryRepository
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val repository: StoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoryState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun getAllStories(
        page: Int,
        size: Int,
    ){
        viewModelScope.launch {
            _uiState.update { it.copy(storyListState = ViewState.Loading)}

            repository.getAllStories(page, size)
                .onSuccess { storyList ->
                    _uiState.update { it.copy(storyListState = ViewState.Success(storyList)) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(storyListState = ViewState.Error(error.message ?: "Terjadi Kesalahan"))
                    }
                }
        }
    }

    fun getStoryDetail(id: String){
       viewModelScope.launch {
           _uiState.update { it.copy(storyState = ViewState.Loading)}

           repository.getStoryDetail(id)
               .onSuccess { story ->
                   _uiState.update { it.copy(storyState = ViewState.Success(story)) }
                   _eventFlow.emit(UiEvent.NavigateStoryDetail(id))
               }
               .onFailure { error ->
                   _uiState.update {
                       it.copy(storyState = ViewState.Error(error.message ?: "Terjadi Kesalahan"))
                   }
               }
       }
    }
}