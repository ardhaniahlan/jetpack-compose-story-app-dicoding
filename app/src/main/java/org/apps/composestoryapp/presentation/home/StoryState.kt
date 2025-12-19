package org.apps.composestoryapp.presentation.home

import android.net.Uri
import org.apps.composestoryapp.ViewState
import org.apps.composestoryapp.model.AddStoryResponse
import org.apps.composestoryapp.model.Story

data class StoryState(
    val description: String = "",
    val photoFile: Uri? = null,

    val storyListState: ViewState<List<Story>> = ViewState.Idle,
    val storyState: ViewState<Story> = ViewState.Idle,
    val addStoryState: ViewState<AddStoryResponse> = ViewState.Idle
)
