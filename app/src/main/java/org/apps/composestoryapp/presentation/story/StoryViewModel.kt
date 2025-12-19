package org.apps.composestoryapp.presentation.story

import android.net.Uri
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
import org.apps.composestoryapp.presentation.home.StoryState
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

    fun addStory() {

        if (_uiState.value.description.isEmpty()){
            _uiState.update { it.copy(addStoryState = ViewState.Error("Deskripsi tidak boleh kosong")) }
            return
        }

        if (_uiState.value.photoFile == null){
            _uiState.update { it.copy(addStoryState = ViewState.Error("Pilih gambar terlebih dahulu")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(addStoryState = ViewState.Loading) }

            _uiState.value.photoFile?.let { uri ->
                repository.addStory(_uiState.value.description, uri).onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            addStoryState = ViewState.Success(response),
                            description = "",
                            photoFile = null
                        )
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(addStoryState = ViewState.Error(exception.message ?: "Unknown error"))
                    }
                }
            }
        }
    }

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

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun setImageUri(uri: Uri?) {
        _uiState.update { it.copy(photoFile = uri) }
    }

    fun clearForm() {
        _uiState.update { it.copy(description = "", photoFile = null) }
    }
}