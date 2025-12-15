package org.apps.composestoryapp

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    object Navigate : UiEvent()
    data class NavigateStoryDetail(val storyId: String) : UiEvent()
}