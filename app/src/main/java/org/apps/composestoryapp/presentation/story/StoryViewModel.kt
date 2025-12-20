package org.apps.composestoryapp.presentation.story

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apps.composestoryapp.UiEvent
import org.apps.composestoryapp.ViewState
import org.apps.composestoryapp.model.StoryUi
import org.apps.composestoryapp.presentation.home.StoryState
import org.apps.composestoryapp.repository.StoryRepository
import org.apps.composestoryapp.reverseGeocode
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val repository: StoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoryState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val locationCache = mutableMapOf<String, String>()

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
                repository.addStory(
                    description = _uiState.value.description,
                    photoUri = uri,
                    lat = (if (_uiState.value.useLocation) _uiState.value.lat else null),
                    lon = (if (_uiState.value.useLocation) _uiState.value.lon else null)
                ).onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            addStoryState = ViewState.Success(response),
                            description = "",
                            photoFile = null,

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

    fun loadStoriesForMap() {
        viewModelScope.launch {
            _uiState.update { it.copy(mapState = ViewState.Loading) }

            repository.getStoriesWithLocation()
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(mapState = ViewState.Success(list))
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(mapState = ViewState.Error(e.message ?: "Error"))
                    }
                }
        }
    }

    fun setLocation(lat: Double, lon: Double) {
        _uiState.update {
            it.copy(
                lat = lat,
                lon = lon
            )
        }
    }

    fun setUseLocation(use: Boolean) {
        _uiState.update {
            it.copy(
                useLocation = use,
                lat = if (!use) null else it.lat,
                lon = if (!use) null else it.lon
            )
        }
    }

    private fun resolveLocations(stories: List<StoryUi>) {
        stories.forEach { storyUi ->
            val lat = storyUi.story.lat
            val lon = storyUi.story.lon
            val key = "$lat,$lon"

            if (locationCache.containsKey(key)) {
                updateStoryLocation(storyUi.story.id, locationCache[key])
            } else {
                reverseGeocode(
                    context = context,
                    lat = lat.toDouble(),
                    lon = lon.toDouble(),
                    onResult = { location ->
                        locationCache[key] = location
                        updateStoryLocation(storyUi.story.id, location)
                    },
                    onError = {}
                )
            }
        }
    }

    private fun updateStoryLocation(storyId: String, location: String?) {
        val current =
            (_uiState.value.storyListState as? ViewState.Success)?.data ?: return

        _uiState.update {
            it.copy(
                storyListState = ViewState.Success(
                    current.map { ui ->
                        if (ui.story.id == storyId)
                            ui.copy(locationName = location)
                        else ui
                    }
                )
            )
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
                    val storyUiList = storyList.map {
                        StoryUi(story = it)
                    }

                    _uiState.update { it.copy(storyListState = ViewState.Success(storyUiList)) }

                    resolveLocations(storyUiList)
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