package org.apps.composestoryapp.presentation.home

import android.net.Uri
import org.apps.composestoryapp.ViewState
import org.apps.composestoryapp.model.AddStoryResponse
import org.apps.composestoryapp.model.Story
import org.apps.composestoryapp.model.StoryUi

data class StoryState(
    val description: String = "",
    val photoFile: Uri? = null,

    val useLocation: Boolean = false,
    val lat: Double? = null,
    val lon: Double? = null,

    val storyListState: ViewState<List<StoryUi>> = ViewState.Idle,
    val mapState: ViewState<List<Story>> = ViewState.Idle,
    val storyState: ViewState<StoryUi> = ViewState.Idle,
    val addStoryState: ViewState<AddStoryResponse> = ViewState.Idle,
)
