package org.apps.composestoryapp.presentation.story

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apps.composestoryapp.UiEvent
import org.apps.composestoryapp.ViewState
import org.apps.composestoryapp.model.Story
import org.apps.composestoryapp.model.StoryUi
import org.apps.composestoryapp.presentation.home.StoryState
import org.apps.composestoryapp.repository.StoryRepository
import org.apps.composestoryapp.reverseGeocode
import org.apps.composestoryapp.room.StoryFavoriteRepository
import org.apps.composestoryapp.room.toStoryUi
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val repository: StoryRepository,
    private val favoriteRepo: StoryFavoriteRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Room
    val favorites: StateFlow<List<StoryUi>> = favoriteRepo.getFavorites()
        .map { list -> list.map { it.toStoryUi() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun isFavorite(id: String): StateFlow<Boolean> =
        favoriteRepo.isFavorite(id)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                false
            )

    fun toggleFavorite(story: Story, isFavorite: Boolean) {
        viewModelScope.launch {
            if (isFavorite) {
                favoriteRepo.removeFavorite(story)
            } else {
                favoriteRepo.toggleFavorite(story)
            }
        }
    }

    // API
    private val _uiState = MutableStateFlow(StoryState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val locationCache = mutableMapOf<String, String>()

    val stories: Flow<PagingData<StoryUi>> =
        repository.getAllStories()
            .map { pagingData ->
                pagingData.map { story ->
                    StoryUi(story)
                }
            }
            .cachedIn(viewModelScope)

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

    fun getStoryDetail(id: String){
        viewModelScope.launch {
            _uiState.update { it.copy(storyState = ViewState.Loading)}

            repository.getStoryDetail(id)
                .onSuccess { story ->
                    val storyUi = StoryUi(story = story)
                    _uiState.update { it.copy(storyState = ViewState.Success(storyUi)) }

                    resolveLocationForDetail(storyUi)

                    _eventFlow.emit(UiEvent.NavigateStoryDetail(id))
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(storyState = ViewState.Error(error.message ?: "Terjadi Kesalahan"))
                    }
                }
        }
    }

    fun resolveLocation(
        lat: Double?,
        lon: Double?,
        onLocationResolved: (String) -> Unit
    ) {
        if (lat == null || lon == null) return
        val key = "$lat,$lon"

        locationCache[key]?.let { cached ->
            onLocationResolved(cached)
            return
        }

        reverseGeocode(
            context = context,
            lat = lat,
            lon = lon,
            onResult = { location ->
                locationCache[key] = location
                onLocationResolved(location)
            },
            onError = {}
        )
    }

    private fun resolveLocationForDetail(storyUi: StoryUi) {
        resolveLocation(
            lat = storyUi.story.lat.toDouble(),
            lon = storyUi.story.lon.toDouble()
        ) { location ->
            updateDetailLocation(location)
        }
    }

    private fun updateDetailLocation(location: String) {
        val current =
            (_uiState.value.storyState as? ViewState.Success)?.data
                ?: return

        _uiState.update {
            it.copy(
                storyState = ViewState.Success(
                    current.copy(locationName = location)
                )
            )
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